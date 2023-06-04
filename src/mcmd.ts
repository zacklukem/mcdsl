import { Condition, solve } from "./condition";
import { Coord, Dir } from "./coord";
export class CallbackRunner<T, K> {
    private p: T;
    private k: K;

    constructor(p: T, k: K) {
        this.p = p;
        this.k = k;
    }

    with(f: (p: T) => void): K {
        f(this.p);
        return this.k;
    }
}

// export function iff(pred: string): string {
//     return `if ${pred}`;
// }

// export function unless(pred: string): string {
//     return `unless ${pred}`;
// }

// export function and(...conditions: string[]): string {
//     return conditions.join(" ");
// }

export interface AddCmd {
    repeat(cmd: string): AddCmd;
    repeat_all(cmds: string[]): AddCmd;
    impulse(cmd: string): AddCmd;
    impulse_all(cmds: string[]): AddCmd;
}

export class Trigger {
    times: Map<number, CommandsInner>;
    ns: string;
    id: string;
    ea: number;

    constructor(ns: string, id: string) {
        this.times = new Map();
        this.ns = ns;
        this.id = id;
        this.ea = 0;
    }

    at(time: number): CallbackRunner<CommandsInner, Trigger> {
        if (this.times.has(time)) {
            return new CallbackRunner(this.times.get(time)!, this);
        } else {
            let c = new CommandsInner(this.ns);
            this.times.set(time, c);
            return new CallbackRunner(c, this);
        }
    }

    trigger(): string {
        return `@t${this.id}%`;
    }

    reset(): string {
        return `@r${this.id}%`;
    }
}

export class VarBool {
    private rep: string;
    private ns: string;

    constructor(id: string, ns: string) {
        this.rep = id;
        this.ns = ns;
    }

    true(): string {
        return `score ${this.rep} ${this.ns} matches 1`;
    }

    false(): string {
        return `score ${this.rep} ${this.ns} matches 0`;
    }

    set_true(): string {
        return `scoreboard players set ${this.rep} ${this.ns} 1`;
    }

    set_false(): string {
        return `scoreboard players set ${this.rep} ${this.ns} 0`;
    }

    toggle(): string[] {
        return [
            `execute if ${this.false()} run ${this.set_true()}`,
            `execute if ${this.true()} run ${this.set_false()}`,
        ];
    }
}

export class VarInt {
    private rep: string;
    private ns: string;

    constructor(id: string, ns: string) {
        this.rep = id;
        this.ns = ns;
    }

    eq(val: number): string {
        return `score ${this.rep} ${this.ns} matches ${val}`;
    }

    matches(val: string): string {
        return `score ${this.rep} ${this.ns} matches ${val}`;
    }

    set(val: number): string {
        return `scoreboard players set ${this.rep} ${this.ns} ${val}`;
    }
}

export class CommandsInner implements AddCmd {
    protected commands: [boolean, string][];
    protected ns: string;

    constructor(ns: string) {
        this.ns = ns;
        this.commands = [];
    }

    repeat(...cmd: string[]): CommandsInner {
        for (let c of cmd) {
            this.commands.push([true, c]);
        }
        return this;
    }

    repeat_all(cmds: string[]): CommandsInner {
        for (let c of cmds) {
            this.commands.push([true, c]);
        }
        return this;
    }

    impulse(...cmd: string[]): CommandsInner {
        for (let c of cmd) {
            this.commands.push([false, c]);
        }
        return this;
    }

    impulse_all(cmds: string[]): CommandsInner {
        for (let c of cmds) {
            this.commands.push([false, c]);
        }
        return this;
    }

    execute(cond: Condition): CallbackRunner<ExecuteBuilder<CommandsInner>, CommandsInner> {
        return new CallbackRunner(new ExecuteBuilder(this, solve(cond)), this);
    }

    var_bool(id?: string): VarBool {
        return new VarBool(id || crypto.randomUUID(), this.ns);
    }

    var_int(id?: string): VarInt {
        return new VarInt(id || crypto.randomUUID(), this.ns);
    }

    cmds(): [boolean, string][] {
        return this.commands;
    }
}

export class Commands extends CommandsInner {
    private triggers: Map<string, Trigger>;
    private trigger_id: number;

    constructor(ns: string) {
        super(ns);
        this.triggers = new Map();
        this.trigger_id = 0;
    }

