export default class Tournament {
    public readonly games: Map<number, TournamentGame>

    constructor(tournament_json: string) {
        const tournament_json_parsed = JSON.parse(tournament_json)
        this.games = new Map(
            tournament_json_parsed.map((game: any) => {
                return [
                    game.id,
                    {
                        teams: game.teams,
                        dependsOn: null,
                        winnerIndex: game.winnerIndex,
                        round: game.round,
                        viewed: false,
                        gameFile: game.gameFile
                    }
                ]
            })
        )

        // load heirarchy structure
        for (const game of tournament_json_parsed) {
            if (game.dependsOn) {
                for (const id of game.dependsOn) {
                    if (!this.games.has(id)) {
                        throw new Error(`Game ${game.id} depends on nonexistent game ${id}`)
                    }
                }
                this.games.get(game.id)!.dependsOn = game.dependsOn.map((id: number) => this.games.get(id))
            }
        }
    }
}

export type TournamentGame = {
    id: number
    teams: [string, string]
    dependsOn?: [TournamentGame, TournamentGame]
    winner: 0 | 1
    viewed: boolean
    gameFile: string
}
