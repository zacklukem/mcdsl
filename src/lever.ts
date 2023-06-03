import { Coord, Dir } from "./coord";

export class Lever {
    private pos: Coord;
    private dir: Dir;
    constructor(pos: Coord, dir: Dir) {
        this.pos = pos;
        this.dir = dir;
    }

    is_on() {
        return `block ${this.pos.raw()} minecraft:lever[powered=true]`;
    }

    is_off() {
        return `block ${this.pos.raw()} minecraft:lever[powered=false]`;
    }

    set_on() {
        return this.set(true);
    }

    set_off() {
        return this.set(false);
    }

    set(on: boolean) {
        return `setblock ${this.pos.raw()} minecraft:lever[facing=${this.dir},powered=${on}]`;
    }
}
