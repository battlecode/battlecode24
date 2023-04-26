import { schema } from 'battlecode-schema';
import assert from 'assert';

type ResourceWellStat = {
    adamantium: number;
    mana: number;
    elixir: number;
    upgraded: boolean;
};

type IslandStat = {
    owner: number;
    flip_progress: number;
    locations: number[];
    is_accelerated: boolean;
    id: number;
};

export class CurrentMap {
    public readonly staticMap: StaticMap;
    public readonly resource_well_stats: Map<number, ResourceWellStat>;
    public readonly island_stats: Map<number, IslandStat>;
    public readonly resources: Int8Array;
    get width(): number { return this.staticMap.width; }
    get height(): number { return this.staticMap.height; }

    constructor(from: StaticMap | CurrentMap) {
        if (from instanceof StaticMap) {
            // create current map from static map
            this.staticMap = from;
            this.resources = from._resources;
            this.resource_well_stats = new Map();
            for (let i = 0; i < this.resources.length; i++) {
                if (this.resources[i] != 0) {
                    this.resource_well_stats.set(i, { adamantium: 0, mana: 0, elixir: 0, upgraded: false });
                }
            }
            this.island_stats = new Map();
            for (let i = 0; i < this.staticMap.islands.length; i++) {
                if (this.staticMap.islands[i] != 0) {
                    let island_id = this.staticMap.islands[i];
                    if (this.island_stats.has(island_id)) {
                        this.island_stats.get(island_id)!.locations.push(i);
                    } else {
                        this.island_stats.set(island_id, { owner: 0, flip_progress: 0, locations: [i], is_accelerated: false, id: island_id });
                    }
                }
            }
        } else {
            // create current map from current map (copy)
            this.resource_well_stats = new Map(from.resource_well_stats);
            for (let [key, value] of this.resource_well_stats)
                this.resource_well_stats.set(key, { ...value });

            this.island_stats = new Map(from.island_stats);
            for (let [key, value] of this.island_stats)
                this.island_stats.set(key, { ...value });

            this.staticMap = from.staticMap;
            this.resources = new Int8Array(from.resources);
        }
    }

    indexToLocation(index: number): { x: number, y: number; } {
        const target_x = index % this.width;
        const target_y = (index - target_x) / this.width;
        assert(target_x >= 0 && target_x < this.width, `target_x ${target_x} out of bounds`);
        assert(target_y >= 0 && target_y < this.height, `target_y ${target_y} out of bounds`);
        return { x: target_x, y: target_y };
    }


    copy(): CurrentMap {
        return new CurrentMap(this);
    }

    /**
     * Mutates this currentMap to reflect the given delta.
     */
    applyDelta(delta: schema.Round): void {
        for (let i = 0; i < delta.resourceWellLocsLength(); i++) {
            const well_index = delta.resourceWellLocs(i) ?? assert.fail(`resource well loc at ${i} not found`);
            this.resources[well_index] = delta.resourceID(i) ?? assert.fail(`resource id at ${i} not found`);

            const current_resource_stats = this.resource_well_stats.get(well_index) ?? assert.fail(`resource well stats at ${well_index} not found`);
            current_resource_stats.adamantium = delta.wellAdamantiumValues(i) ?? assert.fail(`resource adamantium at ${i} not found`);
            current_resource_stats.mana = delta.wellManaValues(i) ?? assert.fail(`resource mana at ${i} not found`);
            current_resource_stats.elixir = delta.wellElixirValues(i) ?? assert.fail(`resource elixir at ${i} not found`);
            current_resource_stats.upgraded = (delta.wellAccelerationID(i) ?? assert.fail(`resource acceleration id at ${i} not found`)) > 0;
        }

        for (let i = 0; i < delta.islandIDsLength(); i++) {
            const id = delta.islandIDs(i) ?? assert.fail(`island id at ${i} not found`);
            const owner = delta.islandOwnership(i) ?? assert.fail(`island ownership at ${i} not found`);
            const turnover = delta.islandTurnoverTurns(i) ?? assert.fail(`island turnover turns at ${i} not found`);
            const island_stats = this.island_stats.get(id) ?? assert.fail(`island stats at ${id} not found`);
            island_stats.flip_progress = turnover;
            if (island_stats.owner != owner) {
                island_stats.is_accelerated = false;
            }
            island_stats.owner = owner;
        }
    };
}

export class StaticMap {
    public readonly width: number;
    public readonly height: number;
    public readonly name: string;
    public readonly randomSeed: number;
    public readonly symmetry: number;

    public readonly walls: Int8Array;
    public readonly clouds: Int8Array;
    public readonly currents: Int8Array;
    public readonly _resources: Int8Array;
    public readonly islands: Int32Array;

    constructor(map: schema.GameMap) {
        this.name = map.name() as string;
        this.randomSeed = map.randomSeed();
        this.symmetry = map.symmetry();
        const minCorner = map.minCorner() ?? assert.fail('minCorner() is null');
        const maxCorner = map.maxCorner() ?? assert.fail('maxCorner() is null');
        this.width = (maxCorner.x() - minCorner.x());
        this.height = (maxCorner.y() - minCorner.y());

        this.walls = map.wallsArray() ?? assert.fail('wallsArray() is null');
        this.clouds = map.cloudsArray() ?? assert.fail('cloudsArray() is null');
        this.currents = Int8Array.from(map.currentsArray() ?? assert.fail('currentsArray() is null'));
        this._resources = Int8Array.from(map.resourcesArray() ?? assert.fail('resourcesArray() is null'));
        this.islands = map.islandsArray() ?? assert.fail('islandsArray() is null');
    }
}