package mcdsl

import kotlin.math.roundToInt

class Trigger(private val id: String) {
    val times: MutableMap<Int, MutableList<Pair<CommandBlockKind, String>>> = mutableMapOf()


    fun at(time: Float, c: CommandBlockBuilder.() -> Unit) {
        at(time.roundToInt(), c)
    }

    fun at(time: Int, c: CommandBlockBuilder.() -> Unit) {
        val builder = CommandBlockBuilder()
        c(builder)
        if (times.containsKey(time)) {
            times[time]!!.addAll(builder.commands)
        } else {
            times[time] = builder.commands
        }
    }

    fun trigger(): String {
        return "%t>$id<t%"
    }

    fun reset(): String {
        return "%r>$id<r%"
    }
}