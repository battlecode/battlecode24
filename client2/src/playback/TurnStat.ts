import { schema } from 'battlecode-schema';
import Game, { Team } from './Game';
import assert from 'assert';
import Turn from './Turn';

class TeamTurnStat {
    robots: number[] = Array(6).fill(0);
    total_hp: number[] = Array(6).fill(0);
    adamantium: number = 0;
    mana: number = 0;
    elixir: number = 0;
    adamantiumChange: number = 0;
    manaChange: number = 0;
    elixirChange: number = 0;
    adamantiumMined: number = 0;
    manaMined: number = 0;
    elixirMined: number = 0;
    // x: turn, y: value
    adamantiumIncomeDataset: { x: number, y: number; }[] = [];
    manaIncomeDataset: { x: number, y: number; }[] = [];
    elixirIncomeDataset: { x: number, y: number; }[] = [];
    adamantiumMinedHist: number[] = [];
    manaMinedHist: number[] = [];
    elixirMinedHist: number[] = [];

    copy(): TeamTurnStat {
        return JSON.parse(JSON.stringify(this));
    }
};

export default class TurnStat {
    private readonly teams: Map<Team, TeamTurnStat>;
    private readonly game: Game;
    constructor(game: Game, teams?: Map<Team, TeamTurnStat>) {
        this.game = game;
        this.teams = teams ?? new Map([
            [game.teams[0], new TeamTurnStat()],
            [game.teams[1], new TeamTurnStat()]
        ]);
    }

    copy(): TurnStat {
        const newTeamStats = new Map(this.teams);
        for (const [team, stat] of this.teams)
            newTeamStats.set(team, stat.copy());
        return new TurnStat(this.game, newTeamStats);
    }

    /**
     * Mutates this stat to reflect the given delta.
     */
    applyDelta(turn: Turn, delta: schema.Round): void {
        const bodies = delta.spawnedBodies(this.game._bodiesSlot) ?? assert.fail('spawnedBodies not found in round');
        const teams = bodies.teamIDsArray() ?? assert.fail('teamIDsArray not found in spawnedBodies');
        const types = bodies.typesArray() ?? assert.fail('typesArray not found in spawnedBodies');

        for (let i = 0; i < bodies.robotIDsLength(); i++) {
            const teamStat = this.teams.get(this.game.teams[teams[i]]) ?? assert.fail(`team ${i} not found in team stats in turn`);
            const robotType: schema.BodyType = types[i];
            teamStat.robots[robotType] += 1;
            teamStat.total_hp[robotType] += this.game.typeMetadata[robotType].health(); // TODO: extract meta info
        }

        const diedIDs = delta.diedIDsArray() ?? assert.fail('diedIDsArray not found in round');
        if (diedIDs.length > 0) {
            for (let i = 0; i < diedIDs.length; i++) {
                const body = turn.bodies.getById(diedIDs[i]) ?? assert.fail(`died body ${i} not found in bodies in turn`);
                const teamStat = this.teams.get(body.team) ?? assert.fail(`team ${i} not found in team stats in turn`);
                teamStat.robots[body.type] -= 1;
                teamStat.total_hp[body.type] -= body.hp;
            }
        }


        for (var i = 0; i < delta.teamIDsLength(); i++) {
            const team = this.game.teams[delta.teamIDs(i) ?? assert.fail('teamID not found in round')];
            const teamStat = this.teams.get(team) ?? assert.fail(`team ${i} not found in team stats in turn`);

            teamStat.adamantium += delta.teamAdChanges(i) ?? assert.fail('teamAdChanges not found in round');
            teamStat.mana += delta.teamMnChanges(i) ?? assert.fail('teamMnChanges not found in round');
            teamStat.elixir += delta.teamExChanges(i) ?? assert.fail('teamExChanges not found in round');

            teamStat.adamantiumChange = delta.teamAdChanges(i) ?? assert.fail('teamAdChanges not found in round');
            teamStat.manaChange = delta.teamMnChanges(i) ?? assert.fail('teamMnChanges not found in round');
            teamStat.elixirChange = delta.teamExChanges(i) ?? assert.fail('teamExChanges not found in round');

            teamStat.adamantiumMined = 0;
            teamStat.manaMined = 0;
            teamStat.elixirMined = 0;
        }

        const actions = delta.actionsArray() ?? assert.fail('actionsArray not found in round');
        for (let i = 0; i < actions.length; i++) {
            issue here is getting the team of an action requires getting the body associated with it - this shouldnt be spread around in this way
          case schema.Action.CHANGE_ADAMANTIUM:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
            teamStatsObj.adamantiumMined += target;
        this.bodies.alter({ id: robotID, adamantium: body.adamantium + target });
        break

          case schema.Action.CHANGE_ELIXIR:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
            teamStatsObj.elixirMined += target;
        this.bodies.alter({ id: robotID, elixir: body.elixir + target });
        break

          case schema.Action.CHANGE_MANA:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
            teamStatsObj.manaMined += target;
        this.bodies.alter({ id: robotID, mana: body.mana + target });
        break;

        const averageWindow = 100;
        const average = (array: number[]) => array.length > 0 ? array.reduce((a, b) => a + b) / array.length : 0;
        for (const team of this.game.teams) {
            const teamStat = this.teams.get(team) ?? assert.fail(`team ${team} not found in team stats in turn`);
            teamStat.adamantiumMinedHist.push(teamStat.adamantiumMined);
            teamStat.manaMinedHist.push(teamStat.manaMined);
            teamStat.elixirMinedHist.push(teamStat.elixirMined);

            if (teamStat.adamantiumMinedHist.length > averageWindow) teamStat.adamantiumMinedHist.shift();
            if (teamStat.manaMinedHist.length > averageWindow) teamStat.manaMinedHist.shift();
            if (teamStat.elixirMinedHist.length > averageWindow) teamStat.elixirMinedHist.shift();

            if (turn.turnNumber % 10 == 0) {
                teamStat.adamantiumIncomeDataset.push({ x: turn.turnNumber, y: average(teamStat.adamantiumMinedHist) });
                teamStat.manaIncomeDataset.push({ x: turn.turnNumber, y: average(teamStat.manaMinedHist) });
                teamStat.elixirIncomeDataset.push({ x: turn.turnNumber, y: average(teamStat.elixirMinedHist) });
            }
        }
    }
}