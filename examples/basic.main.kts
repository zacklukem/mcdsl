@file:DependsOn("com.zacklukem:mcdsl:0.1.0-SNAPSHOT")

import com.zacklukem.mcdsl.*
import com.zacklukem.mcdsl.data.*;

val pack = datapack {}

val ns = pack.namespace("test")

ns.onLoad {
    cmd("say Hello World!")
}

ns.advancement("my_advancement", Advancement(
    display = Advancement.Display(
        title = "My Advancement",
        description = "This is my advancement",
        icon = Advancement.Display.Icon("minecraft:stone"),
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

pack.print("out/basic")
