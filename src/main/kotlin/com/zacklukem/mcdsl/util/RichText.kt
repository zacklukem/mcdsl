@file:Suppress("NAME_SHADOWING")

package com.zacklukem.mcdsl.util

/**
 * Rich text builder
 *
 * Outputs a minecraft formatted text array with the form `[{"text":"...","color":"...",...}, ...]`
 *
 * Formatting is applied using the format `@<format>{text}` where `<format>` is one of the following:
 * - a plaintext color (`red`, `green`, `dark_red`, etc.)
 *      (see: [https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes](https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes))
 * - a hex color (`#ffffff`, `#AbCdEf`, etc.)
 * - a formatting code (`bold`, `italic`, `obfuscated`, etc.)
 *      (see: [https://minecraft.fandom.com/wiki/Formatting_codes#Formatting_codes](https://minecraft.fandom.com/wiki/Formatting_codes#Formatting_codes))
 *
 * These formats can be nested like: `@red{@bold{asdf}}`.
 *
 * Any `@`, `}`, or `\` characters can be escaped using a backslash (`\`).
 *
 * Nested color attributes or formatting codes are ignored.
 *
 * Examples:
 * ```
 * makeRich("Hello, @red{@blue{HI}} world!") // the @blue is ignored
 * makeRich("Hello, @bold{@bold{HI}} world!") // the second bold is ignored
 * makeRich("Hello, @red{world}!")
 * makeRich("Hello, @bold{@red{world}!}")
 * makeRich("@italic{@#fa2c8b{Hello}, @bold{@red{world}!}}")
 * makeRich("Escaped \\@ symbol and \\\\ backslash, and \\} curly brace")
 * ```
 */
fun makeRich(text: String): String {
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
        val added = mutableSetOf<String>()
        for (mod in modifiers) {
            out += if (isColor(mod)) {
                if ("color" in added) continue
                added.add("color")
                ",\"color\":\"$mod\""
            } else if (mod == "bold" || mod == "italic" || mod == "underline" || mod == "strikethrough" || mod == "obfuscated") {
                if (mod in added) continue
                added.add(mod)
                ",\"$mod\":true"
            } else {
                throw Exception("Unknown modifier: $mod")
            }
        }
        out += "}"
        out
    } + "]"
}
