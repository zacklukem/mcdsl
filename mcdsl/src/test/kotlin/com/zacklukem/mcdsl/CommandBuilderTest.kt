package com.zacklukem.mcdsl

import org.junit.jupiter.api.Test
import kotlin.test.DefaultAsserter.assertEquals

fun builder(c: CommandBuilder.() -> Unit): CommandBuilder {
    val builder = CommandBuilder()
    c(builder)
    return builder
}

class CommandBuilderTest {
    @Test
    fun testCommandAll() {
        val b = builder {
            cmd(listOf("a", "b", "c"))
        }

        assertEquals(
            "valid commands",
            listOf("a", "b", "c"),
            b.commands.map { it.second }
        )
    }

    @Test
    fun testNestedExecutes() {
        val b = builder {
            if_(con("A")) {
                cmd("a")
                if_(con("B")) {
                    cmd("b")
                    as_("C") {
                        cmd("c")
                    }
                    at_("D") {
                        cmd("d")
                    }
                }
            }
            as_("A") {
                cmd("a")
                if_(con("B")) {
                    cmd("b")
                    as_("C") {
                        cmd("c")
                    }
                    at_("D") {
                        cmd("d")
                    }
                }
            }
            at_("A") {
                cmd("a")
                if_(con("B")) {
                    cmd("b")
                    as_("C") {
                        cmd("c")
                    }
                    at_("D") {
                        cmd("d")
                    }
                }
            }
            in_("A") {
                cmd("a")
                if_(con("B")) {
                    cmd("b")
                    as_("C") {
                        cmd("c")
                    }
                    at_("D") {
                        cmd("d")
                    }
                }
            }
        }

        assertEquals(
            "valid commands",
            listOf(
                "execute if A if B as C run c",
                "execute if A if B at D run d",
                "execute if A if B run b",
                "execute if A run a",
                "execute as A if B as C run c",
                "execute as A if B at D run d",
                "execute as A if B run b",
                "execute as A run a",
                "execute at A if B as C run c",
                "execute at A if B at D run d",
                "execute at A if B run b",
                "execute at A run a",
                "execute in A if B as C run c",
                "execute in A if B at D run d",
                "execute in A if B run b",
                "execute in A run a",
            ),
            b.commands.map { it.second }
        )
    }
}