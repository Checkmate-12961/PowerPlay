package org.firstinspires.ftc.teamcode.opmodes.freightfrenzy.tele;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.robot.abstracts.BasicOpMode;
import org.firstinspires.ftc.teamcode.robot.util.PoseUtil;

import java.util.Locale;

@TeleOp
public class TeleBasic extends BasicOpMode {
    private enum ControlMode {TELE, AUTO}
    private ControlMode controlMode = ControlMode.TELE;

    @Override
    public void setup(){
        // Retrieve our pose from the PoseStorage.currentPose static field
        robot.drivetrain.setPoseEstimate(PoseUtil.get());
    }

    @Override
    public void run_loop(){
        updatePosition();

        switch (controlMode){
            case TELE:
                // Moves the robot based on the GP1 left stick
                runDrivetrain();

                // Runs the carousel spinner based on the GP2 bumpers
                runCarousel();
                break;

            case AUTO:
                // Replace false here with a check to cancel the sequence
                //noinspection ConstantConditions
                if (false) robot.drivetrain.cancelSequence();
                if (!robot.drivetrain.isBusy()) controlMode = ControlMode.TELE;
                break;
            default:
                // If we end up here, something went horribly wrong.
                // Generally, the best plan of action is to ignore
                //  it and move on.
                controlMode = ControlMode.TELE;
                // Mission accomplished.
                break;
        }
    }

    private void updatePosition() {
        Pose2d position = robot.drivetrain.getPoseEstimate();
        PoseUtil.set(position);
        // Print pose to telemetry
        telemetry.addData("x", position.getX());
        telemetry.addData("y", position.getY());
        telemetry.addData("heading", Math.toDegrees(position.getHeading()));
        telemetry.addData("runtime",String.format(Locale.ENGLISH,"%fs",getRuntime()));
        telemetry.update();
    }

    // BIND:
    //  gamepad1.left_stick_x, gamepad1.left_stick_y
    //  gamepad1.right_stick_x, gamepad1.right_stick_y
    //  gamepad1.right_trigger
    private void runDrivetrain() {
        // Set the power of the DT based on the GP1 left
        robot.drivetrain.setWeightedDrivePower(
                new Pose2d(
                        // left stick X
                        -gamepad1.left_stick_y * Range.scale((gamepad1.right_trigger), -1, 1, 0, 1),
                        // left sick Y
                        -gamepad1.left_stick_x * Range.scale((gamepad1.right_trigger), -1, 1, 0, 1),
                        // right stick X (rotation)
                        -gamepad1.right_stick_x * Range.scale((gamepad1.right_trigger), -1, 1, 0, 1)
                )
        );
    }

    // BIND:
    //  gamepad2.right_bumper, gamepad2.left_bumper
    private void runCarousel() {
        if (gamepad2.right_bumper) {
            robot.carousel.setPower(1);
        } else if (gamepad2.left_bumper) {
            robot.carousel.setPower(-1);
        } else {
            robot.carousel.setPower(0);
        }
    }
}
