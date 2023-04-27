import Turn from './Turn';
import { schema } from 'battlecode-schema';
import assert from 'assert';
import Bodies, { Carrier, Launcher } from './Bodies';
import TurnStat from './TurnStat';

export default class Actions {
    actions: Action[] = [];

    constructor(

    ) {

    }

    applyDelta(turn: Turn, delta: schema.Round, calculateTurnStats: boolean = false): void {
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
                const newAction = new actionClass(robotID, target);
                this.actions.push(newAction);
                newAction.apply(turn, calculateTurnStats);
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

    /**
     * Applies this action to the turn provided. If stat is provided, it will be mutated to reflect the action as well
     * 
     * @param turn the turn to apply this action to
     * @param stat if provided, this action will mutate the stat to reflect the action
     */
    apply(turn: Turn, calculateTurnStats: boolean = false): void { };
    draw(turn: Turn, ctx: CanvasRenderingContext2D) { }
    copy(): Action {
        // creates a new object using this object's prototype and all its parameters. this is a shallow copy, override this if you need a deep copy
        return Object.create(
            Object.getPrototypeOf(this),
            Object.getOwnPropertyDescriptors(this)
        );
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
}

class Launch extends Action {
    apply(turn: Turn): void {
        const body = turn.bodies.getById(this.robotID);
        assert(body instanceof Launcher, 'Cannot launch from non-launcher');
    }
}

class ChangeAdamantium extends Action {
    apply(turn: Turn, calculateTurnStats = false): void {
        const body = turn.bodies.getById(this.robotID);
        if (calculateTurnStats && body.type !== schema.BodyType.HEADQUARTERS) {
            turn.stat.getTeamStat(body.team).adamantiumMined += this.target;
        }
        body.adamantium += this.target;
    }
}

class ChangeElixir extends Action {
    apply(turn: Turn, calculateTurnStats = false): void {
        const body = turn.bodies.getById(this.robotID);
        if (calculateTurnStats && body.type !== schema.BodyType.HEADQUARTERS) {
            turn.stat.getTeamStat(body.team).elixirMined += this.target;
        }
        body.elixir += this.target;
    }
}

class ChangeMana extends Action {
    apply(turn: Turn, calculateTurnStats = false): void {
        const body = turn.bodies.getById(this.robotID);
        if (calculateTurnStats && body.type !== schema.BodyType.HEADQUARTERS) {
            turn.stat.getTeamStat(body.team).manaMined += this.target;
        }
        body.mana += this.target;
    }
}

class ChangeHealth extends Action {
    apply(turn: Turn, calculateTurnStats = false): void {
        const body = turn.bodies.getById(this.robotID);
        if (calculateTurnStats) {
            turn.stat.getTeamStat(body.team).total_hp[body.type] += this.target;
        }
        body.hp += this.target;
    }
}

class DieException extends Action {
    apply(turn: Turn, calculateTurnStats?: boolean | undefined): void {
        console.log(`Exception occured: robotID(${this.robotID}), target(${this.target}`);
    }
}

export const ACTION_DEFINITIONS: Record<number, typeof Action> = {};
ACTION_DEFINITIONS[schema.Action.LAUNCH_ATTACK] = Launch;
ACTION_DEFINITIONS[schema.Action.THROW_ATTACK] = Throw;
ACTION_DEFINITIONS[schema.Action.PICK_UP_ANCHOR] = Action;
ACTION_DEFINITIONS[schema.Action.PLACE_ANCHOR] = Action;
ACTION_DEFINITIONS[schema.Action.DESTABILIZE] = Action;
ACTION_DEFINITIONS[schema.Action.BOOST] = Action;
ACTION_DEFINITIONS[schema.Action.BUILD_ANCHOR] = Action;
ACTION_DEFINITIONS[schema.Action.PLACE_ANCHOR] = Action;
ACTION_DEFINITIONS[schema.Action.CHANGE_ADAMANTIUM] = ChangeAdamantium;
ACTION_DEFINITIONS[schema.Action.CHANGE_ELIXIR] = ChangeElixir;
ACTION_DEFINITIONS[schema.Action.CHANGE_MANA] = ChangeMana;
ACTION_DEFINITIONS[schema.Action.CHANGE_HEALTH] = ChangeHealth;
ACTION_DEFINITIONS[schema.Action.SPAWN_UNIT] = Action;
ACTION_DEFINITIONS[schema.Action.DIE_EXCEPTION] = DieException;