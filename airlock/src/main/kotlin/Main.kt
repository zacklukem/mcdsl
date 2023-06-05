import com.zacklukem.mcdsl.*
import com.zacklukem.mcdsl.blocks.*
import com.zacklukem.mcdsl.commands.Color
import com.zacklukem.mcdsl.util.*
import kotlin.io.path.Path

enum class Pressure(private val value: Int) : Discriminant {
    PRESSURIZED(0),
    DEPRESSURIZED(1),
    PRESSURIZING(2),
    DEPRESSURIZING(3);

    override fun discriminant(): Int = value
}

class Door(private val pos: Coord) {
    fun open(): List<String> {
        return listOf(
            pos.setblock("minecraft:air"),
            (pos + Coord.UP).setblock("minecraft:air"),
        )
    }

    fun close(): List<String> {
        return listOf(
            pos.setblock("minecraft:polished_blackstone_bricks"),
            (pos + Coord.UP).setblock("minecraft:black_stained_glass"),
        )
    }
}

fun airlock(pack: Datapack) {
    val airlock = pack.namespace("airlock_1", Coord(4930, 166, 4943))

    val prOut = PressurePlate(Coord(5045, 227, 4960))
    val prIn = PressurePlate(Coord(5040, 227, 4960))

    val lvOut = Lever(Coord(5043, 228, 4959), Dir.WEST)

    val lvIn = Lever(Coord(5042, 228, 4959), Dir.EAST)

    val lvPres = Lever(Coord(5043, 228, 4961), Dir.NORTH)

    val doorOut = Door(Coord(5044, 227, 4960))
    val doorIn = Door(Coord(5041, 227, 4960))

    val pressure = airlock.varEnum<Pressure>("pressure")
    val airlockBar = airlock.bossbar("airlock_1")

    val presTrigger = airlock.trigger {
        val timeScale = 1.0f / 3.0f

        atTime(0) {
            if_(pressure.oneOf(Pressure.PRESSURIZING, Pressure.DEPRESSURIZING)) {
                repeat(lvPres.setOn())
            }

            if_(pressure.eq(Pressure.DEPRESSURIZED)) {
                impulse(pressure.set(Pressure.PRESSURIZING))
            }

            if_(pressure.eq(Pressure.PRESSURIZED)) {
                impulse(pressure.set(Pressure.DEPRESSURIZING))
            }

            impulse(airlockBar.setColor(Color.RED))
            impulse(airlockBar.setVisible())
        }

        for (i in 0..100 step 10) {
            atTime(i.toFloat() * timeScale) {
                impulse(airlockBar.setValue(i))
            }
        }

        atTime(100.0f * timeScale) {
            impulse(airlockBar.setColor(Color.GREEN))
            impulse(lvPres.setOff())
            if_(pressure.eq(Pressure.PRESSURIZING)) {
                impulse(pressure.set(Pressure.PRESSURIZED))
            }

            if_(pressure.eq(Pressure.DEPRESSURIZING)) {
                impulse(pressure.set(Pressure.DEPRESSURIZED))
            }
        }

        atTime(120.0f * timeScale) {
            impulse(airlockBar.setInvisible())
            impulse(airlockBar.setValue(0))
            impulse(airlockBar.setColor(Color.RED))
            repeat(reset())
        }
    }

    airlock.commands {
        if_(prOut.isOn()) {
            repeat(airlock) {
                cmd(pressure.set(Pressure.DEPRESSURIZED))
                cmd(doorOut.open())
                cmd(lvOut.setOff())
            }
        }

        if_(prIn.isOn()) {
            repeat(airlock) {
                cmd(pressure.set(Pressure.PRESSURIZED))
                cmd(doorIn.open())
                cmd(lvIn.setOff())
            }
        }

        if_(prOut.isOff() * lvOut.isOff()) {
            repeat(doorOut.close())
        }

        if_(prIn.isOff() * lvIn.isOff()) {
            repeat(doorIn.close())
        }

        if_(lvOut.isOn()) {
            if_(pressure.eq(Pressure.DEPRESSURIZED)) {
                repeat(doorOut.open())
            }.else_ {
                repeat(lvOut.setOff())
            }
        }

        if_(lvIn.isOn()) {
            if_(pressure.eq(Pressure.PRESSURIZED)) {
                repeat(doorIn.open())
            }.else_ {
                repeat(lvIn.setOff())
            }
        }

        if_(lvPres.isOn()) {
            if_(lvIn.isOn() + lvOut.isOn()) {
                repeat(lvPres.setOff())
            }

            if_((lvIn.isOn() + lvOut.isOn()) * pressure.eq(Pressure.PRESSURIZED)) {
                repeat(lvPres.setOff())
            }

            if_(lvOut.isOff() * lvIn.isOff() * pressure.oneOf(Pressure.PRESSURIZED, Pressure.DEPRESSURIZED)) {
                repeat(presTrigger.trigger())
            }

            if_(pressure.eq(Pressure.DEPRESSURIZED) * lvOut.isOff() * lvIn.isOff()) {
                repeat(airlockBar.setName("Pressurizing..."))
            }

            if_(pressure.eq(Pressure.PRESSURIZED) * lvOut.isOff() * lvIn.isOff()) {
                repeat(airlockBar.setName("Depressurizing..."))
            }
        }
    }
}

fun main(args: Array<String>) {
    val pack = datapack {
        name = "mc_map"
        description = "A datapack for the mc_map world"
        packFormat = 12
    }

    airlock(pack)

    pack.print(Path(args[0]))
}