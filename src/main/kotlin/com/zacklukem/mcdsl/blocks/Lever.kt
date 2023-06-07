package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Coord
import com.zacklukem.mcdsl.util.Dir
import com.zacklukem.mcdsl.util.Condition
import com.zacklukem.mcdsl.util.con

/**
 * Represents a lever in the world
 */
class Lever(private val pos: Coord, private val dir: Dir) {
    /**
     * A condition that is true when the lever is on
     */
    fun isOn(): Condition {
        return con("block $pos minecraft:lever[powered=true]")
    }

    /**
     * A condition that is true when the lever is off
     */
    fun isOff(): Condition {
        return con("block $pos minecraft:lever[powered=false]")
    }

    /**
     * A command that sets the lever to on
     */
    fun setOn(): String {
        return set(true)
    }

    /**
     * A command that sets the lever to off
     */
    fun setOff(): String {
        return set(false)
    }

    /**
     * A command that sets the lever to the given state (true = on, false = off)
     */
    private fun set(on: Boolean): String {
        return "setblock $pos minecraft:lever[facing=$dir,powered=$on]"
    }
}
