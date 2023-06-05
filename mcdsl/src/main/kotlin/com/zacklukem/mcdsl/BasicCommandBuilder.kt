package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.Condition

class ElseBuilder<Marker, Builder : BasicCommandBuilder<Marker, Builder>>(
    private val builder: Builder,
    private val cond: Condition
) {
    fun else_(c: Builder.() -> Unit) {
        builder.if_(!cond, c)
    }
}

abstract class BasicCommandBuilder<Marker, Builder : BasicCommandBuilder<Marker, Builder>> {
    val commands: MutableList<Pair<Marker, String>> = mutableListOf()
    private var addExecuteCallback: ((Marker, String, String) -> Unit)? = null

    protected abstract fun makeSelf(): Builder

    private fun addExecute(marker: Marker, modifier: String, cmd: String) {
        if (addExecuteCallback != null) {
            addExecuteCallback!!.invoke(marker, modifier, cmd)
        } else {
            addCommand(marker, "execute $modifier run $cmd")
        }
    }


    fun if_(cond: Condition, c: Builder.() -> Unit): ElseBuilder<Marker, Builder> {
        val solved = cond.solve()
        val builder = makeSelf()
        builder.addExecuteCallback = { marker, modifier, cmd ->
            for (s in solved) {
                addExecute(marker, "$s $modifier", cmd)
            }
        }
        c(builder)
        for ((marker, cmd) in builder.commands) {
            for (s in solved) {
                addExecute(marker, s, cmd)
            }
        }
        return ElseBuilder(this as Builder, cond)
    }

    fun as_(_as: String, c: Builder.() -> Unit) {
        val builder = makeSelf()
        builder.addExecuteCallback = { marker, modifier, cmd ->
            addExecute(marker, "as $_as $modifier", cmd)
        }
        c(builder)
        for ((marker, cmd) in builder.commands) {
            addExecute(marker, "as $_as", cmd)
        }
    }

    fun at_(_at: String, c: Builder.() -> Unit) {
        val builder = makeSelf()
        builder.addExecuteCallback = { marker, modifier, cmd ->
            addExecute(marker, "at $_at $modifier", cmd)
        }
        c(builder)
        for ((marker, cmd) in builder.commands) {
            addExecute(marker, "at $_at", cmd)
        }
    }

    fun in_(_in: String, c: Builder.() -> Unit) {
        val builder = makeSelf()
        builder.addExecuteCallback = { marker, modifier, cmd ->
            addExecute(marker, "in $_in $modifier", cmd)
        }
        c(builder)
        for ((marker, cmd) in builder.commands) {
            addExecute(marker, "in $_in", cmd)
        }
    }

    protected fun addCommand(marker: Marker, cmd: String) {
        commands.add(Pair(marker, cmd))
    }
}