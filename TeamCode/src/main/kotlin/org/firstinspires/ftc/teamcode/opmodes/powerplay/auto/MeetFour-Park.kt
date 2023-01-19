package org.firstinspires.ftc.teamcode.opmodes.powerplay.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.teamcode.robot.abstracts.BaseOpMode
import org.firstinspires.ftc.teamcode.robot.subsystems.ColorCone

@Autonomous(name = "-PARK-", group = "PARK", preselectTeleOp = "TeleOp")
class BlueRightPark : BaseOpMode() {
    override fun preRunLoop() {
        robot.zelda!!.followTrajectorySequenceAsync(
            MeetFourParkRoot.gen(
                robot,
                ColorCone::rightColor,
                false,
            ) { it }
        )
    }
}