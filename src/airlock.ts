import { Coord, coord, Commands, and, D, iff } from "./mcmd";

let c = new Commands("airlock_1");

const pr_out = [5045, 227, 4960];
const pr_in = [5040, 227, 4960];

const lv_out = [5043, 228, 4959];
const lv_out_dir = D.WEST;

const lv_in = [5042, 228, 4959];
const lv_in_dir = D.EAST;

const lv_pres = [5043, 228, 4961];
const lv_pres_dir = D.NORTH;

const door_out = [5044, 227, 4960];
const door_in = [5041, 227, 4960];

const status_sign = [5072, 228, 4961];

enum Pressure {
    PRESURIZED = 0,
    DEPRESURIZED = 1,
    PRESSURIZING = 2,
    DEPRESURIZING = 3,
}

function above(pos: Coord) {
    return [pos[0], pos[1] + 1, pos[2]];
}

function lever_on(pos: Coord) {
    return `block ${coord(pos)} minecraft:lever[powered=true]`;
}

function lever_off(pos: Coord) {
    return `block ${coord(pos)} minecraft:lever[powered=false]`;
}

function pr_on(pos: Coord) {
    return `block ${coord(pos)} minecraft:polished_blackstone_pressure_plate[powered=true]`;
}

function pr_off(pos: Coord) {
    return `block ${coord(pos)} minecraft:polished_blackstone_pressure_plate[powered=false]`;
}

function set_lever(pos: Coord, dir: D, on: boolean) {
    return `setblock ${coord(pos)} minecraft:lever[facing=${dir},powered=${on}]`;
}

let pressurized = c.var_int("pressurized");

function close_door(pos: Coord) {
    return [
        `setblock ${coord(pos)} minecraft:stone`,
        `setblock ${coord(above(pos))} minecraft:stone`,
    ];
}

function open_door(pos: Coord) {
    return [`setblock ${coord(pos)} minecraft:air`, `setblock ${coord(above(pos))} minecraft:air`];
}

function set_sign(pos: Coord, text: string, color: string) {
    return `data merge block ${coord(pos)} {Text2:'{\\"text\\":\\"${text}\\"}'}`;
}

let pres_trigger = c.mk_trigger();

let time_scale = 1 / 3;

pres_trigger.at(0).with((c) => {
    c.execute(
        iff(pressurized.eq(Pressure.PRESSURIZING)),
        iff(pressurized.eq(Pressure.DEPRESURIZING))
    ).with((c) => {
        c.repeat(set_lever(lv_pres, lv_pres_dir, true));
    });

    c.execute(iff(pressurized.eq(Pressure.DEPRESURIZED))).with((c) => {
        c.impulse(pressurized.set(Pressure.PRESSURIZING));
        c.impulse(set_sign(status_sign, "Pressurizing", "red"));
        c.impulse(`bossbar set minecraft:airlock_1 name \\"Pressurizing...\\"`);
    });

    c.execute(iff(pressurized.eq(Pressure.PRESURIZED))).with((c) => {
        c.impulse(pressurized.set(Pressure.DEPRESURIZING));
        c.impulse(set_sign(status_sign, "Depressurizing", "red"));
        c.impulse(`bossbar set minecraft:airlock_1 name \\"Depressurizing...\\"`);
    });

    c.impulse("bossbar set minecraft:airlock_1 color red");
    c.impulse("bossbar set minecraft:airlock_1 value 0");
    c.impulse("bossbar set minecraft:airlock_1 visible true");
});

for (let i = 10; i <= 90; i += 10) {
    pres_trigger.at(i * time_scale).with((c) => {
        c.impulse(`bossbar set minecraft:airlock_1 value ${i}`);
    });
}

pres_trigger.at(100 * time_scale).with((c) => {
    c.impulse("bossbar set minecraft:airlock_1 color green");
    c.impulse("bossbar set minecraft:airlock_1 value 100");
    c.impulse(set_lever(lv_pres, lv_pres_dir, false));
    c.execute(iff(pressurized.eq(Pressure.PRESSURIZING))).with((c) => {
        c.impulse(pressurized.set(Pressure.PRESURIZED)).impulse(
            set_sign(status_sign, "Pressurized", "green")
        );
    });
    c.execute(iff(pressurized.eq(Pressure.DEPRESURIZING))).with((c) => {
        c.impulse(pressurized.set(Pressure.DEPRESURIZED)).impulse(
            set_sign(status_sign, "Depressurized", "green")
        );
    });
});

