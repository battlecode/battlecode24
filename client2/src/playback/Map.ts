export class CurrentMap {
    constructor(
        public readonly staticMap: StaticMap
    ) { }

    copy(): CurrentMap {
        const newMap = new CurrentMap(this.staticMap);
        //TODO actually copy
        return newMap;
    }

    /**
     * Mutates this currentMap to reflect the given delta.
     */
    applyDelta(SchemaDelta: any): void {
    }
}

export class StaticMap {
    constructor(
        public readonly width: number,
        public readonly height: number,
    ) { }
}