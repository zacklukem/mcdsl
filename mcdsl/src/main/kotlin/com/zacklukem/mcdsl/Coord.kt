package com.zacklukem.mcdsl

import kotlin.math.roundToInt

enum class Dir(val repr: String) {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west");

    override fun toString(): String {
        return repr
    }
}

class Coord(val x: Float, val y: Float, val z: Float) {
    constructor(x: Int, y: Int, z: Int) : this(x.toFloat(), y.toFloat(), z.toFloat())

    operator fun plus(other: Coord): Coord =
        Coord(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Coord): Coord =
        Coord(x - other.x, y - other.y, z - other.z)

    operator fun times(other: Float): Coord =
        Coord(x * other, y * other, z * other)

    operator fun div(other: Float): Coord =
        Coord(x / other, y / other, z / other)

    override fun toString(): String {
        return "${x.roundToInt()} ${y.roundToInt()} ${z.roundToInt()}"
    }
}