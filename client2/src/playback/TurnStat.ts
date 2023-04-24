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

        // Update spawn stats
        for (let i = 0; i < initialBodies.robotIDsLength(); i++) {
            var statObj = this.teamStats.get(teams[i]);
            statObj.robots[types[i]] += 1;
            statObj.total_hp[types[i]] += this.meta.types[types[i]].health; // TODO: extract meta info
            this.teamStats.set(teams[i], statObj);
            hps[i] = this.meta.types[types[i]].health;
        }
    }
}