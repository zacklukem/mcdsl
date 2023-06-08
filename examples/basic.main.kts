@file:DependsOn("com.zacklukem:mcdsl:0.1.0-SNAPSHOT")

import com.zacklukem.mcdsl.*
import com.zacklukem.mcdsl.data.*;
import com.zacklukem.mcdsl.util.*;

val pack = datapack {}

val ns = pack.namespace("test")

ns.onLoad {
    cmd("say Hello World!")
}

ns.advancement("my_advancement", Advancement(
    display = Advancement.Display(
        title = "My Advancement",
        description = "This is my advancement",
        icon = Item("minecraft:stone"),
        showToast = true,
        announceToChat = true,
        hidden = false,
        background = "minecraft:textures/block/stone.png"
    ),
    parent = "minecraft:recipes/root",
    criteria = mapOf(
        "my_criterion" to Advancement.Criterion(
            trigger = "minecraft:impossible"
        )
    ),
    requirements = listOf(
        listOf("my_criterion")
    )
))

ns.recipe("my_recipe", CraftingShaped(
    pattern = listOf(
        listOf(Item("minecraft:stone"), null, null),
        listOf(Item("minecraft:cobblestone"), Item("minecraft:cobblestone"), Item("minecraft:stone")),
        listOf(Item("minecraft:oak_log"), Item("minecraft:stone"), Item("minecraft:stone")),
    ),
    result = CraftingResult(item = "minecraft:cobblestone")
))

pack.print("out/basic")
