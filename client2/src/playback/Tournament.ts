import assert from 'assert'

export default class Tournament {
    public readonly games: Map<number, TournamentGame>
    public readonly gamesByRoundWinnersBracket: Map<number, TournamentGame[]>
    public readonly gamesByRoundLosersBracket: Map<number, TournamentGame[]>
    public readonly winnersBracketRounds: number
    public readonly losersBracketRounds: number

    constructor(tournament_json: string) {
        const tournament_json_parsed = JSON.parse(tournament_json)
        this.games = new Map(
            tournament_json_parsed.map((game: any): [number, TournamentGame] => {
                return [
                    game.id,
                    {
                        id: game.id,
                        teams: game.teams,
                        dependsOn: undefined,
                        winnerIndex: game.winnerIndex,
                        round: game.round,
                        viewed: false,
                        gameFile: game.gameFile
                    }
                ]
            })
        )

        this.gamesByRoundWinnersBracket = new Map<number, TournamentGame[]>()
        this.gamesByRoundLosersBracket = new Map<number, TournamentGame[]>()

        // load heirarchy structure
        for (const game of tournament_json_parsed) {
            const gameObj = this.games.get(game.id) || assert.fail(`Game ${game.id} not found`)
            if (game.dependsOn) {
                for (const id of game.dependsOn) {
                    if (!this.games.has(id)) throw new Error(`Game ${game.id} depends on nonexistent game ${id}`)
                }
                gameObj.dependsOn = game.dependsOn.map((id: number) => this.games.get(id))
            }

            // sort into winners/losers bracket
            const bracket = game.round < 0 ? this.gamesByRoundLosersBracket : this.gamesByRoundWinnersBracket
            const round = Math.abs(game.round)
            bracket.set(round, (bracket.get(round) || []).concat(gameObj))
        }

        // sort games within each round by id
        for (const bracket of [this.gamesByRoundWinnersBracket, this.gamesByRoundLosersBracket]) {
            for (const round of bracket.keys()) {
                bracket.set(
                    round,
                    bracket.get(round)!.sort((a, b) => a.id - b.id)
                )
            }
        }

        //flip rounds to be positive, now that theyve been filtered into winners/losers
        for (const game of this.games.values()) {
            game.round = Math.abs(game.round)
        }

        //normalize loser ids to start at 0
        const minLoserGameId = Math.min(...[...this.gamesByRoundLosersBracket.values()].map((games) => games[0].id))
        for (const round of this.gamesByRoundLosersBracket.keys()) {
            const games = this.gamesByRoundLosersBracket.get(round)!
            for (const game of games) game.id -= minLoserGameId
        }

        this.winnersBracketRounds = Math.max(...this.gamesByRoundWinnersBracket.keys()) || 0
        this.losersBracketRounds = -Math.min(...this.gamesByRoundLosersBracket.keys()) || 0
    }
}

export type TournamentGame = {
    id: number
    teams: [string, string]
    dependsOn?: [TournamentGame, TournamentGame]
    round: number
    winnerIndex: 0 | 1
    viewed: boolean
    gameFile: string
}
