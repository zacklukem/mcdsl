# MCDSL

Minecraft Datapack Scripting Language or Minecraft Domain Specific Language or whatever you want

A Kotlin DSL for creating Minecraft Datapacks

## Install

build.gradle.kts:
```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.zacklukem:mcdsl:main-SNAPSHOT")
}
```

## Example

```kotlin
val pack = datapack {
    name = "My Pack"
    description = "A cool datapack"
    packFormat = 12
}

val ns = pack.namespace("my_pack")
val pos = Coord(123, 456, 789)
val myVar = ns.varInt("my_var")
val myLever = Lever(pos + Coord(10, 0, 0), Dir.NORTH)

val func = ns.function {
    cmd("say Hello Function!")
    if_(myVar.eq(5) and myLever.isOn()) {
        cmd("say myVar is 5!")
    }
}

ns.onLoad {
    cmd("say Welcome to my datapack!")
    cmd("say This datapack was made with mcdsl!")
    cmd(myVar.set(5))
    for (i in 0..10) {
        schedule_(secs(i)) {
            cmd("say ${10 - i}")
        }
    }
    schedule_(secs(11)) {
        cmd("Liftoff!")
    }
}

ns.onTick {
    cmd("say SPAM!!!")
    cmd(func.call())
}
```
