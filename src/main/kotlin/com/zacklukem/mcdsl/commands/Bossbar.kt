package com.zacklukem.mcdsl.commands

import com.zacklukem.mcdsl.CommandBuilder

enum class BossbarSetting(private val repr: String) {
    MAX("max"),
    PLAYERS("players"),
    VALUE("value"),
    VISIBLE("visible");

    override fun toString(): String = repr
}

enum class Color(private val repr: String) {
    BLUE("blue"),
    GREEN("green"),
    PINK("pink"),
    PURPLE("purple"),
    RED("red"),
    WHITE("white"),
    YELLOW("yellow");

    override fun toString(): String = repr
}

enum class Style(private val repr: String) {
    // notched_6|notched_10|notched_12|notched_20|progress
    NOTCHED_6("notched_6"),
    NOTCHED_10("notched_10"),
    NOTCHED_12("notched_12"),
    NOTCHED_20("notched_20"),
    PROGRESS("progress");

    override fun toString(): String = repr
}

class Bossbar(private val namespace: String, private val name: String): CommandBuilder.Storable {
    private val fullName get() = "$namespace:$name"

    fun add(displayName: String): String {
        return "bossbar add $fullName \"$displayName\""
    }

    fun remove(): String {
        return "bossbar remove $fullName"
    }

    fun get(setting: BossbarSetting): String {
        return "bossbar get $fullName $setting"
    }

    fun setColor(color: Color): String {
        return "bossbar set $fullName color $color"
    }

    fun setMax(max: Int): String {
        return "bossbar set $fullName max $max"
    }

    fun setName(name: String): String {
        return "bossbar set $fullName name \"$name\""
    }

    fun setPlayers(players: List<String>): String {
        return "bossbar set $fullName players ${players.joinToString(" ")}"
    }

    fun setPlayers(vararg players: String): String {
        return setPlayers(players.toList())
    }

    fun setStyle(style: Style): String {
        return "bossbar set $fullName style $style"
    }

    fun setValue(value: Int): String {
        return "bossbar set $fullName value $value"
    }

    fun setVisible(visible: Boolean = true): String {
        return "bossbar set $fullName visible $visible"
    }

    fun setInvisible(): String {
        return setVisible(false)
    }

    fun storeMax(): String {
        return "bossbar $fullName max"
    }

    override fun executeStore(): String {
        return "bossbar $fullName value"
    }
}