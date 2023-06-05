package com.zacklukem.mcdsl

enum class CommandBlockKind {
    IMPULSE,
    REPEAT
}

open class CommandBlockBuilder : BasicCommandBuilder<CommandBlockKind, CommandBlockBuilder>() {
    open fun impulse(cmd: String) {
        addCommand(CommandBlockKind.IMPULSE, cmd)
    }

    fun impulse(cmds: List<String>) {
        cmds.forEach {
            this.impulse(it)
        }
    }

    open fun repeat(cmd: String) {
        commands.add(Pair(CommandBlockKind.REPEAT, cmd))
    }

    open fun repeat(pack: Namespace, cmd: CommandBuilder.() -> Unit) {
        repeat(pack.function(cmd).call())
    }

    fun repeat(cmds: List<String>) {
        cmds.forEach {
            this.repeat(it)
        }
    }

    override fun makeSelf(): CommandBlockBuilder = CommandBlockBuilder()
}