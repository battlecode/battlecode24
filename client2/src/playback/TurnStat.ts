import { schema } from 'battlecode-schema'
import Game, { Team } from './Game'
import assert from 'assert'
import Turn from './Turn'

class TeamTurnStat {
    robots: number[] = Array(6).fill(0)
    total_hp: number[] = Array(6).fill(0)
    adamantium: number = 0
    mana: number = 0
    elixir: number = 0
    adamantiumChange: number = 0
    manaChange: number = 0
    elixirChange: number = 0
    adamantiumMined: number = 0
    manaMined: number = 0
    elixirMined: number = 0
    adamantiumIncomeAverageDatapoint: number | undefined = undefined
    manaIncomeAverageDatapoint: number | undefined = undefined
    elixirIncomeAverageDatapoint: number | undefined = undefined

    copy(): TeamTurnStat {
        const newStat = Object.assign(Object.create(Object.getPrototypeOf(this)), this)
        // manually copy internal objects
        newStat.robots = [...this.robots]
        newStat.total_hp = [...this.total_hp]
        return newStat
    }
}

export default class TurnStat {
    private readonly teams: Map<Team, TeamTurnStat>
    private readonly game: Game
    public completed: boolean = false

    constructor(game: Game, teams?: Map<Team, TeamTurnStat>) {
        this.game = game
        this.teams =
            teams ??
            new Map([
                [game.teams[0], new TeamTurnStat()],
                [game.teams[1], new TeamTurnStat()]
            ])
    }

    copy(): TurnStat {
        const newTeamStats = new Map(this.teams)
        for (const [team, stat] of this.teams) newTeamStats.set(team, stat.copy())
        const copy = new TurnStat(this.game, newTeamStats)
        copy.completed = this.completed
        return copy
    }

    /**
     * Mutates this stat to reflect the given delta.
     */
    applyDelta(turn: Turn, delta: schema.Round): void {
        assert(!this.completed, 'Cannot apply delta to completed turn')
        assert(turn.turnNumber === delta.roundID(), `Wrong turn ID: is ${delta.roundID()}, should be ${turn.turnNumber}`)
        
        for (var i = 0; i < delta.teamIDsLength(); i++) {
            const team = this.game.teams[(delta.teamIDs(i) ?? assert.fail('teamID not found in round')) - 1]
            assert(team != undefined, `team ${i} not found in game.teams in turn`)
            const teamStat = this.teams.get(team) ?? assert.fail(`team ${i} not found in team stats in turn`)

            teamStat.adamantium += delta.teamAdChanges(i) ?? assert.fail('teamAdChanges not found in round')
            teamStat.mana += delta.teamMnChanges(i) ?? assert.fail('teamMnChanges not found in round')
            teamStat.elixir += delta.teamExChanges(i) ?? assert.fail('teamExChanges not found in round')

            teamStat.adamantiumChange = delta.teamAdChanges(i) ?? assert.fail('teamAdChanges not found in round')
            teamStat.manaChange = delta.teamMnChanges(i) ?? assert.fail('teamMnChanges not found in round')
            teamStat.elixirChange = delta.teamExChanges(i) ?? assert.fail('teamExChanges not found in round')

            teamStat.adamantiumMined = 0
            teamStat.manaMined = 0
            teamStat.elixirMined = 0
        }

        const time = Date.now()
        const average = (array: number[]) => (array.length > 0 ? array.reduce((a, b) => a + b) / array.length : 0)
        for (const team of this.game.teams) {
            if (turn.turnNumber % 10 == 0) {
                const teamStat = this.teams.get(team) ?? assert.fail(`team ${team} not found in team stats in turn`)
                const adamantiumMinedHist = [teamStat.adamantiumMined]
                const manaMinedHist = [teamStat.manaMined]
                const elixirMinedHist = [teamStat.elixirMined]
                for (let i = turn.turnNumber - 1; i >= Math.max(0, turn.turnNumber - 100); i--) {
                    const prevTurnStat = turn.match.stats[i].getTeamStat(team)
                    adamantiumMinedHist.push(prevTurnStat.adamantiumMined)
                    manaMinedHist.push(prevTurnStat.manaMined)
                    elixirMinedHist.push(prevTurnStat.elixirMined)
                }

                teamStat.adamantiumIncomeAverageDatapoint = average(adamantiumMinedHist)
                teamStat.manaIncomeAverageDatapoint = average(manaMinedHist)
                teamStat.elixirIncomeAverageDatapoint = average(elixirMinedHist)
            }
        }
        const timems = Date.now() - time
        if (timems > 1) console.log(`took ${timems}ms to calculate income averages`)

        this.completed = true
    }

    public getTeamStat(team: Team): TeamTurnStat {
        return this.teams.get(team) ?? assert.fail(`team ${team} not found in team stats in turn`)
    }
}
