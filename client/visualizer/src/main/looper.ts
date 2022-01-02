import { Game, GameWorld, Match, Metadata, schema } from 'battlecode-playback';
import * as cst from '../constants';
import * as config from '../config';
import * as imageloader from '../imageloader';

import Controls from '../main/controls';
import Splash from '../main/splash';

import { Stats, Console, MatchQueue, Profiler } from '../sidebar/index';
import { GameArea, Renderer, NextStep, TickCounter } from '../gamearea/index';

import WebSocketListener from '../main/websocket';

// import { electron } from '../main/electron-modules';
import { TeamStats } from 'battlecode-playback/out/gameworld';

import { Tournament, readTournament } from '../main/tournament';
import { ARCHON } from '../constants';

/*
Responsible for a single match in the visualizer.
*/
export default class Looper {

    private loopID: number | null;
    private goalUPS: number;
    private externalSeek: boolean;
    private interpGameTime: number;
    private nextStep: NextStep;
    private lastTime: number | null;
    private lastTurn: number | null;
    private rendersPerSecond: TickCounter;
    private updatesPerSecond: TickCounter;
    private lastSelectedID: number | undefined;
    private renderer: Renderer;
    private loadedProfiler: boolean;

    private console: Console;

    constructor(public match: Match, public meta: Metadata,
        private conf: config.Config, private imgs: imageloader.AllImages,
        private controls: Controls, private stats: Stats,
        private gamearea: GameArea, cconsole: Console,
        private matchqueue: MatchQueue, private profiler?: Profiler,
        private mapinfo: string = "",
        showTourneyUpload: boolean = true) {

        this.console = cconsole;

        this.conf.mode = config.Mode.GAME;
        this.conf.splash = false;
        this.gamearea.setCanvas();

        // Cancel previous games if they're running
        this.clearScreen();

        // rotate tall maps
        if (this.conf.doRotate) this.conf.doingRotate = (match.current.maxCorner.y - match.current.minCorner.y) > (match.current.maxCorner.x - match.current.minCorner.x);
        else this.conf.doingRotate = false;

        // Reset the canvas
        this.gamearea.setCanvasDimensions(match.current);

        // Reset the stats bar
        let teamNames = new Array();
        let teamIDs = new Array();
        for (let team in meta.teams) {
            teamNames.push(meta.teams[team].name);
            teamIDs.push(meta.teams[team].teamID);
        }
        this.stats.initializeGame(teamNames, teamIDs);
        const extraInfo = (this.mapinfo ? this.mapinfo + "\n" : "") + (this.conf.doingRotate ? " (Map rotated and flipped! Disable for new matches with 'Z'.)" : "");
        this.stats.setExtraInfo(extraInfo);
        if (!showTourneyUpload) this.stats.hideTourneyUpload();

        // keep around to avoid reallocating
        this.nextStep = new NextStep();

        // Last selected robot ID to display extra info

        this.lastSelectedID = undefined;
        const onRobotSelected = (id: number | undefined) => {
            this.lastSelectedID = id;
            this.console.setIDFilter(id);
        };
        const onMouseover = (x: number, y: number, xrel: number, yrel: number, rubble: number, lead: number, gold: number) => {
            // Better make tile type and hand that over
            controls.setTileInfo(x, y, xrel, yrel, rubble, lead, gold);
        };

        // Configure renderer for this match
        // (radii, etc. may change between matches)
        this.renderer = new Renderer(this.gamearea.canvas, this.imgs,
            this.conf, meta as Metadata, onRobotSelected, onMouseover);

        // How fast the simulation should progress
        //  this.goalUPS = this.controls.getUPS();
        // if (this.conf.tournamentMode) {
        // Always pause on load. Mitigates funky behaviour like 100 rounds playing before any rendering occurs.
        this.goalUPS = 0;
        // }

        // A variety of stuff to track how fast the simulation is going
        this.rendersPerSecond = new TickCounter(.5, 100);
        this.updatesPerSecond = new TickCounter(.5, 100);

        // The current time in the simulation, interpolated between frames
        this.interpGameTime = 0;
        // The time of the last frame
        this.lastTime = null;
        this.lastTurn = null;
        // whether we're seeking
        this.externalSeek = false;

        this.controls.updatePlayPauseButton(this.isPaused());

        if (this.profiler)
            this.profiler.reset();

        this.loadedProfiler = false;

        this.loopID = window.requestAnimationFrame((curTime) => this.loop.call(this, curTime));

    };

    clearScreen() {
        // TODO clear screen
        if (this.loopID !== null) {
            window.cancelAnimationFrame(this.loopID);
            this.loopID = null;
        }
    }

    isPaused() {
        return this.goalUPS == 0;
    }

