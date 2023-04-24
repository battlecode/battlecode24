import Turn from './Turn';
import { schema } from 'battlecode-schema';

export default class Actions {
    actions: Action[] = [];
    applyDelta(delta: schema.Round): void {
        this.actions = [];
    }
    copy(): Actions {
        throw new Error('Method not implemented.');
    }
}

export abstract class Action {
    abstract apply(turn: Turn): void;
    abstract copy(): Action;
}

export const ACTION_DEFINITIONS: Record<number, typeof Action> = {
    1: class Launch implements Action {
        constructor() {
        }
        apply(turn: Turn): void {
            turn.bodies.bodies.get(this.robotID).clearResources();
        }
        copy(): Launch {
            return { ...this }; // if you store data more than one level deep, you'll need to copy it too
        }
    }
};