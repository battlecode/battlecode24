import assert from 'assert'
import { max } from 'd3'

export default class Tournament {
    public readonly games: Map<number, TournamentGame>
    public readonly gamesByRound: Map<number, TournamentGame[]>
    public readonly rounds: number

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

        this.gamesByRound = new Map<number, TournamentGame[]>()

        // load heirarchy structure
        for (const game of tournament_json_parsed) {
            const gameObj = this.games.get(game.id) || assert.fail(`Game ${game.id} not found`)
            if (game.dependsOn) {
                for (const id of game.dependsOn) {
                    if (!this.games.has(id)) throw new Error(`Game ${game.id} depends on nonexistent game ${id}`)
                }
                gameObj.dependsOn = game.dependsOn.map((id: number) => this.games.get(id))
            }
            this.gamesByRound.set(game.round, (this.gamesByRound.get(game.round) || []).concat(gameObj))
        }

        // sort games within each round by id
        for (const round of this.gamesByRound.keys()) {
            this.gamesByRound.set(
                round,
                this.gamesByRound.get(round)!.sort((a, b) => a.id - b.id)
            )
        }

        this.rounds = max(this.gamesByRound.keys()) || 0
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
