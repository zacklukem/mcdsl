@file:Suppress("NAME_SHADOWING")

package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Coord
import com.zacklukem.mcdsl.util.makeRich


/**
 * Helper commands for minecraft signs
 */
class Sign(private val pos: Coord) {
    /**
     * Sets the text on the sign
     *
     * The input is split on newlines, and the sign is filled from top to bottom
     *
     * If the input text contains more than four lines, an [IllegalArgumentException] is thrown.
     *
     * The input text is converted to a rich text format using the [makeRich] function. See the documentation for that
     * function for more information.
     *
     * Example:
     * ```
     * sign.setText("\nHello\n@red{@bold{World}}!")
     * ```
     *
     * @see com.zacklukem.mcdsl.util.makeRich
     */
    fun setText(text: String): String {
        val lines = text.split('\n').toMutableList()
        if (lines.size > 4) {
            throw IllegalArgumentException("Sign text must be 4 lines or less")
        }
        for (i in lines.size until 4) {
            lines.add("")
        }
        val rLines = lines.map { makeRich(it) }
        return "data merge block $pos {Text1:'${rLines[0]}',Text2:'${rLines[1]}',Text3:'${rLines[2]}',Text4:'${rLines[3]}'}"
    }
}