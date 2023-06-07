package com.zacklukem.mcdsl

import com.zacklukem.mcdsl.util.makeRich
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RichTextTest {
    @Test
    fun richTextSimple() {
        val text = makeRich("Hello, @red{world}!")
        assertEquals("""[{"text":"Hello, "},{"text":"world","color":"red"},{"text":"!"}]""", text)
        val text2 = makeRich("Hello, @red{world}")
        assertEquals("""[{"text":"Hello, "},{"text":"world","color":"red"}]""", text2)
    }

    @Test
    fun richTextNested() {
        val text = makeRich("Hello, @red{@underline{rld}}!")
        assertEquals(
            """[{"text":"Hello, "},{"text":"rld","color":"red","underline":true},{"text":"!"}]""",
            text
        )
        val text2 = makeRich("Hello, @red{@blue{rld}}!")
        assertEquals(
            """[{"text":"Hello, "},{"text":"rld","color":"red"},{"text":"!"}]""",
            text2
        )
        val text3 = makeRich("Hello, @red{asdf@underline{r\\}ld}asdf}!")
        assertEquals(
            """[{"text":"Hello, "},{"text":"asdf","color":"red"},{"text":"r}ld","color":"red","underline":true},{"text":"asdf","color":"red"},{"text":"!"}]""",
            text3
        )
    }
}