package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.Coord
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
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

    private val namespaces = mutableListOf<Namespace>()

    fun namespace(namespace: String, coords: Coord? = null): Namespace {
        val namespace = Namespace(namespace, coords)
        namespaces.add(namespace)
        return namespace
    }

    fun print(path: Path) {
        assert(path.isDirectory())

        val packMcMeta = """
{
   "pack": {
       "pack_format": $packFormat,
       "description": "$description"
   }
}
        """.trimMargin()
        File("$path/pack.mcmeta").writeText(packMcMeta)

        if (icon != null) {
            val iconOut = File("${path.parent}/pack.png")
            iconOut.writeBytes(File(icon).readBytes())
        }

        namespaces.forEach { namespace ->
            namespace.print(Path("${path.parent}/${namespace.namespace}.txt"), path)
        }
    }
}