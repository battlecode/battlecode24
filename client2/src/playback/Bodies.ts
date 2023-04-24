import { schema } from 'battlecode-schema';
import assert from 'assert';

export default class Bodies {
    bodies: Map<number, Body> = new Map();

    constructor(initialBodies: schema.SpawnedBodyTable) {
        var teams = initialBodies.teamIDsArray();
        var types = initialBodies.typesArray();
        var hps = new Int32Array(bodies.robotIDsLength());

        // Update spawn stats
        for (let i = 0; i < initialBodies.robotIDsLength(); i++) {
            var statObj = this.teamStats.get(teams[i]);
            statObj.robots[types[i]] += 1;
            statObj.total_hp[types[i]] += this.meta.types[types[i]].health; // TODO: extract meta info
            this.teamStats.set(teams[i], statObj);
            hps[i] = this.meta.types[types[i]].health;
        }

        const locs = bodies.locs(this._vecTableSlot1);
        // Insert bodies
        const idsArray = bodies.robotIDsArray();
        const xsArray = locs.xsArray();
        const ysArray = locs.ysArray();
        this.bodies.insertBulk({
            id: idsArray,
            team: teams,
            type: types,
            x: xsArray,
            y: ysArray,
            bytecodesUsed: new Int32Array(bodies.robotIDsLength()),
            action: (new Int8Array(bodies.robotIDsLength())).fill(-1),
            target: new Int32Array(bodies.robotIDsLength()),
            targetx: new Int32Array(bodies.robotIDsLength()),
            targety: new Int32Array(bodies.robotIDsLength()),
            parent: new Int32Array(bodies.robotIDsLength()),
            hp: hps,
            adamantium: new Int32Array(bodies.robotIDsLength()),
            elixir: new Int32Array(bodies.robotIDsLength()),
            mana: new Int32Array(bodies.robotIDsLength()),
            anchor: new Int8Array(bodies.robotIDsLength()),
        });
    }

    applyDelta(delta: schema.Round): void {
        if (delta.diedIDsLength() > 0) {
            for (let i = 0; i < delta.diedIDsLength(); i++) {
                this.bodies.delete(delta.diedIDs(i) ?? assert.fail('diedIDs(i) is null'));
            }
        }
    }
    copy(): Bodies {
        throw new Error('Method not implemented.');
    }
}

export abstract class Body {
    static robotName: string;
    abstract draw(ctx: CanvasRenderingContext2D): void;
    abstract onHoverInfo(): string;
    abstract copy(): Body;
};

export const BODY_DEFINITIONS: Record<number, typeof Body> = {
    1: class Archon implements Body {
        static robotName = 'Archon';
        constructor() {
        }
        draw(ctx: CanvasRenderingContext2D): void {
        }
        onHoverInfo(): string {
            return 'Archon';
        }
        copy(): Archon {
            return { ...this }; // if you store data more than one level deep, you'll need to copy it too
        }
    },
    2: class Launcher implements Body {
        static robotName = 'Launcher';
        constructor() {
        }
        draw(ctx: CanvasRenderingContext2D): void {
        }
        onHoverInfo(): string {
            return 'Launcher';
        }
        copy(): Launcher {
            return { ...this }; // if you store data more than one level deep, you'll need to copy it too
        }
    }
}; 