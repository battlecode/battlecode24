import Actions from './Actions'
import Bodies from './Bodies'
import { Team } from './Game'
import { CurrentMap } from './Map'
import Match from './Match'
import TurnStat from './TurnStat'

export default class Turn {
    get teams(): Team[] {
        return this.match.game.teams
    }
    constructor(
        public readonly match: Match,
        public turnNumber: number = 0,
        public map: CurrentMap,
        public bodies: Bodies,
        public actions: Actions,
        public stat: TurnStat
    ) {}

    /**
     * Mutates this turn to reflect the given delta.
     */
    public applyDelta(schemaDelta: any): void {
        this.turnNumber += 1

        const firstTimeComputingStat = this.match.stats.length <= this.turnNumber
        if (firstTimeComputingStat)
            this.stat.completed = false // mark that stat should be computed by bodies and actions below
        else this.stat = this.match.stats[this.turnNumber].copy()

        this.map.applyDelta(schemaDelta)
        this.bodies.applyDelta(this, schemaDelta, () => {
            this.actions.applyDelta(this, schemaDelta)
        })

        if (firstTimeComputingStat) {
            // finish computing stat and save to match
            this.stat.applyDelta(this, schemaDelta)
            this.match.stats[this.turnNumber] = this.stat.copy()
        }
    }

    public copy(): Turn {
        return new Turn(
            this.match,
            this.turnNumber,
            this.map.copy(),
            this.bodies.copy(),
            this.actions.copy(),
            this.stat.copy()
        )
    }
}
