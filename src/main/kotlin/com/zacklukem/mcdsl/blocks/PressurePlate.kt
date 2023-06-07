package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Condition
import com.zacklukem.mcdsl.util.Coord
import com.zacklukem.mcdsl.util.con

/**
 * Represents a pressure plate in the world
 *
 * @param pos The position of the pressure plate
 */
class PressurePlate(private val pos: Coord) {
    /**
     * A condition that is true when the pressure plate is on
     */
    fun isOn(): Condition {
        return con("block $pos #minecraft:pressure_plates[powered=true]")
    }

    /**
     * A condition that is true when the pressure plate is off
     */
    fun isOff(): Condition {
        return con("block $pos #minecraft:pressure_plates[powered=false]")
    }
}