pres_trigger.at(120 * time_scale).with((c) => {
    c.impulse("bossbar set minecraft:airlock_1 visible false");
    c.impulse("bossbar set minecraft:airlock_1 value 0");
    c.impulse("bossbar set minecraft:airlock_1 color red");
    c.repeat(pres_trigger.reset());
});

// Enter from outside, set depressurized
c.execute(iff(pr_on(pr_out))).with((c) => {
    c.repeat(pressurized.set(Pressure.DEPRESURIZED));
    c.repeat(set_sign(status_sign, "Depressurized", "green"));
    c.repeat_all(open_door(door_out));
    c.repeat(set_lever(lv_out, lv_out_dir, false));
});

// Enter from inside, set pressurized
c.execute(iff(pr_on(pr_in))).with((c) => {
    c.repeat(pressurized.set(Pressure.PRESURIZED));
    c.repeat(set_sign(status_sign, "Pressurized", "green"));
    c.repeat_all(open_door(door_in));
    c.repeat(set_lever(lv_in, lv_in_dir, false));
});

// close doors
c.execute(and(iff(pr_off(pr_out)), iff(lever_off(lv_out)))).with((c) => {
    c.repeat_all(close_door(door_out));
});

c.execute(and(iff(pr_off(pr_in)), iff(lever_off(lv_in)))).with((c) => {
    c.repeat_all(close_door(door_in));
});

// if lv_out is on and not pressurized
c.execute(and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.DEPRESURIZED)))).with((c) => {
    c.repeat_all(open_door(door_out));
});

c.execute(
    and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.PRESURIZED))),
    and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.PRESSURIZING))),
    and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.DEPRESURIZING)))
).with((c) => {
    c.repeat(set_lever(lv_out, lv_out_dir, false));
});

c.execute(
    and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.DEPRESURIZED))),
    and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.PRESSURIZING))),
    and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.DEPRESURIZING)))
).with((c) => {
    c.repeat(set_lever(lv_in, lv_in_dir, false));
});

// if lv_in is on and pressurized
c.execute(and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.PRESURIZED)))).with((c) => {
    c.repeat_all(open_door(door_in));
});

c.execute(and(iff(lever_on(lv_out)), iff(lever_on(lv_pres)))).with((c) => {
    c.repeat(set_lever(lv_pres, lv_pres_dir, false));
});

c.execute(and(iff(lever_on(lv_in)), iff(lever_on(lv_pres)))).with((c) => {
    c.repeat(set_lever(lv_pres, lv_pres_dir, false));
});

c.execute(
    and(
        iff(pressurized.eq(Pressure.DEPRESURIZED)),
        iff(lever_on(lv_out)),
        iff(lever_off(lv_in)),
        iff(lever_on(lv_pres))
    ),
    and(
        iff(pressurized.eq(Pressure.DEPRESURIZED)),
        iff(lever_off(lv_out)),
        iff(lever_on(lv_in)),
        iff(lever_on(lv_pres))
    ),
    and(
        iff(pressurized.eq(Pressure.DEPRESURIZED)),
        iff(lever_on(lv_out)),
        iff(lever_on(lv_in)),
        iff(lever_on(lv_pres))
    )
).with((c) => {
    c.repeat(set_lever(lv_pres, lv_pres_dir, false));
});

c.execute(
    and(
        iff(pressurized.eq(Pressure.DEPRESURIZED)),
        iff(lever_off(lv_out)),
        iff(lever_off(lv_in)),
        iff(lever_on(lv_pres))
    ),
    and(
        iff(pressurized.eq(Pressure.PRESURIZED)),
        iff(lever_off(lv_out)),
        iff(lever_off(lv_in)),
        iff(lever_on(lv_pres))
    )
).with((c) => {
    c.repeat(pres_trigger.trigger());
});

c.build([4930, 166, 4943]);
