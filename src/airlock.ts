import { Coord, coord, Commands, and, D, iff } from "./mcmd";

let c = new Commands("airlock_1");

const pr_out = [5045, 227, 4960]
const pr_in = [5040, 227, 4960]

const lv_out = [5043, 228, 4959]
const lv_out_dir = D.WEST;

const lv_in = [5042, 228, 4959]
const lv_in_dir = D.EAST;

const lv_pres = [5043, 228, 4961]
const lv_pres_dir = D.NORTH;

const door_out = [5044, 227, 4960]
const door_in = [5041, 227, 4960]

const status_sign = [5072, 228, 4961]

enum Pressure {
    PRESURIZED = 0,
    DEPRESURIZED = 1,
    PRESSURIZING = 2,
    DEPRESURIZING = 3,
}

function above(pos: Coord) {
    return [pos[0], pos[1] + 1, pos[2]]
}

function lever_on(pos: Coord) {
    return `block ${coord(pos)} minecraft:lever[powered=true]`
}

function lever_off(pos: Coord) {
    return `block ${coord(pos)} minecraft:lever[powered=false]`
}

function pr_on(pos: Coord) {
    return `block ${coord(pos)} minecraft:polished_blackstone_pressure_plate[powered=true]`
}

function pr_off(pos: Coord) {
    return `block ${coord(pos)} minecraft:polished_blackstone_pressure_plate[powered=false]`
}

function set_lever(pos: Coord, dir: D, on: boolean) {
    return `setblock ${coord(pos)} minecraft:lever[facing=${dir},powered=${on}]`
}

let pressurized = c.var_int("pressurized")

function close_door(pos: Coord) {
    return [
        `setblock ${coord(pos)} minecraft:stone`,
        `setblock ${coord(above(pos))} minecraft:stone`,
    ];
}

function open_door(pos: Coord) {
    return [
        `setblock ${coord(pos)} minecraft:air`,
        `setblock ${coord(above(pos))} minecraft:air`,
    ];
}

function set_sign(pos: Coord, text: string, color: string) {
    return `data merge block ${coord(pos)} {Text2:'{\\"text\\":\\"${text}\\"}'}`
}

let pres_trigger = c.mk_trigger();

let time_scale = 1 / 3;

pres_trigger.at(0)
    .execute(iff(pressurized.eq(Pressure.PRESSURIZING)), iff(pressurized.eq(Pressure.DEPRESURIZING)))
        .repeat(set_lever(lv_pres, lv_pres_dir, true))
    .end()
    .execute(iff(pressurized.eq(Pressure.DEPRESURIZED)))
        .impulse(pressurized.set(Pressure.PRESSURIZING))
        .impulse(set_sign(status_sign, "Pressurizing", "red"))
        .impulse(`bossbar set minecraft:airlock_1 name \\"Pressurizing...\\"`)
    .end()
    .execute(iff(pressurized.eq(Pressure.PRESURIZED)))
        .impulse(pressurized.set(Pressure.DEPRESURIZING))
        .impulse(set_sign(status_sign, "Depressurizing", "red"))
        .impulse(`bossbar set minecraft:airlock_1 name \\"Depressurizing...\\"`)
    .end()
    .impulse("bossbar set minecraft:airlock_1 color red")
    .impulse("bossbar set minecraft:airlock_1 value 0")
    .impulse("bossbar set minecraft:airlock_1 visible true")

for (let i = 10; i <= 90; i += 10) {
    pres_trigger.at(i * time_scale)
        .impulse(`bossbar set minecraft:airlock_1 value ${i}`)
}

pres_trigger.at(100 * time_scale)
    .impulse("bossbar set minecraft:airlock_1 color green")
    .impulse("bossbar set minecraft:airlock_1 value 100")
    .impulse(set_lever(lv_pres, lv_pres_dir, false))
    .execute(iff(pressurized.eq(Pressure.PRESSURIZING)))
        .impulse(pressurized.set(Pressure.PRESURIZED))
        .impulse(set_sign(status_sign, "Pressurized", "green"))
    .end()
    .execute(iff(pressurized.eq(Pressure.DEPRESURIZING)))
        .impulse(pressurized.set(Pressure.DEPRESURIZED))
        .impulse(set_sign(status_sign, "Depressurized", "green"))
    .end()

pres_trigger.at(120 * time_scale)
    .impulse("bossbar set minecraft:airlock_1 visible false")
    .impulse("bossbar set minecraft:airlock_1 value 0")
    .impulse("bossbar set minecraft:airlock_1 color red")
    .repeat(pres_trigger.reset())

c
    // Enter from outside, set depressurized
    .execute(iff(pr_on(pr_out)))
        .repeat(pressurized.set(Pressure.DEPRESURIZED))
        .repeat(set_sign(status_sign, "Depressurized", "green"))
        .repeat_all(open_door(door_out))
        .repeat(set_lever(lv_out, lv_out_dir, false))
    .end()

    // Enter from inside, set pressurized
    .execute(iff(pr_on(pr_in)))
        .repeat(pressurized.set(Pressure.PRESURIZED))
        .repeat(set_sign(status_sign, "Pressurized", "green"))
        .repeat_all(open_door(door_in))
        .repeat(set_lever(lv_in, lv_in_dir, false))
    .end()

    // close doors
    .execute(and(iff(pr_off(pr_out)), iff(lever_off(lv_out))))
        .repeat_all(close_door(door_out))
    .end()

    .execute(and(iff(pr_off(pr_in)), iff(lever_off(lv_in))))
        .repeat_all(close_door(door_in))
    .end()

    // if lv_out is on and not pressurized
    .execute(and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.DEPRESURIZED))))
        .repeat_all(open_door(door_out))
    .end()

    .execute(
        and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.PRESURIZED))),
        and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.PRESSURIZING))),
        and(iff(lever_on(lv_out)), iff(pressurized.eq(Pressure.DEPRESURIZING)))
    )
        .repeat(set_lever(lv_out, lv_out_dir, false))
    .end()

    .execute(
        and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.DEPRESURIZED))),
        and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.PRESSURIZING))),
        and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.DEPRESURIZING)))
    )
        .repeat(set_lever(lv_in, lv_in_dir, false))
    .end()

    // if lv_in is on and pressurized
    .execute(and(iff(lever_on(lv_in)), iff(pressurized.eq(Pressure.PRESURIZED))))
        .repeat_all(open_door(door_in))
    .end()

    .execute(and(iff(lever_on(lv_out)), iff(lever_on(lv_pres))))
        .repeat(set_lever(lv_pres, lv_pres_dir, false))
    .end()

    .execute(and(iff(lever_on(lv_in)), iff(lever_on(lv_pres))))
        .repeat(set_lever(lv_pres, lv_pres_dir, false))
    .end()

    .execute(
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
    )
        .repeat(set_lever(lv_pres, lv_pres_dir, false))
    .end()

    .execute(
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
    )
        .repeat(pres_trigger.trigger())
    .end()
.build([4930,166,4943])