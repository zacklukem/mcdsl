package com.zacklukem.mcdsl.util

import com.zacklukem.mcdsl.CommandBuilder


class VarInt(val namespace: String, val name: String): CommandBuilder.Storable {
    fun set(value: Int): String {
        return "scoreboard players set $name $namespace $value"
    }

    infix fun eq(value: Int): Condition {
        return con("score $name $namespace matches $value")
    }

    override fun executeStore(): String {
        return "score $name $namespace"
    }
}

interface Discriminant {
    fun discriminant(): Int
}

class VarEnum<T : Discriminant>(val namespace: String, val name: String): CommandBuilder.Storable {
    fun set(value: T): String {
        return "scoreboard players set $name $namespace ${value.discriminant()}"
    }

    infix fun eq(value: T): Condition {
        return con("score $name $namespace matches ${value.discriminant()}")
    }

    fun oneOf(vararg value: T): OrCondition {
        return OrCondition(value.map { eq(it) }.toMutableList())
    }

    fun init(): String {
        return "scoreboard objectives add $name dummy"
    }

    override fun executeStore(): String {
        return "score $name $namespace"
    }
}