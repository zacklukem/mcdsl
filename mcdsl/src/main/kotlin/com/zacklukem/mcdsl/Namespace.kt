package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.commands.Bossbar
import com.zacklukem.mcdsl.util.*
import java.io.File
import java.nio.file.Path
import java.util.*

/**
 * A minecraft namespace where functions, tags, and other datapack content is stored
 *
 * Created using [Datapack.namespace]
 *
 * Example:
 * ```
 * val dp = datapack { /* ... */ }
 * val ns = dp.namespace("test")
 * ns.onLoad {
 *   say("Hello World!")
 * }
 * ```
 */
class Namespace internal constructor(private val parent: Datapack, val namespace: String) {
    private val functions = mutableListOf<Func>()

    /**
     * Create a new function that runs when the datapack is loaded
     *
     * Example:
     * ```
     * namespace.onLoad {
     *   cmd("say Hello World!")
     * }
     * ```
     *
     * The created function will be named `load_<uuid>` where `<uuid>` is a randomly generated uuid
     *
     * A new function is generated each time this function is called, regardless of the namespace
     * @see com.zacklukem.mcdsl.CommandBuilder
     */
    fun onLoad(c: CommandBuilder.() -> Unit) {
        val f = function("load_${UUID.randomUUID()}", c)
        parent.onLoad(f)
    }

    /**
     * Create a new function that runs every tick
     *
     * Example:
     * ```
     * namespace.onTick {
     *   cmd("say TICK")
     * }
     * ```
     *
     * The created function will be named `load_<uuid>` where `<uuid>` is a randomly generated uuid
     *
     * A new function is generated each time this function is called, regardless of the namespace
     * @see com.zacklukem.mcdsl.CommandBuilder
     */
    fun onTick(c: CommandBuilder.() -> Unit) {
        val f = function("tick_${UUID.randomUUID()}", c)
        parent.onTick(f)
    }

    /**
     * Creates a new function with the given name
     *
     * Returns the created function which can be called using [Func.call]
     *
     * Example:
     * ```
     * val func = namespace.function("test") {
     *   cmd("say Hello Func!")
     * }
     *
     * namespace.onLoad {
     *   cmd(func.call()) // generates 'function namespace:test'
     * }
     * ```
     * @see com.zacklukem.mcdsl.Func
     * @see com.zacklukem.mcdsl.CommandBuilder
     */
    fun function(name: String, c: CommandBuilder.() -> Unit): Func {
        val builder = CommandBuilder(this)
        c(builder)
        val func = Func(builder.commands, name, namespace)
        functions.add(func)
        return func
    }

    /**
     * Creates a new function with a random UUID as the name
     *
     * Returns the created function which can be called using [Func.call]
     *
     * Example:
     * ```
     * val func = namespace.function {
     *   cmd("say Hello Func!")
     * }
     *
     * namespace.onLoad {
     *   cmd(func.call()) // generates 'function namespace:<uuid>'
     * }
     * ```
     * @see com.zacklukem.mcdsl.Func
     * @see com.zacklukem.mcdsl.CommandBuilder
     */
    fun function(c: CommandBuilder.() -> Unit): Func {
        return function(UUID.randomUUID().toString(), c)
    }

    /**
     * Creates a new bossbar with the given id, or a random uuid if no id is given
     *
     * Example:
     * ```
     * val bar = namespace.bossbar("test")
     * namespace.onLoad {
     *   cmd(bar.setColor(Color.GREEN))
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.commands.Bossbar
     */
    fun bossbar(id: String = UUID.randomUUID().toString()): Bossbar {
        return Bossbar(namespace, id)
    }

    /**
     * Creates a new scoreboard variable with the given name as player name. If no name is given, a random uuid is used.
     *
     * This variable is used only to store integers.
     *
     * Example:
     * ```
     * val var1 = namespace.varInt("test")
     * namespace.onLoad {
     *   if_(var1.eq(0)) {
     *     cmd("say Hello World!")
     *     cmd(var1.set(1))
     *   }
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.util.VarInt
     */
    fun varInt(name: String = UUID.randomUUID().toString()): VarInt {
        return VarInt(namespace, name)
    }

    /**
     * Creates a new scoreboard variable with the given name as player name. If no name is given, a random uuid is used.
     *
     * This variable is used only to store enum values.
     *
     * The enum type must be a subclass of [Discriminant] which implements the method [Discriminant.discriminant]
     * returning a unique integer for each enum value.
     *
     * Example:
     * ```
     * enum class TestEnum(val value: Int) : Discriminant {
     *   A(0), B(1), C(2);
     *   override fun discriminant(): Int = value
     * }
     *
     * val var1 = namespace.varEnum<TestEnum>("test")
     * namespace.onLoad {
     *   if_(var1.eq(TestEnum.A)) {
     *     cmd(var1.set(TestEnum.B))
     *   }
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.util.VarEnum
     * @see com.zacklukem.mcdsl.util.Discriminant
     * @see com.zacklukem.mcdsl.util.Discriminant.discriminant
     */
    fun <T : Discriminant> varEnum(name: String = UUID.randomUUID().toString()): VarEnum<T> {
        return VarEnum(namespace, name)
    }

    internal fun print(dataPack: Path) {
        File("$dataPack/data/$namespace").deleteRecursively()

        File("$dataPack/data/$namespace/functions").mkdirs()

        for (func in functions) {
            val f = File("$dataPack/data/$namespace/functions/${func.name}.mcfunction")
            val s = "# GENERATED BY MCDSL\n\n" + func.commands.joinToString("\n")
            f.writeText(s)
        }
    }
}
