import { Coord, Dir } from "./coord";

export class PressurePlate {
    private pos: Coord;

    constructor(pos: Coord) {
        this.pos = pos;
    }

    is_on() {
        return `block ${this.pos.raw()} minecraft:polished_blackstone_pressure_plate[powered=true]`;
    }

    is_off() {
        return `block ${this.pos.raw()} minecraft:polished_blackstone_pressure_plate[powered=false]`;
    }
}
