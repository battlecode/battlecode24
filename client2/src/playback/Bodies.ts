import { schema } from 'battlecode-schema';
import assert from 'assert';
import Game, { Team } from './Game';

export default class Bodies {
    private bodies: Map<number, Body> = new Map();
    constructor(
        private readonly game: Game,
        initialBodies?: schema.SpawnedBodyTable
    ) {
        if (initialBodies)
            this.insertBodies(initialBodies);
    }

    applyDelta(delta: schema.Round): void {
        const bodies = delta.spawnedBodies(this.game._bodiesSlot);
        if (bodies) this.insertBodies(bodies);

        const movedLocs = delta.movedLocs(this.game._vecTableSlot1);
        if (movedLocs) {
            const movedIds = delta.movedIDsArray() ?? assert.fail('movedIDsArray not found in round');
            const xsArray = movedLocs.xsArray() ?? assert.fail('movedLocs.xsArray not found in round');
            const ysArray = movedLocs.ysArray() ?? assert.fail('movedLocs.ysArray not found in round');
            for (let i = 0; i < movedIds.length; i++) {
                const body = this.bodies.get(movedIds[i]) ?? assert.fail('Moved body not found in bodies');
                body.moveTo(xsArray[i], ysArray[i]);
            }
        }

        if (delta.diedIDsLength() > 0) {
            for (let i = 0; i < delta.diedIDsLength(); i++) {
                this.bodies.delete(delta.diedIDs(i)!);
            }
        }
    }

    insertBodies(bodies: schema.SpawnedBodyTable): void {
        var teams = bodies.teamIDsArray() ?? assert.fail('Initial body teams not found in header');
        var types = bodies.typesArray() ?? assert.fail('Initial body types not found in header');

        const locs = bodies.locs(this.game._vecTableSlot1) ?? assert.fail('Initial body locations not found in header');
        const xsArray = locs.xsArray() ?? assert.fail('Initial body x locations not found in header');
        const ysArray = locs.ysArray() ?? assert.fail('Initial body y locations not found in header');
        const idsArray = bodies.robotIDsArray() ?? assert.fail('Initial body IDs not found in header');

        for (let i = 0; i < idsArray.length; i++) {
            const bodyClass = BODY_DEFINITIONS[types[i]] ?? assert.fail(`Body type ${types[i]} not found in BODY_DEFINITIONS`);
            this.bodies.set(idsArray[i], new bodyClass(
                xsArray[i],
                ysArray[i],
                this.game.typeMetadata[types[i]].health(),
                this.game.teams[teams[i]],
                idsArray[i],
            ));
        }
    }

    getById(id: number): Body {
        return this.bodies.get(id) ?? assert.fail(`Body with id ${id} not found in bodies`);
    }

    copy(): Bodies {
        const newBodies = new Bodies(this.game);
        newBodies.bodies = new Map(this.bodies);
        for (const body of this.bodies.values())
            newBodies.bodies.set(body.id, body.copy());

        return newBodies;
    }
}

export class Body {
    static robotName: string;
    public type: number = -1;
    constructor(
        public x: number,
        public y: number,
        public hp: number,
        public readonly team: Team,
        public readonly id: number,
        public adamantium: number = 0,
        public elixir: number = 0,
        public mana: number = 0,
        public anchor: number = 0,
        public bytecodesUsed: number = 0,
    ) { }

    public draw(ctx: CanvasRenderingContext2D): void {
        throw new Error('Method not implemented. Instances of Body should not be constructed');
    };
    public onHoverInfo(): string {
        throw new Error('Method not implemented. Instances of Body should not be constructed');
    };
    public copy(): Body {
        throw new Error('Method not implemented. Instances of Body should not be constructed');
    };

    public moveTo(x: number, y: number): void {
        this.x = x;
        this.y = y;
    }

    public clearResources(): void {
        this.adamantium = 0;
        this.elixir = 0;
        this.mana = 0;
        this.anchor = 0;
    }
};

export class Archon extends Body {
    static robotName = 'Archon';
    public type = schema.BodyType.HEADQUARTERS;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return 'Archon';
    }
    copy(): Archon {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export class Launcher extends Body {
    static robotName = 'Launcher';
    public type = schema.BodyType.LAUNCHER;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return Launcher.robotName;
    }
    copy(): Launcher {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export class Carrier extends Body {
    static robotName = 'Carrier';
    public type = schema.BodyType.CARRIER;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return 'Carrier';
    }
    copy(): Carrier {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export class Booster extends Body {
    static robotName = 'Booster';
    public type = schema.BodyType.BOOSTER;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return Booster.robotName;
    }
    copy(): Booster {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export class Destabilizer extends Body {
    static robotName = 'Destabilizer';
    public type = schema.BodyType.DESTABILIZER;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return Destabilizer.robotName;
    }
    copy(): Destabilizer {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export class Amplifier extends Body {
    static robotName = 'Amplifier';
    public type = schema.BodyType.AMPLIFIER;
    constructor(x: number, y: number, hp: number, team: Team, id: number) {
        super(x, y, hp, team, id);
    }
    draw(ctx: CanvasRenderingContext2D): void {
    }
    onHoverInfo(): string {
        return Amplifier.robotName;
    }
    copy(): Amplifier {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

export const BODY_DEFINITIONS: Record<number, typeof Body> = {};
BODY_DEFINITIONS[schema.BodyType.HEADQUARTERS] = Archon;
BODY_DEFINITIONS[schema.BodyType.LAUNCHER] = Launcher;
BODY_DEFINITIONS[schema.BodyType.CARRIER] = Carrier;
BODY_DEFINITIONS[schema.BodyType.BOOSTER] = Booster;
BODY_DEFINITIONS[schema.BodyType.DESTABILIZER] = Destabilizer;
BODY_DEFINITIONS[schema.BodyType.AMPLIFIER] = Amplifier;
