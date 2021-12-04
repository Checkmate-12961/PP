/*
The MIT License (MIT)

Copyright © 2021 Checkmate Robotics

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the “Software”), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.firstinspires.ftc.teamcode.robot.subsystems.drivetrain.localizers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.Localizer;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.spartronics4915.lib.T265Camera;

import org.firstinspires.ftc.teamcode.robot.subsystems.drivetrain.localizers.RealsenseLocalizer;
import org.firstinspires.ftc.teamcode.robot.subsystems.drivetrain.localizers.TrackingWheelLocalizer;
import org.firstinspires.ftc.teamcode.robot.util.PoseUtil;

import java.util.List;

/**
 * Localizer using both the tracking wheels and the realsense camera
 */
public class BiLocalizer implements Localizer {
    private final RealsenseLocalizer realsenseLocalizer;
    private final TrackingWheelLocalizer trackingWheelLocalizer;

    private ConfidenceTracker confidenceTracker = ConfidenceTracker.HIGH;

    /**
     * Keeps track of what the latest confidence was. If the confidence just turned to HIGH,
     *  it will update the camera's pose before going into HIGH.
     */
    private enum ConfidenceTracker {
        LOW,
        HIGH
    }

    /**
     * Initializes both localizers
     * @param hardwareMap Hardware map passed in from an op mode
     */
    public BiLocalizer(HardwareMap hardwareMap) {
        super();
        realsenseLocalizer = new RealsenseLocalizer(hardwareMap);
        trackingWheelLocalizer = new TrackingWheelLocalizer(hardwareMap);
    }

    /**
     * Gets the most confident localizer's estimated position
     * @return The position
     */
    @NonNull
    @Override
    public Pose2d getPoseEstimate() {
        // TODO: get the bounding box thing working
        if (confidenceTracker == ConfidenceTracker.HIGH) {
            return realsenseLocalizer.getPoseEstimate();
        }
        return trackingWheelLocalizer.getPoseEstimate();
    }

    /**
     * Sets both localizers' estimated positions
     * @param pose2d The position
     */
    @Override
    public void setPoseEstimate(@NonNull Pose2d pose2d) {
        trackingWheelLocalizer.setPoseEstimate(pose2d);
        realsenseLocalizer.setPoseEstimate(pose2d);
    }

    /**
     * Gets the velocity of the current pose
     * @return The velocity
     */
    @Nullable
    @Override
    public Pose2d getPoseVelocity() {
        // Using the camera's pose velocity because it takes the
        // tracking wheels into consideration when generating it
        return realsenseLocalizer.getPoseVelocity();
    }

    /**
     * Applies the tracking wheel velocity to the realsense camera and the updates
     *  the least confident localizer with the most confident one
     */
    @Override
    public void update() {
        List<Double> wheelVelocities = trackingWheelLocalizer.getWheelVelocities();
        realsenseLocalizer.sendOdometry(
                PoseUtil.inchesToMeters(
                    new Pose2d(wheelVelocities.get(0), wheelVelocities.get(2))
            )
        );

        // Get an update from the camera
        T265Camera.CameraUpdate update = realsenseLocalizer.getRawUpdate();

        // Update the pose of the least confident localizer
        // TODO: get the bounding box thing working
        switch (update.confidence) {
            case Failed:
            case Low:
            case Medium:
                confidenceTracker = ConfidenceTracker.LOW;
                break;
            default:
                if (confidenceTracker == ConfidenceTracker.LOW) {
                    realsenseLocalizer.setPoseEstimate(trackingWheelLocalizer.getPoseEstimate());
                    confidenceTracker = ConfidenceTracker.HIGH;
                }
        }
    }
}