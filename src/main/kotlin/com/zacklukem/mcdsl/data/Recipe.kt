package com.zacklukem.mcdsl.data

import com.zacklukem.mcdsl.util.Item
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A minecraft recipe
 */
@Serializable
sealed interface Recipe

@Serializable
data class Cooking(
    @Required val type: String,
    val group: String? = null,
    val ingredient: List<Item>,
    val result: String,
    val experience: Float = 0.0f,
    val cookingtime: Int? = null,
): Recipe

fun Blasting(
    group: String? = null,
    ingredient: List<Item>,
    result: String,
    experience: Float = 0.0f,
    cookingtime: Int? = null,
): Cooking = Cooking("minecraft:blasting", group, ingredient, result, experience, cookingtime)

fun CampfireCooking(
    group: String? = null,
    ingredient: List<Item>,
    result: String,
    experience: Float = 0.0f,
    cookingtime: Int? = null,
): Cooking = Cooking("minecraft:campfire_cooking", group, ingredient, result, experience, cookingtime)

fun Smelting(
    group: String? = null,
    ingredient: List<Item>,
    result: String,
    experience: Float = 0.0f,
    cookingtime: Int? = null,
): Cooking = Cooking("minecraft:smelting", group, ingredient, result, experience, cookingtime)

fun Smoking(
    group: String? = null,
    ingredient: List<Item>,
    result: String,
    experience: Float = 0.0f,
    cookingtime: Int? = null,
): Cooking = Cooking("minecraft:smoking", group, ingredient, result, experience, cookingtime)

@Serializable
data class CraftingResult(
    @Required val count: Int = 1,
    val item: String,
)

@Serializable
data class CraftingShaped(
    @Required val type: String = "minecraft:crafting_shaped",
    val group: String? = null,
    val pattern: List<String>,
    val key: Map<String, List<Item>>,
    val result: CraftingResult,
): Recipe {
    init {
        require(pattern.size == 3) { "Pattern must be 3 lines" }
        require(pattern.all { it.length == 3 }) { "Pattern lines must be 3 characters long" }
    }
}

fun CraftingShaped(
    group: String? = null,
    pattern: List<List<Item?>>,
    result: CraftingResult,
): CraftingShaped {
    val key = mutableMapOf<String, List<Item>>()
    val reverseKey = mutableMapOf<Item, String>()
    var nextChar = 'a'
    for (item in pattern.flatten().filterNotNull().distinct()) {
        key["$nextChar"] = listOf(item)
        reverseKey[item] = "$nextChar"
        nextChar++
    }

    val stringPattern = pattern.map { row ->
        row.joinToString("") {
            if (it != null) reverseKey[it]!! else " "
        }
    }

    return CraftingShaped(
        group = group,
        pattern = stringPattern,
        key = key,
        result = result,
    )
}

@Serializable
data class CraftingShapeless(
    @Required val type: String = "minecraft:crafting_shapeless",
    val group: String? = null,
    val ingredients: List<List<Item>>,
    val result: CraftingResult,
): Recipe

@Serializable
data class CraftingSpecial(val type: String) : Recipe

@Serializable
data class Smithing(
    @Required val type: String = "minecraft:smithing",
    val group: String? = null,
    val base: Item,
    val addition: Item,
    val result: Item,
): Recipe
