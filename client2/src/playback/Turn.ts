import Actions from './Actions';
import Bodies from './Bodies';
import { CurrentMap } from './Map';
import Match from './Match';
import TurnStat from './TurnStat';

export default class Turn {
    constructor(
        private readonly parent: Match,
        public turnNumber: number = 0,
        public map: CurrentMap,
        public bodies: Bodies,
        public actions: Actions,
        public stat: TurnStat,
    ) { }

    /**
     * Mutates this turn to reflect the given delta.
     */
    public applyDelta(SchemaDelta: any): void {
        this.turnNumber += 1;
        
        // stat should be done first so it can see info about the bodies that have died
        if (this.parent.stats.length > this.turnNumber) {
            this.stat = this.parent.stats[this.turnNumber].copy();
        } else {
            this.stat.applyDelta(this, SchemaDelta);
            this.parent.stats[this.turnNumber] = this.stat.copy();
        }

        this.map.applyDelta(SchemaDelta);
        this.bodies.applyDelta(SchemaDelta);
        this.actions.applyDelta(SchemaDelta);
    }

    public copy(): Turn {
        return new Turn(this.parent, this.turnNumber, this.map.copy(), this.bodies.copy(), this.actions.copy(), this.stat.copy());
    }
}