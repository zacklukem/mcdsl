package com.zacklukem.mcdsl

data class Func(val commands: List<String>, val name: String, val namespace: String)

fun Func.call(): String {
    return "function $namespace:$name"
}