package com.zacklukem.mcdsl

class PressurePlate(private val pos: Coord) {
    fun isOn(): Condition {
        return con("block $pos minecraft:polished_blackstone_pressure_plate[powered=true]")
    }

    fun isOff(): Condition {
        return con("block $pos minecraft:polished_blackstone_pressure_plate[powered=false]")
    }
}