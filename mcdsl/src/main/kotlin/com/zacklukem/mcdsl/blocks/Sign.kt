package com.zacklukem.mcdsl.blocks

import com.zacklukem.mcdsl.util.Coord

class Sign(private val pos: Coord) {
    fun setText(text: String): String {
        val lines = text.split('\n').toMutableList()
        assert(lines.size <= 4)
        for (i in lines.size until 4) {
            lines.add("")
        }
        return "data merge block $pos {Text1:'{\"text\":\"${lines[0]}\"}',Text2:'{\"text\":\"${lines[1]}\"}',Text3:'{\"text\":\"${lines[2]}\"}',Text4:'{\"text\":\"${lines[3]}\"}'}"
    }
}