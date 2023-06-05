package com.zacklukem.mcdsl

class VarInt(val namespace: String, val name: String) {
    fun set(value: Int): String {
        return "scoreboard players set $name $namespace $value"
    }

    fun eq(value: Int): Condition {
        return con("score $name $namespace matches $value")
    }
}

interface Discriminant {
    fun discriminant(): Int
}

class VarEnum<T : Discriminant>(val namespace: String, val name: String) {
    fun set(value: T): String {
        return "scoreboard players set $name $namespace ${value.discriminant()}"
    }

    fun eq(value: T): Condition {
        return con("score $name $namespace matches ${value.discriminant()}")
    }

    fun oneOf(vararg value: T): OrCondition {
        return OrCondition(value.map { eq(it) }.toMutableList())
    }
}