export default class TurnStat {
    constructor(
        public team1gold: number = 0,
    ) { }

    copy(): TurnStat {
        const newStat = new TurnStat(this.team1gold);
        return newStat;
    }

    /**
     * Mutates this stat to reflect the given delta.
     */
    applyDelta(SchemaDelta: any): void {
        this.team1gold += 1;
    }
}