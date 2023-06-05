package com.zacklukem.mcdsl


open class CommandBuilder : BasicCommandBuilder<Unit, CommandBuilder>() {
    open fun cmd(cmd: String) {
        addCommand(Unit, cmd)
    }

    fun cmd(cmds: List<String>) {
        cmds.forEach {
            this.cmd(it)
        }
    }

    override fun makeSelf(): CommandBuilder = CommandBuilder()
}