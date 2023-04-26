import Actions from './Actions';
import Bodies from './Bodies';
import { Team } from './Game';
import { CurrentMap } from './Map';
import Match from './Match';
import TurnStat from './TurnStat';

export default class Turn {
    get teams(): Team[] { return this.match.game.teams; }
    constructor(
        private readonly match: Match,
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
        
        the order of these events is really messy, and should be figured out
        its also messy how actions loop up bodies to then go change stats(why is it not more seperated);

        // stat should be done first so it can see info about the bodies that have died
        if (this.match.stats.length > this.turnNumber) {
            this.stat = this.match.stats[this.turnNumber].copy();
            this.map.applyDelta(SchemaDelta);
            this.bodies.applyDelta(SchemaDelta);
            this.actions.applyDelta(this, SchemaDelta);
        } else {
            this.stat.applyDelta(this, SchemaDelta);
            this.map.applyDelta(SchemaDelta);
            this.bodies.applyDelta(SchemaDelta);
            this.actions.applyDelta(this, SchemaDelta, true);
            this.match.stats[this.turnNumber] = this.stat.copy();
        }
    }

    public copy(): Turn {
        return new Turn(this.match, this.turnNumber, this.map.copy(), this.bodies.copy(), this.actions.copy(), this.stat.copy());
    }
}