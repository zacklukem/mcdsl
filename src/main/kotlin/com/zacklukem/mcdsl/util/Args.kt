package com.zacklukem.mcdsl.util

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Item(
    val item: String,
    val nbt: String? = null,
)

/**
 * Minecraft entity anchors (feet, eyes)
 */
enum class EntityAnchor {
    FEET,
    EYES;

    override fun toString(): String {
        return name.lowercase()
    }
}

/**
 * Minecraft heightmap arguments
 */
enum class HeightmapArg {
    WORLD_SURFACE, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR;

    override fun toString(): String {
        return name.lowercase()
    }
}

/**
 * Minecraft relation arguments
 *
 * @see com.zacklukem.mcdsl.CommandBuilder.on
 */
enum class RelationArg {
    ATTACKER, CONTROLLER, LEASHER, ORIGIN, OWNER, PASSENGERS, TARGET, VEHICLE;

    override fun toString(): String {
        return name.lowercase()
    }
}


/**
 * A Minecraft entity argument
 */
interface EntityArg {
    override fun toString(): String
}

/**
 * A Minecraft player name
 */
data class PlayerName(val name: String) : EntityArg {
    override fun toString(): String {
        return name
    }
}

/**
 * A Minecraft player uuid
 */
data class PlayerUUID(val id: UUID) : EntityArg {
    override fun toString(): String {
        return id.toString()
    }
}

/**
 * Minecraft selectors (@a, @p, ...)
 */
enum class Selector : EntityArg {
    /** \@p */
    P,

    /** \@r */
    R,

    /** \@a */
    A,

    /** \@e */
    E,

    /** \@s */
    S;

    private val arguments = mutableMapOf<String, String>()

    fun arg(key: String, value: String): Selector {
        arguments[key] = value
        return this
    }

    override fun toString(): String {
        val selector = when (this) {
            P -> "p"
            R -> "r"
            A -> "a"
            E -> "e"
            S -> "s"
        }
        val args = arguments.map { (k, v) -> "$k=$v" }.joinToString(",")
        return if (arguments.isNotEmpty()) {
            "@$selector[$args]"
        } else {
            "@$selector"
        }
    }
}
