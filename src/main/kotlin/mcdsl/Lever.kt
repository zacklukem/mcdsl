package mcdsl

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
