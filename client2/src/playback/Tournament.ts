import assert from 'assert'

export default class Tournament {
    private readonly games: TournamentGame[]
    private readonly gamesByTeamID: Map<number, TournamentGame[]>
    public readonly winnersBracketRoot: TournamentGame
    public readonly losersBracketRoot: TournamentGame | undefined

    constructor(raw_games: JsonTournamentGame[]) {
        let nextID = -1
        this.games = raw_games
            .filter((game) => game.tournament_round.release_status === 2)
            .map((game) => {
                const team0 = game.participants.find((p) => p.player_index === 0)
                const team1 = game.participants.find((p) => p.player_index === 1)
                assert(team0 && team1, 'Missing team in round')
                assert(team0.score != team1.score, 'Tie games not supported')
                const winnerIndex = team0.score > team1.score ? 0 : 1
                nextID++
                return {
                    id: nextID,
                    teams: [team0.teamname, team1.teamname],
                    teamIDs: [team0.team, team1.team],
                    dependsOn: [undefined, undefined],
                    winnerIndex: winnerIndex,
                    round: game.tournament_round.external_id,
                    viewed: true,
                    gameFile: game.replay_url
                }
            })

        this.gamesByTeamID = new Map()
        for (const game of this.games.values()) {
            for (const teamID of game.teamIDs) {
                if (!this.gamesByTeamID.has(teamID)) this.gamesByTeamID.set(teamID, [])
                this.gamesByTeamID.get(teamID)!.push(game)
            }
        }

        const maxRound = Math.max(...this.games.map((g) => g.round))
        const maxRoundGames = this.games.filter((g) => g.round === maxRound)
        assert(maxRoundGames.length === 1, 'Multiple games in final round')
        this.winnersBracketRoot = maxRoundGames[0]
        this.setGameDependents(this.winnersBracketRoot)

        const minRound = Math.min(...this.games.map((g) => g.round))
        if (minRound < 0) {
            const minRoundGames = this.games.filter((g) => g.round === minRound)
            assert(minRoundGames.length === 1, 'Multiple games in final losers round')
            this.losersBracketRoot = minRoundGames[0]
            this.setGameDependents(this.losersBracketRoot)
        }

        console.log(this)
    }

    getTeamGameInRound(teamID: number, round: number): TournamentGame | undefined {
        const games = this.gamesByTeamID.get(teamID)!.filter((g) => g.round === round)
        assert(games.length === 1 || games.length === 0, 'Multiple games in round')
        return games.length === 1 ? games[0] : undefined
    }

    setGameDependents(game: TournamentGame) {
        assert(game.round != 0, '0th round shouldnt exist')

        const dependentRound = game.round > 0 ? game.round - 1 : game.round + 1
        game.dependsOn = [
            this.getTeamGameInRound(game.teamIDs[0], dependentRound),
            this.getTeamGameInRound(game.teamIDs[1], dependentRound)
        ]

        if (game.dependsOn[0]) this.setGameDependents(game.dependsOn[0])
        if (game.dependsOn[1]) this.setGameDependents(game.dependsOn[1])
    }
}

export type TournamentGame = {
    id: number
    teams: [string, string]
    teamIDs: [number, number]
    dependsOn: [TournamentGame | undefined, TournamentGame | undefined]
    round: number
    winnerIndex: 0 | 1
    viewed: boolean
    gameFile: string
}

export type JsonTournamentGame = {
    tournament_round: {
        external_id: number
        tournament: string
        name: string
        release_status: number
    }
    participants: {
        team: number
        teamname: string
        player_index: number
        score: number
    }[]
    replay_url: string
}