    onTogglePause() {
        this.goalUPS = this.goalUPS === 0 ? this.controls.getUPS() : 0;
    }

    onToggleUPS() {
        this.goalUPS = this.isPaused() ? 0 : this.controls.getUPS();
    }

    onSeek(turn: number) {
        this.externalSeek = true;
        this.match.seek(turn);
        this.interpGameTime = turn;
    };

    onStop() {
        if (!(this.goalUPS == 0)) {
            this.controls.pause();
        }
        this.onSeek(0);
    };

    onGoEnd() {
        if (!(this.goalUPS == 0)) {
            this.controls.pause();
        }
        this.onSeek(this.match['_farthest'].turn);
    };

    onStepForward() {
        if (!(this.goalUPS == 0)) {
            this.controls.pause();
        }
        if (this.match.current.turn < this.match['_farthest'].turn) {
            this.onSeek(this.match.current.turn + 1);
        }
    };

    onStepBackward() {
        if (!(this.goalUPS == 0)) {
            this.controls.pause();
        }
        if (this.match.current.turn > 0) {
            this.onSeek(this.match.current.turn - 1);
        }
    };

    // cleanup when looper is destroyed (match is switched / ended)
    die() {
        this.clearScreen();
        this.goalUPS = 0;
        this.controls.pause();
        this.controls.removeInfoString();
        this.controls.setDefaultText();
        this.controls.setDefaultUPS();
    }

    private loop(curTime) {

        let delta = 0;
        if (this.lastTime === null) {
            // first simulation step
            // do initial stuff?
        } else if (this.externalSeek) {
            if (this.match.current.turn === this.match.seekTo) {
                this.externalSeek = false;
            }
        } else if (this.goalUPS < 0 && this.match.current.turn === 0) {
            this.controls.pause();
        } else if (Math.abs(this.interpGameTime - this.match.current.turn) < 10) {
            // only update time if we're not seeking
            delta = this.goalUPS * (curTime - this.lastTime) / 1000;
            //console.log("igt:",this.interpGameTime, "delta", delta, "goalUPS", this.goalUPS, "lastTime", this.lastTime);
            this.interpGameTime += delta;

            // tell the simulation to go to our time goal
            this.match.seek(this.interpGameTime | 0);
        } if (this.match['_farthest'].winner !== null && this.match.current.turn === this.match['_farthest'].turn && this.match.current.turn !== 0) {
            // this.match have ended
            this.controls.onFinish(this.match, this.meta);
        }

        // update fps
        this.rendersPerSecond.update(curTime, 1);
        this.updatesPerSecond.update(curTime, delta);

        this.controls.setTime(
            this.match.current.turn,
            this.match['_farthest'].turn,
            this.controls.getUPS(),
            this.isPaused(),
            this.rendersPerSecond.tps,
            Math.abs(this.updatesPerSecond.tps) < Math.max(0, Math.abs(this.goalUPS) - 2)
        );

        // run simulation
        // this may look innocuous, but it's a large chunk of the run time
        this.match.compute(30 /* ms */); // An ideal FPS is around 30 = 1000/30, so when compute takes its full time
        // FPS is lowered significantly. But I think it's a worthwhile tradeoff.
        // update the info string in controls
        if (this.lastSelectedID !== undefined) {
            let bodies = this.match.current.bodies.arrays;
            let index = bodies.id.indexOf(this.lastSelectedID)
            if (index === -1) {
                // The body doesn't exist anymore so indexOf returns -1
                this.lastSelectedID = undefined;
            } else {
                let id = bodies.id[index];
                let x = bodies.x[index];
                let y = bodies.y[index];
                let type = bodies.type[index];
                // let influence = bodies.influence[index];
                // let conviction = bodies.conviction[index];
                let hp = bodies.hp[index]; 
                let max_hp = this.meta.types[type].health;
                let dp = this.meta.types[type].damage;
                let bytecodes = bodies.bytecodesUsed[index];
                let level = bodies.level[index];
                let parent = bodies.parent[index];

                let indicatorString = this.match.current.indicatorStrings[id]

                this.controls.setInfoString(id, x, y, hp, max_hp, dp, cst.bodyTypeToString(type), bytecodes, level,
                    parent !== 0 ? parent : undefined, indicatorString);
            }
        }

        if (this.lastSelectedID === undefined) {
            this.controls.removeInfoString();
        }

        this.lastTime = curTime;

        if (this.match.current.turn != this.lastTurn) {
            this.console.setLogsRef(this.match.current.logs, this.match.current.logsShift);
            this.console.seekRound(this.match.current.turn);
            this.lastTurn = this.match.current.turn;
            this.updateStats(this.match.current, this.meta);
        }

        // @ts-ignore
        // renderer.render(this.match.current, this.match.current.minCorner, this.match.current.maxCorner);
        if (this.conf.interpolate &&
            this.match.current.turn + 1 < this.match.deltas.length &&
            this.goalUPS < this.rendersPerSecond.tps) {

            //console.log('interpolating!!');

            this.nextStep.loadNextStep(
                this.match.current,
                this.match.deltas[this.match.current.turn + 1]
            );

            let lerp = Math.min(this.interpGameTime - this.match.current.turn, 1);

            // @ts-ignore
            this.renderer.render(this.match.current, this.match.current.minCorner, this.match.current.maxCorner, curTime, this.nextStep, this.isPaused() ? 0 : lerp);
        } else {
            //console.log('not interpolating!!');
            // interpGameTime might be incorrect if we haven't computed fast enough
            // @ts-ignore
            this.renderer.render(this.match.current, this.match.current.minCorner, this.match.current.maxCorner, curTime);
        }

        if (this.profiler && this.match.profilerFiles.length && !this.loadedProfiler) {
            this.profiler.load(this.match);
            this.loadedProfiler = true;
        }

        //this.updateStats(this.match.current, this.meta);
        this.loopID = window.requestAnimationFrame((curTime) => this.loop.call(this, curTime));
    }

