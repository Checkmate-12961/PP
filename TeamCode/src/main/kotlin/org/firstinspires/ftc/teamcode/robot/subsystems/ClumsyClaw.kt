package org.firstinspires.ftc.teamcode.robot.subsystems

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.HardwareNames.Servos
import org.firstinspires.ftc.teamcode.robot.abstracts.AbstractSubsystem
import org.firstinspires.ftc.teamcode.robot.abstracts.SubsystemMap
import kotlin.reflect.KProperty

/**
 * It's a claw that can flip around in annoying-to-program ways.
 *
 * @param hardwareMap Map of the hardware passed in from
 * [org.firstinspires.ftc.teamcode.robot.CheckmateRobot].
 */
@Config
@Suppress("unused")
class ClumsyClaw(hardwareMap: HardwareMap) : AbstractSubsystem {
    override val tag = this.javaClass.simpleName
    override val subsystems = SubsystemMap { tag }

    // Servos
    private val wristServo = Servos.WRIST.get(hardwareMap)
    private val gripperServo = Servos.GRIPPER.get(hardwareMap)
    private val pivotServoA = Servos.PIVOT_A.get(hardwareMap)
    private val pivotServoB = Servos.PIVOT_B.get(hardwareMap)

    private val pivotServos = listOf(pivotServoA, pivotServoB)
    private val servos = listOf(wristServo, gripperServo, pivotServoA, pivotServoB)

    // Use these to interact with the servos
    var wrist: WristPosition by ServoDelegate(wristServo, WristPosition.BIG_EYES)
    var gripper: GripperPosition by ServoDelegate(gripperServo, GripperPosition.OPEN)
    var pivot: PivotPosition by ServoDelegate(pivotServos, PivotPosition.REST)

    /**
     * Manages applying values from the enum to the servos.
     *
     * @param E [PositionEnum] to apply positions from.
     *
     * @param servos [List] of [Servo] to apply the position to.
     * @param default Starting position for the servos.
     */
    private class ServoDelegate<E : PositionEnum>(private val servos: List<Servo>, default: E) {
        /**
         * Manages applying values from the enum to a servo.
         *
         * @param servo Single servo to be built into a list of servos.
         */
        constructor(servo: Servo, default: E) : this(listOf(servo), default)

        /**
         * Current position of [servos].
         */
        private var field: E = default

        operator fun getValue(thisRef: Any?, property: KProperty<*>): E = field
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: E) {
            field = value

            servos.forEach { it.position = value.position() }
        }
    }

    private interface PositionEnum {
        val position: () -> Double
    }

    enum class WristPosition(override val position: () -> Double): PositionEnum {
        BIG_EYES({ wristPositions.grab }),
        SMALL_EYES({ wristPositions.cap })
    }
    data class WristPositions(
        @JvmField var grab: Double,
        @JvmField var cap: Double
    )

    enum class GripperPosition(override val position: () -> Double): PositionEnum {
        CLOSED({ gripperPositions.closed }),
        OPEN({ gripperPositions.open })
    }
    data class GripperPositions(
        @JvmField var closed: Double,
        @JvmField var open: Double
    )

    enum class PivotPosition(override val position: () -> Double): PositionEnum {
        GRAB({ pivotPositions.grab }),
        REST({ pivotPositions.rest }),
        START({ pivotPositions.start }),
        CAP({ pivotPositions.cap })
    }
    data class PivotPositions(
        @JvmField var grab: Double,
        @JvmField var rest: Double,
        @JvmField var start: Double,
        @JvmField var cap: Double
    )

    override fun generateTelemetry(telemetry: Telemetry) {
        telemetry.addData("wrist", wrist)
        telemetry.addData("gripper", gripper)
        telemetry.addData("pivot", pivot)
    }

    init {
        wrist = WristPosition.SMALL_EYES
        gripper = GripperPosition.CLOSED
        pivot = PivotPosition.START
    }

    companion object {
        @JvmField var wristPositions = WristPositions(0.33, 1.0)
        @JvmField var gripperPositions = GripperPositions(0.96, 0.7)
        @JvmField var pivotPositions = PivotPositions(0.9, 0.65, 0.31, 0.15)
    }
}