package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.data.Advancement
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

/**
 * Create a new datapack
 *
 * Example:
 * ```
 * val dp = datapack {
 *   name = "My Datapack"
 *   description = "A datapack made with mcdsl"
 *   packFormat = 12 // Defaults to 12
 *   icon = "path/to/icon.png"
 * }
 * ```
 * @see com.zacklukem.mcdsl.Datapack
 */
fun datapack(c: Datapack.() -> Unit): Datapack {
    val datapack = Datapack()
    c(datapack)
    return datapack
}

/**
 * The main class for a datapack. Contains all namespaces and pack metadata.
 *
 * Created using the [datapack] builder function
 *
 * Example:
 * ```
 * val dp = datapack {
 *   name = "My Datapack"
 *   description = "A datapack made with mcdsl"
 *   packFormat = 12 // Defaults to 12
 *   icon = "path/to/icon.png"
 * }
 * ```
 *
 * @see com.zacklukem.mcdsl.datapack
 */
class Datapack internal constructor() {
    /** The name of the datapack */
    var name = "datapack"

    /** The datapack description */
    var description = "A datapack made with mcdsl"

    /** The datapack pack format */
    var packFormat = 12

    /** The datapack icon */
    var icon: String? = null

    private val onLoad = mutableListOf<Func>()
    private val onTick = mutableListOf<Func>()

    internal fun onLoad(f: Func) {
        onLoad.add(f)
    }

    internal fun onTick(f: Func) {
        onTick.add(f)
    }

    private val namespaces = mutableListOf<Namespace>()

    /**
     * Create a new namespace in this datapack
     *
     * Example:
     * ```
     * val dp = datapack { /* ... */ }
     * val ns = dp.namespace("test")
     * ns.onLoad {
     *   say("Hello World!")
     * }
     * ```
     *
     * @see com.zacklukem.mcdsl.Namespace
     */
    fun namespace(namespace: String): Namespace {
        val ns = Namespace(this, namespace)
        namespaces.add(ns)
        return ns
    }

    /**
     * Outputs the datapack to the given path
     *
     * Called last, this generates the datapack files and writes them to the given path
     *
     * Anything at the given path will be deleted before the datapack is generated
     */
    fun print(path: String) {
        print(Path(path))
    }

    /**
     * Outputs the datapack to the given path
     *
     * Called last, this generates the datapack files and writes them to the given path
     *
     * Anything at the given path will be deleted before the datapack is generated
     */
    fun print(path: Path) {
        assert(path.isDirectory())
        val f = File(path.toString())
        f.deleteRecursively()
        f.mkdirs()


        val packMcMeta = """
{
   "pack": {
       "pack_format": $packFormat,
       "description": "$description"
   }
}
        """.trimMargin()
        File("$path/pack.mcmeta").writeText(packMcMeta)

        icon?.let { icon ->
            val iconOut = File("${path.parent}/pack.png")
            iconOut.writeBytes(File(icon).readBytes())
        }

        namespaces.forEach { namespace ->
            namespace.print(path)
        }

        File("$path/data/minecraft/tags/functions").mkdirs()
        File("$path/data/minecraft/tags/functions/tick.json").writeText(
            """{"values": [${onTick.joinToString(", ") { "\"${it.namespace}:${it.name}\"" }}]}"""
        )
        File("$path/data/minecraft/tags/functions/load.json").writeText(
            """{"values": [${onLoad.joinToString(", ") { "\"${it.namespace}:${it.name}\"" }}]}"""
        )
    }
}