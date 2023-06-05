package com.zacklukem.mcdsl.util

interface Condition {
    operator fun not(): Condition
    operator fun times(other: Condition): AndCondition
    operator fun plus(other: Condition): OrCondition
    fun solve(): List<String>
}

class TermCondition(private val term: String, private val negated: Boolean = false) : Condition {
    override fun not(): TermCondition =
        TermCondition(term, !negated)

    override fun times(other: Condition): AndCondition =
        AndCondition(mutableListOf(this, other))

    override fun plus(other: Condition): OrCondition =
        OrCondition(mutableListOf(this, other))

    override fun solve(): List<String> {
        return listOf(if (negated) "unless $term" else "if $term")
    }
}

class AndCondition(private val conditions: MutableList<Condition>) : Condition {
    override fun not(): OrCondition =
        OrCondition(conditions.map(Condition::not).toMutableList())

    override fun times(other: Condition): AndCondition {
        conditions.add(other)
        return this
    }

    override fun plus(other: Condition): OrCondition =
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

class OrCondition(private val conditions: MutableList<Condition>) : Condition {
    override fun not(): AndCondition =
        AndCondition(conditions.map(Condition::not).toMutableList())

    override fun times(other: Condition): AndCondition =
        AndCondition(mutableListOf(this, other))

    override fun plus(other: Condition): OrCondition {
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

fun con(term: String): TermCondition =
    TermCondition(term)