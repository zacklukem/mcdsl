package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.Condition
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

    private fun addExecute(modifier: String, cmd: String) {
        if (addExecuteCallback != null) {
            addExecuteCallback!!.invoke(modifier, cmd)
        } else {
            cmd("execute $modifier run $cmd")
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
     * This can be nested with [as_], [at_], and [in_] to create more complex conditions
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
        c(builder)
        for (cmd in builder.commands) {
            for (s in solved) {
                addExecute(s, cmd)
            }
        }
        return ElseBuilder(this, cond)
    }

    /**
     * Creates one or more execute commands running as the given entity
     *
     * This can be nested with [if_], [at_], and [in_] to create more complex conditions
     *
     * Example:
     * ```
     * as_("@p") {
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
    fun as_(_as: String, c: CommandBuilder.() -> Unit) {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("as $_as $modifier", cmd)
        }
        c(builder)
        for (cmd in builder.commands) {
            addExecute("as $_as", cmd)
        }
    }

    /**
     * Creates one or more execute commands running at the position of given entity
     *
     * This can be nested with [as_], [if_], and [in_] to create more complex conditions
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
    fun at_(_at: String, c: CommandBuilder.() -> Unit) {
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("at $_at $modifier", cmd)
        }
        c(builder)
        for (cmd in builder.commands) {
            addExecute("at $_at", cmd)
        }
    }

    /**
     * Creates one or more execute commands running in the given dimension
     *
     * This can be nested with [as_], [at_], and [if_] to create more complex conditions
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
        val builder = CommandBuilder(ns)
        builder.addExecuteCallback = { modifier, cmd ->
            addExecute("in $_in $modifier", cmd)
        }
        c(builder)
        for (cmd in builder.commands) {
            addExecute("in $_in", cmd)
        }
    }

    /**
     * Creates a new command
     */
    fun cmd(cmd: String) {
        commands.add(cmd)
    }

    /**
     * Creates multiple commands
     */
    fun cmd(cmds: List<String>) {
        cmds.forEach {
            this.cmd(it)
        }
    }
}