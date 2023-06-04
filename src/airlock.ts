import { Lever } from "./lever";
import { Commands } from "./mcmd";
import { PressurePlate } from "./pressure_plate";
import { Coord, Dir, cor } from "./coord";
import { iff, solve } from "./condition";

let c = new Commands("airlock_1");

const pr_out = new PressurePlate(cor(5045, 227, 4960));
const pr_in = new PressurePlate(cor(5040, 227, 4960));

const lv_out = new Lever(cor(5043, 228, 4959), Dir.WEST);

const lv_in = new Lever(cor(5042, 228, 4959), Dir.EAST);

const lv_pres = new Lever(cor(5043, 228, 4961), Dir.NORTH);

const door_out = cor(5044, 227, 4960);
const door_in = cor(5041, 227, 4960);

const status_sign = cor(5072, 228, 4961);

enum Pressure {
    PRESURIZED = 0,
    DEPRESURIZED = 1,
    PRESSURIZING = 2,
    DEPRESURIZING = 3,
}

let pressurized = c.var_int("pressurized");

function close_door(pos: Coord) {
    return [
        `setblock ${pos.raw()} minecraft:white_concrete`,
        `setblock ${pos.above().raw()} minecraft:black_stained_glass`,
    ];
}

function open_door(pos: Coord) {
    return [`setblock ${pos.raw()} minecraft:air`, `setblock ${pos.above().raw()} minecraft:air`];
}

function set_sign(pos: Coord, text: string, color: string) {
    return `data merge block ${pos.raw()} {Text2:'{\\"text\\":\\"${text}\\"}'}`;
}

let pres_trigger = c.mk_trigger();

let time_scale = 1 / 3;

pres_trigger.at(0).with((c) => {
    c.execute(
        // prettier-ignore
        iff(pressurized.eq(Pressure.PRESSURIZING))
        .or(iff(pressurized.eq(Pressure.DEPRESURIZING)))
    ).with((c) => {
        c.repeat(lv_pres.set_on());
    });

    c.execute(iff(pressurized.eq(Pressure.DEPRESURIZED))).with((c) => {
        c.impulse(pressurized.set(Pressure.PRESSURIZING));
    });

    c.execute(iff(pressurized.eq(Pressure.PRESURIZED))).with((c) => {
        c.impulse(pressurized.set(Pressure.DEPRESURIZING));
    });

    c.impulse("bossbar set minecraft:airlock_1 color red");
    c.impulse("bossbar set minecraft:airlock_1 visible true");
});

for (let i = 0; i <= 100; i += 10) {
    pres_trigger.at(i * time_scale).with((c) => {
        c.impulse(`bossbar set minecraft:airlock_1 value ${i}`);
    });
}

pres_trigger.at(100 * time_scale).with((c) => {
    c.impulse("bossbar set minecraft:airlock_1 color green");
    c.impulse(lv_pres.set_off());
    c.execute(iff(pressurized.eq(Pressure.PRESSURIZING))).with((c) => {
        c.impulse(pressurized.set(Pressure.PRESURIZED));
    });
    c.execute(iff(pressurized.eq(Pressure.DEPRESURIZING))).with((c) => {
        c.impulse(pressurized.set(Pressure.DEPRESURIZED));
    });
});

pres_trigger.at(120 * time_scale).with((c) => {
    c.impulse("bossbar set minecraft:airlock_1 visible false");
    c.impulse("bossbar set minecraft:airlock_1 value 0");
    c.impulse("bossbar set minecraft:airlock_1 color red");
    c.repeat(pres_trigger.reset());
});

// Enter from outside, set depressurized
c.execute(iff(pr_out.is_on())).with((c) => {
    c.repeat(pressurized.set(Pressure.DEPRESURIZED));
    c.repeat_all(open_door(door_out));
    c.repeat(lv_out.set_off());
});

// Enter from inside, set pressurized
c.execute(iff(pr_in.is_on())).with((c) => {
    c.repeat(pressurized.set(Pressure.PRESURIZED));
    c.repeat_all(open_door(door_in));
    c.repeat(lv_in.set_off());
});

// close doors
c.execute(iff(pr_out.is_off()).and(iff(lv_out.is_off()))).with((c) => {
    c.repeat_all(close_door(door_out));
});

c.execute(iff(pr_in.is_off()).and(iff(lv_in.is_off()))).with((c) => {
    c.repeat_all(close_door(door_in));
});

// if lv_out is on and not pressurized
c.execute(iff(lv_out.is_on()).and(iff(pressurized.eq(Pressure.DEPRESURIZED)))).with((c) => {
    c.repeat_all(open_door(door_out));
});

c.execute(
    // prettier-ignore
    iff(lv_out.is_on()).and(
        iff(pressurized.eq(Pressure.PRESURIZED))
        .or(iff(pressurized.eq(Pressure.PRESSURIZING)))
        .or(iff(pressurized.eq(Pressure.DEPRESURIZING)))
    )
).with((c) => {
    c.repeat(lv_out.set_off());
});

c.execute(
    // prettier-ignore
    iff(lv_in.is_on()).and(
        iff(pressurized.eq(Pressure.DEPRESURIZED))
        .or(iff(pressurized.eq(Pressure.PRESSURIZING)))
        .or(iff(pressurized.eq(Pressure.DEPRESURIZING)))
    )
).with((c) => {
    c.repeat(lv_in.set_off());
});

// if lv_in is on and pressurized
c.execute(iff(lv_in.is_on()).and(iff(pressurized.eq(Pressure.PRESURIZED)))).with((c) => {
    c.repeat_all(open_door(door_in));
});

c.execute(iff(lv_out.is_on()).and(iff(lv_pres.is_on()))).with((c) => {
    c.repeat(lv_pres.set_off());
});

c.execute(iff(lv_in.is_on()).and(iff(lv_pres.is_on()))).with((c) => {
    c.repeat(lv_pres.set_off());
});

c.execute(
    // prettier-ignore
    iff(lv_pres.is_on())
    .and(iff(lv_out.is_on()).or(iff(lv_in.is_on())))
    .and(iff(pressurized.eq(Pressure.PRESURIZED)))
).with((c) => {
    c.repeat(lv_pres.set_off());
});

c.execute(
    // prettier-ignore
    iff(lv_pres.is_on())
    .and(iff(lv_out.is_off()))
    .and(iff(lv_in.is_off()))
    .and(iff(pressurized.eq(Pressure.PRESURIZED)).or(iff(pressurized.eq(Pressure.DEPRESURIZED))))
).with((c) => {
    c.repeat(pres_trigger.trigger());
});

c.execute(
    // prettier-ignore
    iff(pressurized.eq(Pressure.DEPRESURIZED))
    .and(iff(lv_out.is_off()))
    .and(iff(lv_in.is_off()))
    .and(iff(lv_pres.is_on()))
).with((c) => {
    c.repeat(`bossbar set minecraft:airlock_1 name \\"Pressurizing...\\"`);
});

c.execute(
    // prettier-ignore
    iff(pressurized.eq(Pressure.PRESURIZED))
    .and(iff(lv_out.is_off()))
    .and(iff(lv_in.is_off()))
    .and(iff(lv_pres.is_on()))
).with((c) => {
    c.repeat(`bossbar set minecraft:airlock_1 name \\"Depressurizing...\\"`);
});

c.build(cor(4930, 166, 4943));
