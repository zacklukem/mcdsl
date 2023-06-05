package mcdsl


open class CommandBuilder {
    val commands: MutableList<String> = mutableListOf()

    fun executeIf(cond: Condition, c: CommandBuilder.() -> Unit) {
        val builder = CommandBuilder()
        c(builder)
        val solved = cond.solve()
        for (s in solved) {
            for (cmd in builder.commands) {
                commands.add("execute $s run $cmd")
            }
        }
    }

    open fun cmd(cmd: String) {
        commands.add(cmd)
    }

    fun cmd(cmds: List<String>) {
        cmds.forEach {
            this.cmd(it)
        }
    }

}