    /**
     * Updates the stats bar displaying VP, bullets, and robot counts for each
     * team in the current game world.
     */
    private updateStats(world: GameWorld, meta: Metadata) {
        let teamIDs: number[] = [];
        let teamNames: string[] = [];
        let totalHP = 0;
        // this.stats.resetECs();
        // for (let i = 0; i < world.bodies.length; i++) {
        //     const type = world.bodies.arrays.type[i];
        //     if (type === schema.BodyType.ENLIGHTENMENT_CENTER) {
        //         this.stats.addEC(world.bodies.arrays.team[i]);
        //     }
        // }

        let teamLead: number[] = [];
        let teamGold: number[] = [];
        for (let team in meta.teams) {
            let teamID = meta.teams[team].teamID;
            let teamStats = world.teamStats.get(teamID) as TeamStats;
            teamIDs.push(teamID);
            teamNames.push(meta.teams[team].name);
            totalHP += teamStats.total_hp.reduce((a,b) => a.concat(b)).reduce((a, b) => a + b);

        }

        for (let team in meta.teams) {
            let teamID = meta.teams[team].teamID;
            let teamStats = world.teamStats.get(teamID) as TeamStats;
            let teamHP = teamStats.total_hp.reduce((a,b) => a.concat(b)).reduce((a, b) => a + b);

            // Update each robot count
            console.log(teamStats);
            this.stats.robots.forEach((type: schema.BodyType) => {
                this.stats.setRobotCount(teamID, type, teamStats.robots[type].reduce((a, b) => a + b)); // TODO: show number of robots per level
                this.stats.setRobotHP(teamID, type, teamStats.total_hp[type].reduce((a,b) => a+b), teamHP); // TODO: differentiate levels, maybe
            });
            /*const hps = world.bodies.arrays.hp;
            const types = world.bodies.arrays.type;
            for(var i = 0; i < hps.length; i++){
                this.stats.setRobotCount(teamID, types[i], hps[i]); // TODO: show number of robots per level
                this.stats.setRobotHP(teamID, types[i], hps[i], teamHP); // TODO: differentiate levels, maybe
            }*/

            // Set votes
            // this.stats.setVotes(teamID, teamStats.votes);
            //### this.stats.setTeamInfluence(teamID, teamHP, totalHP);
            // this.stats.setBuffs(teamID, teamStats.numBuffs);
            // this.stats.setBid(teamID, teamStats.bid);
            this.stats.setIncome(teamID, teamStats.leadChange, teamStats.goldChange, world.turn);
            // this.stats.setIncome(teamID, 3 + teamID, 5 + teamID, world.turn);
        }

        for(var a = 0; a < teamIDs.length; a++){
            //@ts-ignore
            teamLead.push(world.teamStats.get(teamIDs[a]).lead);
            //@ts-ignore
            teamGold.push(world.teamStats.get(teamIDs[a]).gold);
        }
        this.stats.updateBars(teamLead, teamGold);
        this.stats.resetECs();
        const hps = world.bodies.arrays.hp;
        const teams = world.bodies.arrays.team;
        const types = world.bodies.arrays.type;
        for(var i = 0; i < hps.length; i++){
            if(types[i] == ARCHON) this.stats.addEC(teams[i], hps[i]);
        }

        if (this.match.winner && this.match.current.turn == this.match.lastTurn) {
            this.stats.setWinner(this.match.winner, teamNames, teamIDs);
        }
    }

}