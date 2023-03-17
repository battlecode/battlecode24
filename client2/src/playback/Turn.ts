import Bodies from './Bodies';
import { CurrentMap } from './Map';
import Round from './Round';
import TurnStat from './TurnStat';

export default class Turn {

    constructor(
        private readonly parent: Round,
        public turnNumber: number = 0,
        public map: CurrentMap,
        public bodies: Bodies,
        // public actions: Action[],
        public stat: TurnStat,
    ) { }

    /**
     * Mutates this turn to reflect the given delta.
     */
    public applyDelta(SchemaDelta: any): void {
        this.map.applyDelta(SchemaDelta);
        this.turnNumber += 1;

        if (this.parent.stats.length > this.turnNumber) {
            this.stat = this.parent.stats[this.turnNumber].copy();
        } else {
            this.stat.applyDelta(SchemaDelta);
            this.parent.stats[this.turnNumber] = this.stat.copy();
        }
    }

    public copy(): Turn {
        return new Turn(this.parent, this.turnNumber, this.map.copy(), this.bodies.copy(), this.actions.copy(), this.stat.copy());
    }
}