package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.blocks.rich
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RichTextTest {
    @Test
    fun richTextSimple() {
        val text = rich("Hello, @red{world}!")
        assertEquals("""[{"text":"Hello, "},{"text":"world","color":"red"},{"text":"!"}]""", text)
        val text2 = rich("Hello, @red{world}")
        assertEquals("""[{"text":"Hello, "},{"text":"world","color":"red"}]""", text2)
    }

    @Test
    fun richTextNested() {
        val text = rich("Hello, @red{@underline{rld}}!")
        assertEquals(
            """[{"text":"Hello, "},{"text":"rld","color":"red","underline":true},{"text":"!"}]""",
            text
        )
        val text2 = rich("Hello, @red{asdf@underline{r\\}ld}asdf}!")
        assertEquals(
            """[{"text":"Hello, "},{"text":"asdf","color":"red"},{"text":"r}ld","color":"red","underline":true},{"text":"asdf","color":"red"},{"text":"!"}]""",
            text2
        )
    }
}