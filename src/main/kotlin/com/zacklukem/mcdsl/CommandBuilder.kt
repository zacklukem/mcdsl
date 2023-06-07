package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.*
import java.util.*

class ElseBuilder internal constructor(
    private val builder: CommandBuilder,
    private val cond: Condition
) {
    /**
     * Executes the given commands if the previous 'if' statement was false
     *
     * @see com.zacklukem.mcdsl.CommandBuilder.if_
     */
    @Suppress("FunctionName")
    fun else_(c: CommandBuilder.() -> Unit) {
        builder.if_(!cond, c)
    }
}

/**
 * Formats a string with ticks as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun ticks(ticks: Int): String {
    return "${ticks}t"
}

/**
 * Formats a string with ticks as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun ticks(ticks: Float): String {
    return "${ticks}t"
}

/**
 * Formats a string with seconds as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun secs(secs: Int): String {
    return "${secs}s"
}

/**
 * Formats a string with ticks as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun secs(secs: Float): String {
    return "${secs}s"
}

/**
 * Formats a string with days as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun days(days: Int): String {
    return "${days}d"
}

/**
 * Formats a string with days as units
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.schedule_
 */
fun days(days: Float): String {
    return "${days}d"
}

/**
 * A builder for Minecraft commands.
 *
 * This is used to build up a list of commands that can be executed in a function.
 */
class CommandBuilder internal constructor(private val ns: Namespace) {
    internal val commands: MutableList<String> = mutableListOf()
    private var addExecuteCallback: ((String, String) -> Unit)? = null
    private var addCmdCallback: ((String) -> Unit)? = null

    private fun addExecute(modifier: String, cmd: String) {
        if (addExecuteCallback != null) {
            addExecuteCallback!!.invoke(modifier, cmd)
        } else {
            cmd("execute $modifier run $cmd")
        }
    }

    /**
     * Creates a new command
     */
    fun cmd(cmd: String) {
        if (addCmdCallback != null) {
            addCmdCallback!!.invoke(cmd)
        } else {
            commands.add(cmd)
        }
    }

    /**
     * Creates multiple commands
     */
    fun cmd(cmds: List<String>) {
        cmds.forEach {
            this.cmd(it)
        }
    }

    /**
     * Schedules commands to be run at a specific time
     *
     * Creates a function and returns a schedule command to run it at a specific time
     *
     * Example:
     * ```
     * ns.schedule(ticks(10)) {
     *   cmd("say Hello World!")
     *   cmd("say Hi!")
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.ticks
     * @see com.zacklukem.mcdsl.secs
     * @see com.zacklukem.mcdsl.days
     */
    @Suppress("FunctionName")
    fun schedule_(time: String, c: CommandBuilder.() -> Unit): Func {
        val f = ns.function("schedule_${UUID.randomUUID()}", c)
        return schedule_(time, f)
    }

