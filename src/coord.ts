export enum Dir {
    NORTH = "north",
    SOUTH = "south",
    EAST = "east",
    WEST = "west",
}

export function cor(x: number, y: number, z: number): Coord {
    return new Coord(x, y, z);
}

export class Coord {
    x: number;
    y: number;
    z: number;

    constructor(x: number, y: number, z: number) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    above(): Coord {
        return cor(this.x, this.y + 1, this.z);
    }

    plus(...args: [Coord] | [number, number, number]): Coord {
        let other: Coord;
        if (args.length == 1 && args[0] instanceof Coord) {
            other = args[0];
        } else if (
            args.length == 3 &&
            typeof args[0] == "number" &&
            typeof args[1] == "number" &&
            typeof args[2] == "number"
        ) {
            other = new Coord(args[0], args[1], args[2]);
        } else {
            throw new Error("Invalid arguments to Coord.plus");
        }

        return cor(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    minus(...args: [Coord] | [number, number, number]): Coord {
        let other: Coord;
        if (args.length == 1 && args[0] instanceof Coord) {
            other = args[0];
        } else if (
            args.length == 3 &&
            typeof args[0] == "number" &&
            typeof args[1] == "number" &&
            typeof args[2] == "number"
        ) {
            other = new Coord(args[0], args[1], args[2]);
        } else {
            throw new Error("Invalid arguments to Coord.plus");
        }

        return cor(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    times(other: number): Coord {
        return cor(this.x * other, this.y * other, this.z * other);
    }

    div(other: number): Coord {
        return cor(this.x / other, this.y / other, this.z / other);
    }

    raw() {
        return `${this.x} ${this.y} ${this.z}`;
    }
}
