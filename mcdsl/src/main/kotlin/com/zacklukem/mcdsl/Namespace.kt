package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.*
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Namespace(val namespace: String, private val coords: Coord? = null) {
    private val commandBlocks = mutableListOf<Pair<CommandBlockKind, String>>()
    private val functions = mutableListOf<Func>()
    private val triggers = mutableMapOf<String, Trigger>()

    fun commands(c: CommandBlockBuilder.() -> Unit) {
        if (coords == null) {
            throw NoCoordsException()
        }
        val builder = CommandBlockBuilder()
        c(builder)
        commandBlocks.addAll(builder.commands)
    }

    fun trigger(c: Trigger.() -> Unit): Trigger {
        if (coords == null) {
            throw NoCoordsException()
        }
        val id = UUID.randomUUID().toString()
        val trigger = Trigger(id)
        c(trigger)
        triggers[id] = trigger
        return trigger
    }

    fun function(name: String, c: CommandBuilder.() -> Unit): Func {
        val builder = CommandBuilder()
        c(builder)
        val func = Func(builder.commands.map { it.second }, name, namespace);
        functions.add(func)
        return func
    }

    fun function(c: CommandBuilder.() -> Unit): Func {
        return function(UUID.randomUUID().toString(), c)
    }

    fun varInt(name: String = UUID.randomUUID().toString()): VarInt {
        return VarInt(namespace, name)
    }

    fun <T : Discriminant> varEnum(name: String = UUID.randomUUID().toString()): VarEnum<T> {
        return VarEnum<T>(namespace, name)
    }

    fun print(outFile: Path, dataPack: Path) {

        fun repeater(pos: Coord, dir: Dir, delay: Int): String {
            return "setblock $pos minecraft:repeater[facing=$dir,delay=$delay]"
        }

        fun cmdBlk(
            pos: Coord,
            repeat: CommandBlockKind,
            needsRedstone: Boolean,
            cmd: String
        ): String {
            return if (repeat == CommandBlockKind.REPEAT) {
                "setblock $pos minecraft:repeating_command_block{Command:\"$cmd\",auto:${
                    if (needsRedstone) 0 else 1
                }}"
            } else {
                "setblock $pos minecraft:command_block{Command:\"$cmd\",auto:${
                    if (needsRedstone) 0 else 1
                }}"
            }
        }

        var triggerMap: MutableMap<String, Pair<Coord, Coord>>? = null

        if (coords != null) {
            val rootPos = coords
            val out = mutableListOf<String>()

            triggerMap = mutableMapOf()

            var x = 0
            val startX = x
            val startZ = 3
            var endZ = 3

            for ((id, trigger) in triggers.entries) {
                val startX = x

                for ((time, commands) in trigger.times.entries) {
                    for ((repeat, command) in commands) {
                        var t = time
                        var n = 3
                        while (t > 0) {
                            out.add(repeater(rootPos + Coord(n, 0, x), Dir.WEST, Math.min(t, 4)))
                            t -= min(t, 4)
                            n++
                        }
                        endZ = max(endZ, n)
                        out.add(cmdBlk(rootPos + Coord(n, 0, x), repeat, true, command))
                        x++
                    }
                }
                val endX = x
                triggerMap[id] = Pair(rootPos + Coord(2, 0, startX), rootPos + Coord(2, 0, endX))
            }
            val endX = x

            out.add(
                0,
                "fill ${rootPos + Coord(startZ - 1, 0, startX)} ${
                    rootPos + Coord(
                        endZ,
                        0,
                        endX
                    )
                } minecraft:air"
            )

            out.add(
                0, "fill ${rootPos + Coord(startZ, -1, startX)} ${
                    rootPos + Coord(
                        endZ,
                        -1,
                        endX
                    )
                } minecraft:gray_wool"
            )

            x = 0

            for ((repeat, command) in this.commandBlocks) {
                out.add(cmdBlk(rootPos + Coord(0, 0, x), repeat, false, command))
                x++
            }

            for (i in out.indices) {
                out[i] = replaceTriggers(triggerMap, out[i])
            }
            val outFile = File(outFile.toString())

            outFile.writeText(out.joinToString("\n"))
        }

        File("$dataPack/data/$namespace").deleteRecursively()

        File("$dataPack/data/$namespace/functions").mkdirs()

        for (func in functions) {
            val f = File("$dataPack/data/$namespace/functions/${func.name}.mcfunction")
            val s = "# GENERATED\n\n" + func.commands.joinToString("\n") { replaceTriggers(triggerMap, it) }
            f.writeText(s)
        }

    }

    private fun replaceTriggers(triggerMap: Map<String, Pair<Coord, Coord>>?, str: String): String {
        if (triggerMap == null) return str
        var out = ""
        val strIter = str.iterator()

        val inner = inner@{ kind: Char ->
            if (!strIter.hasNext()) return@inner
            var c = strIter.next()
            var id = ""
            while (strIter.hasNext() && c != '<') {
                id += c
                c = strIter.next()
            }
            if (!strIter.hasNext()) return@inner
            c = strIter.next()
            if (c != kind) {
                out += "%$kind>$id<$c"
                return@inner
            }
            if (!strIter.hasNext()) return@inner
            c = strIter.next()
            if (c != '%') {
                out += "%$kind>$id<$kind$c"
                return@inner
            }
            val trig = triggerMap[id] ?: return@inner
            out += if (kind == 't') {
                "fill ${trig.first} ${trig.second} minecraft:redstone_block"
            } else {
                "fill ${trig.first} ${trig.second} minecraft:brown_wool"
            }
        }

        while (strIter.hasNext()) {
            val c = strIter.next()
            if (c == '%') {
                val kind = strIter.next()
                if (kind == 't' || kind == 'r') {
                    val right = strIter.next()
                    if (right == '>') {
                        inner(kind)
                    } else {
                        out += "%$kind$right"
                    }
                } else {
                    out += "%$kind"
                }
            } else {
                out += c
            }
        }
        return out
    }
}

class NoCoordsException : Exception("You must specify root coordinates to add command blocks") {

}
