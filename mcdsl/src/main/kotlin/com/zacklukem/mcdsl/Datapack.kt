package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.Coord
import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory

fun datapack(c: Datapack.() -> Unit): Datapack {
    val datapack = Datapack()
    c(datapack)
    return datapack
}

class Datapack {
    // properties
    var name = "datapack"
    var description = "A datapack made with mcdsl"
    var packFormat = 12
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

    fun namespace(namespace: String, coords: Coord? = null): Namespace {
        val ns = Namespace(this, namespace, coords)
        namespaces.add(ns)
        return ns
    }

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