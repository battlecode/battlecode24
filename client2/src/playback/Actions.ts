import Turn from './Turn';
import { schema } from 'battlecode-schema';
import assert from 'assert';
import Bodies, { Carrier, Launcher } from './Bodies';

export default class Actions {
    actions: Action[] = [];

    constructor(

    ) {

    }

    applyDelta(delta: schema.Round): void {
        for (let i = 0; i < this.actions.length; i++) {
            this.actions[i].duration--;
            if (this.actions[i].duration == 0) {
                this.actions.splice(i, 1);
                i--;
            }
        }

        if (delta.actionsLength() > 0) {
            for (let i = 0; i < delta.actionsLength(); i++) {
                const action = delta.actions(i) ?? assert.fail('actions not found in round');
                const robotID = delta.actionIDs(i) ?? assert.fail('actionIDs not found in round');
                const target = delta.actionTargets(i) ?? assert.fail('actionTargets not found in round');
                const actionClass = ACTION_DEFINITIONS[action] ?? assert.fail(`Action ${action} not found in ACTION_DEFINITIONS`);
                this.actions.push(new actionClass(robotID, target));
            }
        }
    }

    copy(): Actions {
        const newActions = new Actions();
        newActions.actions = this.actions.map(action => action.copy());
        return newActions;
    }
}

export class Action {
    constructor(
        protected robotID: number,
        protected target: number,
        public duration: number = 1
    ) { }

    apply(turn: Turn): void {
        throw new Error('Not implemented, Action should not be instantiated directly');
    };
    draw(turn: Turn, ctx: CanvasRenderingContext2D) {
        throw new Error('Not implemented, Action should not be instantiated directly');
    }
    copy(): Action {
        throw new Error('Not implemented, Action should not be instantiated directly');
    };
}

class PlaceholderAction extends Action {
    constructor(robotID: number, target: number) {
        super(robotID, target, 1);
    }
    apply(turn: Turn): void { }
    draw(turn: Turn, ctx: CanvasRenderingContext2D) { }
    copy(): PlaceholderAction {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

class Throw extends Action {
    constructor(robotID: number, target: number) {
        super(robotID, target, 1);
    }
    apply(turn: Turn): void {
        const body = turn.bodies.getById(this.robotID);
        assert(body instanceof Carrier, 'Cannot throw from non-carrier');
        body.clearResources();
    }
    draw(turn: Turn, ctx: CanvasRenderingContext2D) {
        let targetLoc;
        if (this.target >= 0) { // Hit attack: target is bot
            const targetBody = turn.bodies.getById(this.target);
            targetLoc = { x: targetBody.x, y: targetBody.y };
        } else { // Missed attack: target is location (-location - 1)
            targetLoc = turn.map.indexToLocation(-this.target - 1);
        }
    }
    copy(): Throw {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

class Launch extends Action {
    constructor(robotID: number, target: number) {
        super(robotID, target, 1);
    }

    apply(turn: Turn): void {
        const body = turn.bodies.getById(this.robotID);
        assert(body instanceof Launcher, 'Cannot launch from non-launcher');
    }

    copy(): Launch {
        return { ...this }; // if you store data more than one level deep, you'll need to copy it too
    }
}

class ChangeAdamantium extends Action {
    case schema.Action.CHANGE_ADAMANTIUM:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
teamStatsObj.adamantiumMined += target;
this.bodies.alter({ id: robotID, adamantium: body.adamantium + target });
break;
}

class ChangeElixir extends Action {
    case schema.Action.CHANGE_ELIXIR:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
teamStatsObj.elixirMined += target;
this.bodies.alter({ id: robotID, elixir: body.elixir + target });
break;
}

class ChangeMana extends Action {
    case schema.Action.CHANGE_MANA:
        if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
teamStatsObj.manaMined += target;
this.bodies.alter({ id: robotID, mana: body.mana + target });
break;
}

class ChangeHealth extends Action {
    case schema.Action.CHANGE_HEALTH:
        this.bodies.alter({ id: robotID, hp: body.hp + target });
        teamStatsObj.total_hp[body.type] += target;
        break;
}

class DieException extends Action {

    case schema.Action.DIE_EXCEPTION:
        console.log(`Exception occured: robotID(${robotID}), target(${target}`);
break;
}

export const ACTION_DEFINITIONS: Record<number, typeof Action> = {};
ACTION_DEFINITIONS[schema.Action.LAUNCH_ATTACK] = Launch;
ACTION_DEFINITIONS[schema.Action.THROW_ATTACK] = Throw;
ACTION_DEFINITIONS[schema.Action.PICK_UP_ANCHOR] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.PLACE_ANCHOR] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.DESTABILIZE] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.BOOST] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.BUILD_ANCHOR] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.PLACE_ANCHOR] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.CHANGE_ADAMANTIUM] = ChangeAdamantium;
ACTION_DEFINITIONS[schema.Action.CHANGE_ELIXIR] = ChangeElixir;
ACTION_DEFINITIONS[schema.Action.CHANGE_MANA] = ChangeMana;
ACTION_DEFINITIONS[schema.Action.CHANGE_HEALTH] = ChangeHealth;
ACTION_DEFINITIONS[schema.Action.SPAWN_UNIT] = PlaceholderAction;
ACTION_DEFINITIONS[schema.Action.DIE_EXCEPTION] = DieException;