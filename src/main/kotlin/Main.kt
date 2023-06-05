import mcdsl.*
import java.io.File

enum class Pressure(val value: Int) : Discriminant {
    PRESSURIZED(0),
    DEPRESSURIZED(1),
    PRESSURIZING(2),
    DEPRESSURIZING(3);

    override fun discriminant(): Int {
        return value
    }
}

class Door(val pos: Coord) {
    fun open(): List<String> {
        return listOf(
            "setblock $pos minecraft:air",
            "setblock ${pos + Coord(0, 1, 0)} minecraft:air"
        )
    }

    fun close(): List<String> {
        return listOf(
            "setblock $pos minecraft:polished_blackstone_bricks",
            "setblock ${pos + Coord(0, 1, 0)} minecraft:black_stained_glass"
        )
    }
}

fun main() {
    val pack = Namespace("airlock_1")

    val prOut = PressurePlate(Coord(5045, 227, 4960))
    val prIn = PressurePlate(Coord(5040, 227, 4960))

    val lvOut = Lever(Coord(5043, 228, 4959), Dir.WEST)

    val lvIn = Lever(Coord(5042, 228, 4959), Dir.EAST)

    val lvPres = Lever(Coord(5043, 228, 4961), Dir.NORTH)

    val doorOut = Door(Coord(5044, 227, 4960))
    val doorIn = Door(Coord(5041, 227, 4960))

    val pressure = pack.varEnum<Pressure>("pressure")

    val presTrigger = pack.trigger {
        val timeScale = 1.0f / 3.0f

        at(0) {
            executeIf(pressure.oneOf(Pressure.PRESSURIZING, Pressure.DEPRESSURIZING)) {
                repeat(lvPres.setOn())
            }

            executeIf(pressure.eq(Pressure.DEPRESSURIZED)) {
                impulse(pressure.set(Pressure.PRESSURIZING))
            }

            executeIf(pressure.eq(Pressure.PRESSURIZED)) {
                impulse(pressure.set(Pressure.DEPRESSURIZING))
            }

            impulse("bossbar set minecraft:airlock_1 color red")
            impulse("bossbar set minecraft:airlock_1 visible true")
        }

        for (i in 0..100 step 10) {
            at(i.toFloat() * timeScale) {
                impulse("bossbar set minecraft:airlock_1 value $i")
            }
        }

        at(100.0f * timeScale) {
            impulse("bossbar set minecraft:airlock_1 color green")
            impulse(lvPres.setOff())
            executeIf(pressure.eq(Pressure.PRESSURIZING)) {
                impulse(pressure.set(Pressure.PRESSURIZED))
            }

            executeIf(pressure.eq(Pressure.DEPRESSURIZING)) {
                impulse(pressure.set(Pressure.DEPRESSURIZED))
            }
        }

        at(120.0f * timeScale) {
            impulse("bossbar set minecraft:airlock_1 visible false")
            impulse("bossbar set minecraft:airlock_1 value 0")
            impulse("bossbar set minecraft:airlock_1 color red")
            repeat(reset())
        }
    }

    pack.commands {
        executeIf(prOut.isOn()) {
            repeat(pack) {
                cmd(pressure.set(Pressure.DEPRESSURIZED))
                cmd(doorOut.open())
                cmd(lvOut.setOff())
            }
        }

        executeIf(prIn.isOn()) {
            repeat(pack) {
                cmd(pressure.set(Pressure.PRESSURIZED))
                cmd(doorIn.open())
                cmd(lvIn.setOff())
            }
        }

        executeIf(prOut.isOff() * lvOut.isOff()) {
            repeat(doorOut.close())
        }

        executeIf(prIn.isOff() * lvIn.isOff()) {
            repeat(doorIn.close())
        }

        executeIf(lvOut.isOn() * pressure.eq(Pressure.DEPRESSURIZED)) {
            repeat(doorOut.open())
        }

        executeIf(lvIn.isOn() * pressure.eq(Pressure.PRESSURIZED)) {
            repeat(doorIn.open())
        }

        executeIf(lvOut.isOn() * pressure.oneOf(Pressure.PRESSURIZED, Pressure.PRESSURIZING, Pressure.DEPRESSURIZING)) {
            repeat(lvOut.setOff())
        }

        executeIf(
            lvIn.isOn() * pressure.oneOf(
                Pressure.DEPRESSURIZED,
                Pressure.PRESSURIZING,
                Pressure.DEPRESSURIZING
            )
        ) {
            repeat(lvIn.setOff())
        }

        executeIf(lvPres.isOn() * (lvIn.isOn() + lvOut.isOn())) {
            repeat(lvPres.setOff())
        }

        // Unnecessary?
        executeIf(lvPres.isOn() * (lvIn.isOn() + lvOut.isOn()) * pressure.eq(Pressure.PRESSURIZED)) {
            repeat(lvPres.setOff())
        }

        executeIf(
            lvPres.isOn() * lvOut.isOff() * lvIn.isOff() * pressure.oneOf(Pressure.PRESSURIZED, Pressure.DEPRESSURIZED)
        ) {
            repeat(presTrigger.trigger())
        }

        executeIf(
            pressure.eq(Pressure.DEPRESSURIZED) * lvOut.isOff() * lvIn.isOff() * lvPres.isOn()
        ) {
            repeat("bossbar set minecraft:airlock_1 name \\\"Pressurizing...\\\"")
        }

        executeIf(
            pressure.eq(Pressure.PRESSURIZED) * lvOut.isOff() * lvIn.isOff() * lvPres.isOn()
        ) {
            repeat("bossbar set minecraft:airlock_1 name \\\"Depressurizing...\\\"")
        }


    }

    pack.print(Coord(4930, 166, 4943), File("out.txt"))
}