    override execute(cond: Condition): CallbackRunner<ExecuteBuilder<Commands>, Commands> {
        return new CallbackRunner(new ExecuteBuilder(this, solve(cond)), this);
    }

    build(root_pos: Coord) {
        function repeater(pos: Coord, dir: Dir, delay: number): string {
            return `setblock ${pos.raw()} minecraft:repeater[facing=${dir},delay=${delay}]`;
        }

        function cmd_blk(
            pos: Coord,
            repeat: boolean,
            needs_redstone: boolean,
            cmd: string
        ): string {
            if (repeat) {
                return `setblock ${pos.raw()} minecraft:repeating_command_block{Command:"${cmd}",auto:${
                    needs_redstone ? 0 : 1
                }}`;
            } else {
                return `setblock ${pos.raw()} minecraft:command_block{Command:"${cmd}",auto:${
                    needs_redstone ? 0 : 1
                }}`;
            }
        }

        let out = [];

        let trigger_map: Map<string, [Coord, Coord]> = new Map();

        let x = 0;
        let start_x = x;
        let start_z = 3;
        let end_z = 3;
        for (let [id, trigger] of this.triggers.entries()) {
            let start_x = x;
            for (let [time, commands] of trigger.times.entries()) {
                for (let [repeat, command] of commands.cmds()) {
                    let t = Math.round(time);
                    let n = 3;
                    while (t > 0) {
                        out.push(repeater(root_pos.plus(n, 0, x), Dir.WEST, Math.min(t, 4)));
                        t -= Math.min(t, 4);
                        n++;
                    }
                    end_z = Math.max(end_z, n);
                    out.push(cmd_blk(root_pos.plus(n, 0, x), repeat, true, command));
                    x++;
                }
            }
            let end_x = x;
            trigger_map.set(id, [root_pos.plus(2, 0, start_x), root_pos.plus(2, 0, end_x)]);
        }
        let end_x = x;
        out.unshift(
            `fill ${root_pos.plus(start_z - 1, 0, start_x).raw()} ${root_pos
                .plus(end_z, 0, end_x)
                .raw()} minecraft:air`
        );
        out.unshift(
            `fill ${root_pos.plus(start_z, -1, start_x).raw()} ${root_pos
                .plus(end_z, -1, end_x)
                .raw()} minecraft:gray_wool`
        );

        x = 0;
        for (let [repeat, command] of this.commands) {
            out.push(cmd_blk(root_pos.plus(0, 0, x), repeat, false, command));
            x++;
        }

        for (let i in out) {
            for (let [id, trig] of trigger_map.entries()) {
                out[i] = out[i].replaceAll(
                    `@t${id}%`,
                    `fill ${trig[0].raw()} ${trig[1].raw()} minecraft:redstone_block`
                );
                out[i] = out[i].replaceAll(
                    `@r${id}%`,
                    `fill ${trig[0].raw()} ${trig[1].raw()} minecraft:brown_wool`
                );
            }
        }

        for (let cmd of out) {
            console.log(cmd);
        }
    }

    mk_trigger(): Trigger {
        let t = new Trigger(this.ns, (this.trigger_id++).toString());
        this.triggers.set(t.id, t);
        return t;
    }
}

export class ExecuteBuilder<T extends AddCmd> implements AddCmd {
    c: T;
    conditions: string[];

    constructor(c: T, conditions: string[]) {
        this.c = c;
        this.conditions = conditions;
    }

    repeat(cmd: string): ExecuteBuilder<T> {
        for (let condition of this.conditions) {
            this.c.repeat(`execute ${condition} run ${cmd}`);
        }
        return this;
    }

    repeat_all(cmds: string[]): ExecuteBuilder<T> {
        for (let condition of this.conditions) {
            this.c.repeat_all(cmds.map((c) => `execute ${condition} run ${c}`));
        }
        return this;
    }

    impulse(cmd: string): ExecuteBuilder<T> {
        for (let condition of this.conditions) {
            this.c.impulse(`execute ${condition} run ${cmd}`);
        }
        return this;
    }

    impulse_all(cmds: string[]): ExecuteBuilder<T> {
        for (let condition of this.conditions) {
            this.c.impulse_all(cmds.map((c) => `execute ${condition} run ${c}`));
        }
        return this;
    }

    end(): T {
        return this.c;
    }
}
