export default class Bodies {
    bodies: Map<number, Body> = new Map();
    applyDelta(delta: any): void {
        if (delta.diedIDsLength() > 0) {
            for (let i = 0; i < delta.diedIDsLength(); i++) {
                this.bodies.delete(delta.diedIDs(i));
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