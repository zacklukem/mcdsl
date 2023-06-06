package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Condition
import com.zacklukem.mcdsl.util.Coord
import com.zacklukem.mcdsl.util.con

class PressurePlate(private val pos: Coord) {
    fun isOn(): Condition {
        return con("block $pos #minecraft:pressure_plates[powered=true]")
    }

    fun isOff(): Condition {
        return con("block $pos #minecraft:pressure_plates[powered=false]")
    }
}