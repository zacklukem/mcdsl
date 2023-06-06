package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.Condition
import java.util.*

class ElseBuilder(
    private val builder: CommandBuilder,
    private val cond: Condition
) {
    fun else_(c: CommandBuilder.() -> Unit) {
        builder.if_(!cond, c)
    }
}

class CommandBuilder(val ns: Namespace) {
    val commands: MutableList<String> = mutableListOf()
    private var addExecuteCallback: ((String, String) -> Unit)? = null

    private fun addExecute(modifier: String, cmd: String) {
        if (addExecuteCallback != null) {
            addExecuteCallback!!.invoke(modifier, cmd)
        } else {
            cmd("execute $modifier run $cmd")
        }
    }

    fun ticks(ticks: Int): String {
        return "${ticks}t"
    }

    fun ticks(ticks: Float): String {
        return "${ticks}t"
    }

    fun schedule_(time: String, c: CommandBuilder.() -> Unit): Func {
        val f = ns.function("schedule_${UUID.randomUUID()}", c)
        cmd("schedule function ${f.namespace}:${f.name} $time")
        return f
    }

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

    fun cmd(cmd: String) {
        commands.add(cmd)
    }

    fun cmd(cmds: List<String>) {
        cmds.forEach {
            this.cmd(it)
        }
    }
}