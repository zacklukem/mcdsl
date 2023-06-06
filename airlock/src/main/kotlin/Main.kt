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
    val airlock = pack.namespace("airlock_1")

    val prOut = PressurePlate(Coord(5045, 227, 4960))
    val prIn = PressurePlate(Coord(5040, 227, 4960))

    val lvOut = Lever(Coord(5043, 228, 4959), Dir.WEST)

    val lvIn = Lever(Coord(5042, 228, 4959), Dir.EAST)

    val lvPres = Lever(Coord(5043, 228, 4961), Dir.NORTH)

    val doorOut = Door(Coord(5044, 227, 4960))
    val doorIn = Door(Coord(5041, 227, 4960))

    val pressureSign = Sign(Coord(5042, 228, 4961))
    val pressure = airlock.varEnum<Pressure>("pressure")
    val airlockBar = airlock.bossbar("airlock_1")

    airlock.onLoad {
        cmd(airlockBar.add("Pressure"))
        cmd(pressure.init())
    }

    val presTrigger = airlock.function("pres_trigger") {
        val timeScale = 1.0f / 3.0f

        if_(pressure.eq(Pressure.DEPRESSURIZED)) {
            cmd(pressure.set(Pressure.PRESSURIZING))
        }

        if_(pressure.eq(Pressure.PRESSURIZED)) {
            cmd(pressure.set(Pressure.DEPRESSURIZING))
        }

        cmd(airlockBar.setPlayers("@a"))
        cmd(airlockBar.setColor(Color.RED))
        cmd(airlockBar.setVisible())

        for (i in 0..100 step 5) {
            schedule_(ticks(i.toFloat() * timeScale)) {
                cmd(airlockBar.setValue(i))
            }
        }

        schedule_(ticks(100.0f * timeScale)) {
            cmd(airlockBar.setColor(Color.GREEN))
            cmd(lvPres.setOff())
            if_(pressure.eq(Pressure.PRESSURIZING)) {
                cmd(pressureSign.setText("\nPRESSURIZED"))
                cmd(airlockBar.setName("Pressurized"))
                cmd(pressure.set(Pressure.PRESSURIZED))
            }

            if_(pressure.eq(Pressure.DEPRESSURIZING)) {
                cmd(pressureSign.setText("\nDEPRESSURIZED"))
                cmd(airlockBar.setName("Depressurized"))
                cmd(pressure.set(Pressure.DEPRESSURIZED))
            }
        }

        schedule_(ticks(200.0f * timeScale)) {
            cmd(airlockBar.setInvisible())
            cmd(airlockBar.setValue(0))
            cmd(airlockBar.setColor(Color.RED))
        }
    }

    airlock.onTick {
        if_(pressure.oneOf(Pressure.PRESSURIZING, Pressure.DEPRESSURIZING)) {
            cmd(lvPres.setOn())
        }

        if_(prOut.isOn()) {
            cmd(pressure.set(Pressure.DEPRESSURIZED))
            cmd(pressureSign.setText("\nDEPRESSURIZED"))
            cmd(doorOut.open())
            cmd(lvOut.setOff())
        }

        if_(prIn.isOn()) {
            cmd(pressure.set(Pressure.PRESSURIZED))
            cmd(pressureSign.setText("\nPRESSURIZED"))
            cmd(doorIn.open())
            cmd(lvIn.setOff())
        }

        if_(prOut.isOff() and lvOut.isOff()) {
            cmd(doorOut.close())
        }

        if_(prIn.isOff() and lvIn.isOff()) {
            cmd(doorIn.close())
        }

        if_(lvOut.isOn()) {
            if_(pressure.eq(Pressure.DEPRESSURIZED)) {
                cmd(doorOut.open())
            }.else_ {
                cmd(lvOut.setOff())
            }
        }

        if_(lvIn.isOn()) {
            if_(pressure.eq(Pressure.PRESSURIZED)) {
                cmd(doorIn.open())
            }.else_ {
                cmd(lvIn.setOff())
            }
        }

        if_(lvPres.isOn()) {
            if_(lvIn.isOn() or lvOut.isOn()) {
                cmd(lvPres.setOff())
            }

            if_((lvIn.isOn() or lvOut.isOn()) and pressure.eq(Pressure.PRESSURIZED)) {
                cmd(lvPres.setOff())
            }

            if_(lvOut.isOff() and lvIn.isOff() and pressure.oneOf(Pressure.PRESSURIZED, Pressure.DEPRESSURIZED)) {
                cmd(presTrigger.call())
            }

            if_(lvOut.isOff() and lvIn.isOff()) {
                if_(pressure.oneOf(Pressure.DEPRESSURIZED, Pressure.PRESSURIZING)) {
                    cmd(airlockBar.setName("Pressurizing..."))
                    cmd(pressureSign.setText("\nPRESSURIZING..."))
                }.else_ {
                    cmd(airlockBar.setName("Depressurizing..."))
                    cmd(pressureSign.setText("\nDEPRESSURIZING..."))
                }
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