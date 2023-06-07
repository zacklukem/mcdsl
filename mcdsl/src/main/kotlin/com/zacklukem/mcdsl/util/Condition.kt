package com.zacklukem.mcdsl.util

/**
 * The base class for all conditions.
 *
 * Conditions are an expressive way to simplify complex execute commands. They allow you to apply boolean logic to
 * generate multiple execute commands from a single condition.
 *
 * For example, the following code:
 * ```
 * if_(
 *     (con("A") and (con("B") or !con("C")))
 *     or con("D")
 *     or !(con("E") or !con("F"))
 * ) {
 *   cmd("say hi")
 * }
 * ```
 *
 * Generates the following commands:
 * ```text
 * execute if A if B run say hi
 * execute if A unless C run say hi
 * execute if D run say hi
 * execute unless E if F run say hi
 * ```
 *
 * This allows you to write conditions in an understandable way.
 *
 * The [TermCondition] is generally created with the shorthand `con("...")` and represents a terminal condition, just a
 * basic boolean such as "if ..." or "unless ...". The [TermCondition] generates an "if ..." by default, and generates
 * an "unless ..." when negated.
 *
 * There are also other classes and functions that create [TermCondition]s such as
 * [com.zacklukem.mcdsl.blocks.Lever.isOff].
 *
 * The [AndCondition] and [OrCondition] are created with the `and` and `or` infix functions respectively. These classes
 * represent the boolean logic of "and" and "or" respectively. These classes can be nested to create complex boolean
 * logic.
 *
 * It is highly discouraged to mix the `and` and `or` infix functions. This is because the precedence of the infix
 * function is not the same as the precedence of the boolean logic. Instead, always use parentheses to group your
 * conditions.
 *
 * Basically, don't do this: `A and B or C` instead do this: `(A and B) or C`
 *
 * [AndCondition] and [OrCondition] can both be negated with the `not` function. This just applies De Morgan's law to
 * the condition.
 *
 * @see com.zacklukem.mcdsl.util.con
 */
sealed interface Condition {
    operator fun not(): Condition
    infix fun and(other: Condition): AndCondition
    infix fun or(other: Condition): OrCondition
    fun solve(): List<String>
}

/**
 * A terminal condition ("if ..." or "unless ...")
 *
 * For more information, look at [Condition]
 *
 * @see com.zacklukem.mcdsl.util.Condition
 */
class TermCondition(private val term: String, private val negated: Boolean = false) : Condition {
    override fun not(): TermCondition =
        TermCondition(term, !negated)

    override fun and(other: Condition): AndCondition =
        AndCondition(mutableListOf(this, other))

    override fun or(other: Condition): OrCondition =
        OrCondition(mutableListOf(this, other))

    override fun solve(): List<String> {
        return listOf(if (negated) "unless $term" else "if $term")
    }
}

/**
 * An "and" condition
 *
 * For more information, look at [Condition]
 *
 * @see com.zacklukem.mcdsl.util.Condition
 */
class AndCondition(private val conditions: MutableList<Condition>) : Condition {
    override fun not(): OrCondition =
        OrCondition(conditions.map(Condition::not).toMutableList())

    override fun and(other: Condition): AndCondition {
        conditions.add(other)
        return this
    }

    override fun or(other: Condition): OrCondition =
        OrCondition(mutableListOf(this, other))

    override fun solve(): List<String> {
        assert(conditions.size > 0)

        val iter = conditions.iterator()
        var out = iter.next().solve().toMutableList()
        for (term in iter) {
            val solved = term.solve()
            val newOut = mutableListOf<String>()
            for (s in solved) {
                for (o in out) {
                    newOut.add("$o $s")
                }
            }
            out = newOut
        }

        return out
    }
}

/**
 * An "or" condition
 *
 * @see com.zacklukem.mcdsl.util.Condition
 */
class OrCondition(private val conditions: MutableList<Condition>) : Condition {
    override fun not(): AndCondition =
        AndCondition(conditions.map(Condition::not).toMutableList())

    override fun and(other: Condition): AndCondition =
        AndCondition(mutableListOf(this, other))

    override fun or(other: Condition): OrCondition {
        conditions.add(other)
        return this
    }

    override fun solve(): List<String> {
        val out = mutableListOf<String>()
        for (term in conditions) {
            out.addAll(term.solve())
        }
        return out
    }
}

/**
 * Creates a terminal condition
 *
 * Example:
 * ```
 * executeIf(con("score @s test matches 1")) {
 *   // ...
 * }
 * ```
 */
fun con(term: String): TermCondition =
    TermCondition(term)