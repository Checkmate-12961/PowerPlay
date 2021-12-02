package org.firstinspires.ftc.teamcode.robot.subsystems

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.teamcode.robot.HardwareNames.Motors
import org.firstinspires.ftc.teamcode.robot.abstracts.AbstractSubsystem
import kotlin.math.PI

class Lift(hardwareMap: HardwareMap, private val bucket: Bucket) : AbstractSubsystem {
    private val liftMotor: DcMotorEx

    // This is weird because of the dashboard
    @Config
    object Lift {
        @JvmField var liftBounds = LiftBounds(0.0, 20.0)
        @JvmField var liftSetPoints = LiftSetPoints(12.0, 15.0, 20.0)
    }

    enum class Points {
        LOW, MID, HIGH, MIN, MAX
    }

    // Multiplier for the height of the lift
    private val spoolDiameter = 1.5 // inches
    private val encoderTicksPerRev = 537.7
    private val ticksPerInch = encoderTicksPerRev / (spoolDiameter * PI)

    private var zeroPositionLatch = false

    override fun update() {
        if (!liftMotor.isBusy && bucket.position != Bucket.Positions.ZERO && height == 0.0) {
            if (!zeroPositionLatch) {
                zeroPositionLatch = true
                bucket.position = Bucket.Positions.ZERO
            }
        } else {
            zeroPositionLatch = false
        }
    }

    var height: Double
        set(value) {
            val temp = (
                    Range.clip(
                        value, Lift.liftBounds.min, Lift.liftBounds.max
                    ) * ticksPerInch).toInt()
            if (temp != 0) {
                bucket.position = Bucket.Positions.REST
            }
            liftMotor.targetPosition = temp
        }
        get() {
            return liftMotor.targetPosition.toDouble() / ticksPerInch
        }

    var target: Points? = null
        set(value) {
            field = value
            when (value) {
                Points.MIN -> height = Lift.liftBounds.min
                Points.LOW -> height = Lift.liftSetPoints.low
                Points.MID -> height = Lift.liftSetPoints.mid
                Points.HIGH -> height = Lift.liftSetPoints.high
                Points.MAX -> height = Lift.liftBounds.max
            }
        }

    init {
        // Initialize the motor
        liftMotor = hardwareMap.get(DcMotorEx::class.java, Motors.LIFT.name)
        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        // Reverse the motor if the config says to
        if (Motors.LIFT.reverse) {
            liftMotor.direction = DcMotorSimple.Direction.REVERSE
        }

        // Set it to run to a target position and hold it
        height = 0.0
        liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftMotor.power = 1.0
    }

    data class LiftBounds(var min: Double, var max: Double)
    data class LiftSetPoints(var low: Double, var mid: Double, var high: Double)
}