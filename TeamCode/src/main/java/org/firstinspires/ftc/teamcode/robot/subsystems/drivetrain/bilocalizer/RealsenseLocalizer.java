package org.firstinspires.ftc.teamcode.robot.subsystems.drivetrain.bilocalizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.Localizer;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.spartronics4915.lib.T265Camera;

import org.firstinspires.ftc.teamcode.robot.subsystems.realsenseloader.RealsenseManager;
import org.firstinspires.ftc.teamcode.robot.util.PoseUtil;

/**
 * Localizer that implements Intel's T265 Realsense camera's VSLAM system
 */
public class RealsenseLocalizer implements Localizer {

    private T265Camera slamera = null;

    /**
     * Initializes the localizer and starts receiving packets from it
     * @param hardwareMap Passed in from the OpMode
     */
    public RealsenseLocalizer(HardwareMap hardwareMap) {
        super();

        if (RealsenseManager.slamera != null) {
            slamera = RealsenseManager.slamera;
        } else {
            throw new RuntimeException("Realsense camera was not initialized before attempting to use it!");
        }
    }

    /**
     * Gets the localizer's estimated position
     * @return The position
     */
    @NonNull
    @Override
    public Pose2d getPoseEstimate() {
        T265Camera.CameraUpdate update = slamera.getLastReceivedCameraUpdate();

        // Convert meters to inches
        return PoseUtil.metersToInches(
                // Convert to Roadrunner Pose2d
                PoseUtil.toRoadrunnerPose(
                        update.pose));
    }

    /**
     * Sets the localizer's estimated position
     * @param pose The position
     */
    @Override
    public void setPoseEstimate(@NonNull Pose2d pose) {
        slamera.setPose(
                // Convert to FTCLib Pose2d
                PoseUtil.toFtclibPose(
                        // Convert inches to meters
                        PoseUtil.inchesToMeters(
                                pose)));
    }

    /**
     * Gets the velocity of the current pose
     * @return The velocity
     */
    @Nullable
    @Override
    public Pose2d getPoseVelocity() {
        // Convert meters to inches
        return PoseUtil.metersToInches(
                // Convert ChassisSpeeds to Pose2d
                PoseUtil.chassisSpeedsToRoadrunnerPose(
                        // Get the camera's reported velocity
                        slamera.getLastReceivedCameraUpdate().velocity));
    }

    /**
     * Updates all of the nothing
     */
    @Override
    public void update() {
    }

    /**
     * Gets the last update received from the camera
     * @return The CameraUpdate
     */
    public T265Camera.CameraUpdate getRawUpdate() {
        return slamera.getLastReceivedCameraUpdate();
    }

    /**
     * Sends external odometry to the Realsense camera for extra accuracy
     */
    public void sendOdometry(Pose2d velocity) {
        slamera.sendOdometry(velocity.getX(), velocity.getY());
    }
}