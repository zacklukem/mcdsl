@file:Suppress("NAME_SHADOWING")

package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Coord

fun rich(text: String): String {
    val iter = text.iterator()

    val getNext = {
        if (!iter.hasNext()) {
            null
        } else {
            iter.next()
        }
    }

    fun isColor(s: String): Boolean {
        return when (s) {
            "dark_blue", "dark_green", "dark_aqua", "dark_pink", "dark_purple",
            "dark_red", "gray", "dark_yellow", "blue", "green", "aqua", "pink",
            "purple", "red", "white", "yellow", "black" -> true
            else -> s.matches(Regex("#(\\p{XDigit}){6}"))
        }
    }

    val stack = mutableListOf<String>()
    val segments = mutableListOf<Pair<String, List<String>>>()

    var out = ""

    var c = getNext()
    while (c != null) {
        when (c) {
            '@' -> {
                var transform = ""
                var c: Char? = getNext() ?: throw Exception("Unexpected end of string")
                while (c != '{') {
                    transform += c
                    c = getNext() ?: throw Exception("Unexpected end of string")
                }
                if (out != "") {
                    segments.add(Pair(out, stack.map { it }))
                }
                stack.add(transform)
                out = ""
            }
            '}' -> {
                if (out != "") {
                    segments.add(Pair(out, stack.map { it }))
                }
                stack.removeLast()
                out = ""
            }
            '\\' -> {
                val c = getNext() ?: throw Exception("Unexpected end of string")
                out += c
            }
            else -> {
                out += c
            }
        }
        c = getNext()
    }

    if (out != "") {
        segments.add(Pair(out, stack.map { it }))
    }

    return "[" + segments.joinToString(",") { (text, modifiers) ->
        var out = "{\"text\":\"$text\""
        for (mod in modifiers) {
            out += if (isColor(mod)) {
                ",\"color\":\"$mod\""
            } else if (mod == "bold" || mod == "italic" || mod == "underline" || mod == "strikethrough" || mod == "obfuscated") {
                ",\"$mod\":true"
            } else {
                throw Exception("Unknown modifier: $mod")
            }
        }
        out += "}"
        out
    } + "]"
}

class Sign(private val pos: Coord) {
    fun setText(text: String): String {
        val lines = text.split('\n').toMutableList()
        assert(lines.size <= 4)
        for (i in lines.size until 4) {
            lines.add("")
        }
        val rlines = lines.map { rich(it) }
        return "data merge block $pos {Text1:'${rlines[0]}',Text2:'${rlines[1]}',Text3:'${rlines[2]}',Text4:'${rlines[3]}'}"
    }
}