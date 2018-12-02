package wallfollowing;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

	private final int bandCenter;
	private final int bandwidth;
	private final int motorLow;
	private final int motorHigh;
	private int distance;

	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
		WallFollowingLab.rightMotor.setSpeed(motorHigh);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)

		// right when robot is out of bandwidth and close to the wall
		if (bandCenter - bandwidth > distance) {

			// Safety for when robot is very close to the wall
			// Hardcoded value to compare the closeness to the wall

			if (bandCenter - bandwidth > distance - 20) {

				// reverse one wheel and continue the next
				WallFollowingLab.rightMotor.setSpeed(250);
				WallFollowingLab.rightMotor.backward();
				WallFollowingLab.leftMotor.setSpeed(100);

			} else {
				WallFollowingLab.leftMotor.setSpeed(motorHigh + 50);
				WallFollowingLab.rightMotor.setSpeed(motorLow);
				WallFollowingLab.rightMotor.forward();

			}

			// left when robot is out of bandwidth and far to the wall
		} else if (bandCenter + bandwidth < distance) {

			WallFollowingLab.leftMotor.setSpeed(motorLow + 25);
			WallFollowingLab.rightMotor.setSpeed(motorHigh);

			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();

			// move straight
		} else {

			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
		}

	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
