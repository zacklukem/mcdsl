package com.zacklukem.mcdsl.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Minecraft advancements
 *
 * @see com.zacklukem.mcdsl.Namespace.advancement
 */
@Serializable
data class Advancement(
    val display: Display? = null,
    val parent: String? = null,
    val criteria: Map<String, Criterion>,
    val requirements: List<List<String>> = listOf(),
    val rewards: Rewards? = null,
) {
    @Serializable
    data class Criterion(
        val trigger: String,
        val conditions: JsonObject? = null,
    )

    @Serializable
    data class Display(
        val icon: Icon,
        val title: String,
        val description: String,
        val frame: String = "task",
        val background: String? = null,
        @SerialName("show_toast")
        val showToast: Boolean = true,
        @SerialName("announce_to_chat")
        val announceToChat: Boolean = true,
        val hidden: Boolean = false,
    ) {
        @Serializable
        data class Icon(
            val item: String,
            val nbt: String? = null,
        )
    }

    @Serializable
    data class Rewards(
        val recipes: List<String> = listOf(),
        val loot: List<String> = listOf(),
        val experience: Int = 0,
        val function: String? = null,
    )
}
