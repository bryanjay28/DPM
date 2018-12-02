package wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

	/* Constants */
	private static final int MOTOR_SPEED = 200;
	private static final int FILTER_OUT = 30;

	private final int bandCenter;
	private final int bandWidth;
	private int distance;
	private int filterControl;
	private int distError;
	private double PROPCONST = 5.0;
	private int FWDSPEED = MOTOR_SPEED;
	private int MAXCORRECTION = 45;

	public PController(int bandCenter, int bandwidth) {
		this.bandCenter = bandCenter;
		this.bandWidth = bandwidth;
		this.filterControl = 0;

		WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initalize motor rolling forward
		WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}

		// TODO: process a movement based on the us distance passed in (P style)

		distError = this.distance - bandCenter;
		int correction = calcProp(distError);

		// Go straight
		if (Math.abs(distError) <= this.bandWidth) {

			WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED);
			WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();

			// left when robot is far from wall
		} else if (distError > 0) {

			// hard coded a set distance for the wall when it is very far to increase speed
			// more
			if (distError > 60) {
				WallFollowingLab.leftMotor.setSpeed((int) (MOTOR_SPEED - correction));
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + correction * 2);

				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();

				// Normal left turn
			} else {

				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + correction);
				WallFollowingLab.leftMotor.setSpeed((int) (MOTOR_SPEED - correction * 0.5));

				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();

			}

			// right when robot is close to wall
		} else if (distError < 0) {

			// When the robot is very close to the wall reverse one wheel and speed up the
			// other wheel
			if (-10 >= distError && distError >= -25) {

				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED + correction * 3);
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED - correction * 2);

				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.backward();
				// when the robot is extremely close to the wall we reverse completely
			} else if (distError < -25) {

				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED - correction * 2);
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + correction * 1);

				WallFollowingLab.leftMotor.backward();
				WallFollowingLab.rightMotor.backward();

			}
			// Normal right turn
			else {

				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED + correction * 2);
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED - correction * 2);

				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();

			}

		}

	}

	int calcProp(int diff) {

		int correction;

		// PROPORTIONAL: Correction is proportional to magnitude of error

		diff = Math.abs(diff);

		correction = (int) (PROPCONST * (double) diff);
		if (correction >= FWDSPEED) {
			correction = MAXCORRECTION;
		}

		return correction;
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
