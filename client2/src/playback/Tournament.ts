export default class Tournament {
    public readonly games: Map<number, TournamentGame>

    constructor(tournament_json: string) {
        const tournament_json_parsed = JSON.parse(tournament_json)
        this.games = new Map(
            tournament_json_parsed.games.map((game: any) => {
                return [
                    game.id,
                    {
                        teams: game.teams,
                        dependsOn: null,
                        winner: game.winner,
                        viewed: false,
                        gameFile: game.gameFile
                    }
                ]
            })
        )

        // load heirarchy structure
        for (const game of tournament_json_parsed.games) {
            if (game.dependsOn) {
                this.games.get(game.id)!.dependsOn = this.games.get(game.dependsOn)
            }
        }
    }
}

type TournamentGame = {
    id: number
    teams: [string, string]
    dependsOn?: TournamentGame
    winner: 0 | 1
    viewed: boolean
    gameFile: string
}
