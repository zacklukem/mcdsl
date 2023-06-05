package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Coord
import com.zacklukem.mcdsl.util.Dir
import com.zacklukem.mcdsl.util.Condition
import com.zacklukem.mcdsl.util.con

class Lever(private val pos: Coord, private val dir: Dir) {
    fun isOn(): Condition {
        return con("block $pos minecraft:lever[powered=true]")
    }

    fun isOff(): Condition {
        return con("block $pos minecraft:lever[powered=false]")
    }

    fun setOn(): String {
        return set(true)
    }

    fun setOff(): String {
        return set(false)
    }

    private fun set(on: Boolean): String {
        return "setblock $pos minecraft:lever[facing=$dir,powered=$on]"
    }
}
