package com.zacklukem.mcdsl

/**
 * Represents a function in a datapack
 *
 * Create using [Namespace.function]
 *
 * @see com.zacklukem.mcdsl.Namespace.function
 */
data class Func internal constructor(val commands: List<String>, val name: String, val namespace: String)

/**
 * Calls this function
 */
fun Func.call(): String {
    return "function $namespace:$name"
}