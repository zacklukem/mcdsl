package com.zacklukem.mcdsl.util

import kotlin.math.roundToInt

/**
 * A minecraft cardinal direction
 */
enum class Dir(private val repr: String) {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west");

    override fun toString(): String {
        return repr
    }
}

sealed interface CoordComponent {
    operator fun plus(other: CoordComponent): CoordComponent
    operator fun minus(other: CoordComponent): CoordComponent
    operator fun times(other: Float): CoordComponent
    operator fun div(other: Float): CoordComponent
}

class Tilde(val offset: Float = 0.0f) : CoordComponent {
    constructor(offset: Int) : this(offset.toFloat())

    override fun plus(other: CoordComponent): CoordComponent {
        return when (other) {
            is Number -> {
                Tilde(offset + other.v)
            }
            is Tilde -> {
                Tilde(offset + other.offset)
            }
        }
    }

    override fun minus(other: CoordComponent): CoordComponent {
        return when (other) {
            is Number -> {
                Tilde(offset - other.v)
            }
            is Tilde -> {
                Tilde(offset - other.offset)
            }
        }
    }

    override fun times(other: Float): CoordComponent {
        throw IllegalArgumentException("Cannot multiply Tilde by float")
    }

    override fun div(other: Float): CoordComponent {
        throw IllegalArgumentException("Cannot multiply Tilde by float")
    }

    override fun toString(): String {
        return "~${offset.roundToInt()}"
    }
}

class Number(val v: Float) : CoordComponent {
    constructor(v: Int) : this(v.toFloat())

    override fun plus(other: CoordComponent): CoordComponent {
        return when (other) {
            is Number -> {
                Number(v + other.v)
            }
            is Tilde -> {
                Tilde(v + other.offset)
            }
        }
    }

    override fun minus(other: CoordComponent): CoordComponent {
        return when (other) {
            is Number -> {
                Number(v - other.v)
            }
            is Tilde -> {
                Tilde(v - other.offset)
            }
        }
    }

    override fun times(other: Float): CoordComponent {
        return Number(v * other)
    }

    override fun div(other: Float): CoordComponent {
        return Number(v / other)
    }

    override fun toString(): String {
        return "${v.roundToInt()}"
    }
}

/**
 * Represents a coordinate in the Minecraft world.
 *
 * This also includes the tilde (~) operator, which represents the current position of caller.
 *
 * This supports math using the +, -, *, and / operators.
 *
 * Tilde values don't support multiplication or division.
 *
 * By default, the toString method will return the coordinate in the format `x y z`. With all values rounded to
 * integers (with tildes).
 *
 * If you want to output floating point values, you can use the [Coord.toFloatString] method.
 *
 * Example:
 * ```
 * val coord = Coord(1, 2, 3)
 * val coord2 = Coord(4, 5, 6)
 * val coord3 = coord + coord2
 * val coord4 = coord3 - Coord(Tilde(), 5, 6)
 * val coord4 = coord3 - Coord(Tilde(), 5, Tilde())
 * ```
 */
class Coord(val x: CoordComponent, val y: CoordComponent, val z: CoordComponent) {

    constructor(x: Int, y: Int, z: Int) : this(Number(x), Number(y), Number(z))
    constructor(x: CoordComponent, y: Int, z: Int) : this(x, Number(y), Number(z))
    constructor(x: Int, y: CoordComponent, z: Int) : this(Number(x), y, Number(z))
    constructor(x: CoordComponent, y: CoordComponent, z: Int) : this(x, y, Number(z))
    constructor(x: Int, y: Int, z: CoordComponent) : this(Number(x), Number(y), z)
    constructor(x: CoordComponent, y: Int, z: CoordComponent) : this(x, Number(y), z)
    constructor(x: Int, y: CoordComponent, z: CoordComponent) : this(Number(x), y, z)

    constructor(x: Float, y: Float, z: Float) : this(Number(x), Number(y), Number(z))
    constructor(x: CoordComponent, y: Float, z: Float) : this(x, Number(y), Number(z))
    constructor(x: Float, y: CoordComponent, z: Float) : this(Number(x), y, Number(z))
    constructor(x: CoordComponent, y: CoordComponent, z: Float) : this(x, y, Number(z))
    constructor(x: Float, y: Float, z: CoordComponent) : this(Number(x), Number(y), z)
    constructor(x: CoordComponent, y: Float, z: CoordComponent) : this(x, Number(y), z)
    constructor(x: Float, y: CoordComponent, z: CoordComponent) : this(Number(x), y, z)

    operator fun plus(other: Coord): Coord =
        Coord(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Coord): Coord =
        Coord(x - other.x, y - other.y, z - other.z)

    operator fun times(other: Float): Coord =
        Coord(x * other, y * other, z * other)

    operator fun div(other: Float): Coord =
        Coord(x / other, y / other, z / other)

    /**
     * Convert this coordinate to the format `x y z` with integer values.
     */
    override fun toString(): String {
        return "$x $y $z"
    }

    /**
     * Convert this coordinate to the format `x y z` with floating point values.
     */
    fun toFloatString(): String {
        return "$x $y $z"
    }

    /**
     * Set the block at this coordinate.
     */
    fun setblock(block: String): String {
        return "setblock $this $block"
    }

    companion object {
        val UP = Coord(0, 1, 0)
        val DOWN = Coord(0, -1, 0)
    }
}