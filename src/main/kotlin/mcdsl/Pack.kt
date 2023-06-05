package mcdsl

import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Pack(private val namespace: String) {
    private val commandBlocks = mutableListOf<Pair<CommandBlockKind, String>>()
    private val functions = mutableListOf<Func>()
    private val triggers = mutableMapOf<String, Trigger>()

    fun commands(c: CommandBlockBuilder.() -> Unit) {
        val builder = CommandBlockBuilder()
        c(builder)
        commandBlocks.addAll(builder.commands)
    }

    fun function(name: String, c: CommandBuilder.() -> Unit): Func {
        val builder = CommandBuilder()
        c(builder)
        val func = Func(builder.commands, name, namespace);
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

    fun trigger(c: Trigger.() -> Unit): Trigger {
        val id = UUID.randomUUID().toString()
        val trigger = Trigger(id)
        c(trigger)
        triggers[id] = trigger
        return trigger
    }

    fun print(rootPos: Coord, outFile: File) {
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

        val out = mutableListOf<String>()

        val triggerMap = mutableMapOf<String, Pair<Coord, Coord>>()

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

        x = 0;

        for ((repeat, command) in this.commandBlocks) {
            out.add(cmdBlk(rootPos + Coord(0, 0, x), repeat, false, command))
            x++
        }

        for (i in out.indices) {
            out[i] = replaceTriggers(triggerMap, out[i])
        }

        File("./datapack/data/$namespace").deleteRecursively()

        File("./datapack/data/$namespace/functions").mkdirs()

        for (func in functions) {
            val f = File("./datapack/data/$namespace/functions/${func.name}.mcfunction")
            val s = "# GENERATED\n\n" + func.commands.joinToString("\n") { replaceTriggers(triggerMap, it) }
            f.writeText(s)
        }

        outFile.writeText(out.joinToString("\n"))
    }

    private fun replaceTriggers(triggerMap: Map<String, Pair<Coord, Coord>>, str: String): String {
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