    /**
     * Schedules a function to be run at a specific time
     *
     * Example:
     * ```
     * val myFunc = ns.function("my_func") { /*...*/ }
     * ns.schedule(secs(10), myFunc)
     * ```
     *
     * @see com.zacklukem.mcdsl.ticks
     * @see com.zacklukem.mcdsl.secs
     * @see com.zacklukem.mcdsl.days
     */
    @Suppress("FunctionName")
    fun schedule_(time: String, f: Func): Func {
        cmd("schedule function ${f.namespace}:${f.name} $time")
        return f
    }

    /**
     * Creates one or more execute commands following the given condition
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * This also returns an else builder that can be used to form an else statement.
     *
     * Example:
     * ```
     * if_(myPressurePlate.isOn() and con("B")) {
     *   cmd("say Hello World!")
     *   as_("@a") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }.else {
     *   cmd("say Goodbye")
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.util.Condition
     */
    @Suppress("FunctionName")
    fun if_(cond: Condition, c: CommandBuilder.() -> Unit): ElseBuilder {
        val solved = cond.solve()
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            for (s in solved) {
                addExecute("$s $modifier", cmd)
            }
        }
        builder.addCmdCallback = { cmd ->
            for (s in solved) {
                addExecute(s, cmd)
            }
        }
        c(builder)
        return ElseBuilder(this, cond)
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.as_
     */
    @Suppress("FunctionName")
    fun as_(_as: String): CommandBuilder {
        return as_(PlayerName(_as))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.as_
     */
    @Suppress("FunctionName")
    fun as_(_as: String, c: CommandBuilder.() -> Unit) {
        as_(PlayerName(_as), c)
    }

    /**
     * Creates one or more execute commands running as the given entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * as_(Selector.P) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun as_(_as: EntityArg, c: CommandBuilder.() -> Unit) {
        c(as_(_as))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.as_
     */
    @Suppress("FunctionName")
    fun as_(_as: EntityArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("as $_as $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("as $_as", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands aligned in to the given axes
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * align_("xyz") {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun align_(_align: String, c: CommandBuilder.() -> Unit) {
        c(align_(_align))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.anchored_
     */
    @Suppress("FunctionName")
    fun align_(_align: String): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("align $_align $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("align $_align", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands anchored to the entity anchor
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * anchored_(EntityAnchor.EYES) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun anchored_(_anchor: EntityAnchor, c: CommandBuilder.() -> Unit) {
        c(anchored_(_anchor))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.anchored_
     */
    @Suppress("FunctionName")
    fun anchored_(_anchor: EntityAnchor): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("anchored $_anchor $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("anchored $_anchor", cmd)
        }
        return builder
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.at_
     */
    @Suppress("FunctionName")
    fun at_(_at: String): CommandBuilder {
        return at_(PlayerName(_at))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.at_
     */
    @Suppress("FunctionName")
    fun at_(_at: String, c: CommandBuilder.() -> Unit) {
        at_(PlayerName(_at), c)
    }

    /**
     * Creates one or more execute commands running at the position of given entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * at_("jeb") {
     *   cmd("say Hello World!")
     *   as_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun at_(_at: EntityArg, c: CommandBuilder.() -> Unit) {
        c(at_(_at))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.at_
     */
    @Suppress("FunctionName")
    fun at_(_at: EntityArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("at $_at $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("at $_at", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands running in the given dimension
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * in_("minecraft:the_end") {
     *   cmd("say Hello World!")
     *   as_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun in_(_in: String, c: CommandBuilder.() -> Unit) {
        c(in_(_in))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.in_
     */
    @Suppress("FunctionName")
    fun in_(_in: String): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("in $_in $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("in $_in", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands facing a given position
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * facing_(Coord(3, 2, 1)) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun facing_(_pos: Coord, c: CommandBuilder.() -> Unit) {
        c(facing_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.facing_
     */
    @Suppress("FunctionName")
    fun facing_(_pos: Coord): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("facing $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("facing $_pos", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands on a relation to the given entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * on_(RelationArg.ATTACKER) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun on_(_rel: RelationArg, c: CommandBuilder.() -> Unit) {
        c(on_(_rel))
    }

    @Suppress("FunctionName")
    fun on_(_rel: RelationArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("on $_rel $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("on $_rel", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands positioned at a given position
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * positioned_(Coord(1, 2, 3)) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun positioned_(_pos: Coord, c: CommandBuilder.() -> Unit) {
        c(positioned_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.positioned_
     */
    @Suppress("FunctionName")
    fun positioned_(_pos: Coord): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("positioned $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("positioned $_pos", cmd)
        }
        return builder
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.positioned_as_
     */
    @Suppress("FunctionName")
    fun positioned_as_(_pos: String): CommandBuilder {
        return positioned_as_(PlayerName(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.positioned_as_
     */
    @Suppress("FunctionName")
    fun positioned_as_(_pos: String, c: CommandBuilder.() -> Unit) {
        positioned_as_(PlayerName(_pos), c)
    }

    /**
     * Creates one or more execute commands positioned as a given entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * positioned_as_("jeb") {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun positioned_as_(_pos: EntityArg, c: CommandBuilder.() -> Unit) {
        c(positioned_as_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.positioned_as_
     */
    @Suppress("FunctionName")
    fun positioned_as_(_pos: EntityArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("positioned as $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("positioned as $_pos", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands positioned over a heightmap
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * positioned_over_(HeightmapArg.WORLD_SURFACE) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun positioned_over_(_pos: HeightmapArg, c: CommandBuilder.() -> Unit) {
        c(positioned_over_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.positioned_over_
     */
    @Suppress("FunctionName")
    fun positioned_over_(_pos: HeightmapArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("positioned over $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("positioned over $_pos", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands rotated at a given rotation
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * rotated_(Rotation(2.3, -170.0)) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun rotated_(_pos: Rotation, c: CommandBuilder.() -> Unit) {
        c(rotated_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.rotated_
     */
    @Suppress("FunctionName")
    fun rotated_(_pos: Rotation): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("rotated $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("rotated $_pos", cmd)
        }
        return builder
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.rotated_as_
     */
    @Suppress("FunctionName")
    fun rotated_as_(_pos: String): CommandBuilder {
        return rotated_as_(PlayerName(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.rotated_as_
     */
    @Suppress("FunctionName")
    fun rotated_as_(_pos: String, c: CommandBuilder.() -> Unit) {
        rotated_as_(PlayerName(_pos), c)
    }

    /**
     * Creates one or more execute commands rotated as a given entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * rotated_as_("jeb") {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun rotated_as_(_pos: EntityArg, c: CommandBuilder.() -> Unit) {
        c(rotated_as_(_pos))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.rotated_as_
     */
    @Suppress("FunctionName")
    fun rotated_as_(_pos: EntityArg): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("rotated as $_pos $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("rotated as $_pos", cmd)
        }
        return builder
    }

    /**
     * Creates one or more execute commands summoning an entity and executing a command as that entity
     *
     * This can be nested with [if_], [as_], [align_], [anchored_], [at_], [in_], [facing_], [on_], [positioned_],
     * [positioned_as_], [positioned_over_], [rotated_], [rotated_as_] and [summon_] to create more complex conditions
     *
     * Example:
     * ```
     * summon_(Rotation(2.3, -170.0)) {
     *   cmd("say Hello World!")
     *   at_("notch") {
     *     cmd("say Hi!")
     *   }
     *   if_(con("C")) {
     *     cmd("say C was true too!")
     *   }
     * }
     * ```
     */
    @Suppress("FunctionName")
    fun summon_(_entity: String, c: CommandBuilder.() -> Unit) {
        c(summon_(_entity))
    }

    /**
     * @see com.zacklukem.mcdsl.CommandBuilder.summon_
     */
    @Suppress("FunctionName")
    fun summon_(_entity: String): CommandBuilder {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("summon $_entity $modifier", cmd)
        }
        builder.addCmdCallback = { cmd ->
            addExecute("summon $_entity", cmd)
        }
        return builder
    }
}