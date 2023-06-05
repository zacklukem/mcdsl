package mcdsl

enum class CommandBlockKind {
    IMPULSE,
    REPEAT
}

open class CommandBlockBuilder {
    val commands: MutableList<Pair<CommandBlockKind, String>> = mutableListOf()

    fun executeIf(cond: Condition, c: CommandBlockBuilder.() -> Unit) {
        val builder = CommandBlockBuilder()
        c(builder)
        val solved = cond.solve()
        for (s in solved) {
            for (cmd in builder.commands) {
                commands.add(Pair(cmd.first, "execute $s run ${cmd.second}"))
            }
        }
    }

    open fun impulse(cmd: String) {
        commands.add(Pair(CommandBlockKind.IMPULSE, cmd))
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
}