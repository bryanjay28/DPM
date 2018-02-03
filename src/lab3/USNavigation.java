package lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class USNavigation extends Thread {

	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private double deltax;
	private double deltay;

	private double currx;
	private double curry;
	private double currTheta;

	private static final int FORWARD_SPEED = 180;
	private static final int ROTATE_SPEED = 100;
	private static final double TILE_SIZE = 30.48;
	
	private static final int bandCenter = 30; // Offset from the wall (cm)
	private static final int bandWidth = 3; // Width of dead band (cm)
	private static final int motorLow = 140; // Speed of slower rotating wheel (deg/sec)
	private static final int motorHigh = 280; // Speed of the faster rotating wheel (deg/sec)
	
	private int distance;

	private boolean navigate = true;

	public USNavigation(Odometer odo, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor leftMotor) {
		this.odometer = odo;
		this.rightMotor = rightMotor;
		this.leftMotor = leftMotor;
	}
	
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)

		// right when robot is out of bandwidth and close to the wall
		if (bandCenter - bandWidth > distance) {

			// Safety for when robot is very close to the wall
			// Hardcoded value to compare the closeness to the wall

			if (bandCenter - bandWidth > distance - 20) {

				// reverse one wheel and continue the next
				rightMotor.setSpeed(250);
				rightMotor.backward();
				leftMotor.setSpeed(100);

			} else {
				leftMotor.setSpeed(motorHigh + 50);
				rightMotor.setSpeed(motorLow);
				rightMotor.forward();

			}

			// left when robot is out of bandwidth and far to the wall
		} else if (bandCenter + bandWidth < distance) {

			leftMotor.setSpeed(motorLow + 25);
			rightMotor.setSpeed(motorHigh);

			leftMotor.forward();
			rightMotor.forward();

		}
	}

	public int readUSDistance() {
		return this.distance;
	}
	
	// main run method for USnavigation
		public void run() {
			travelTo(TILE_SIZE, 2 * TILE_SIZE);
			travelTo(2 * TILE_SIZE, 2 * TILE_SIZE);
			travelTo(0.0, 0.0);
			travelTo(2 * TILE_SIZE, TILE_SIZE);

		}

		/**
		 * A method to drive our vehicle to a certain Cartesian coordinate
		 * 
		 * @param x
		 *            X-Coordinate
		 * @param y
		 *            Y-Coordinate
		 */
		void travelTo(double x, double y) {

			navigate = true;
			currx = odometer.getXYT()[0];
			curry = odometer.getXYT()[1];

			deltax = x - currx;
			deltay = y - curry;

			// Calculate the minimum angle to turn around
			currTheta = (odometer.getXYT()[2]) * Math.PI / 180;
			double minTheta = Math.atan2(deltax, deltay) - currTheta;
			double hypot = Math.hypot(deltax, deltay);

			// Turn to correct angle towards the endpoint
			turnTo(minTheta);

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);

			leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, hypot), true);
			rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, hypot), false);

			// stop vehicle
			leftMotor.stop(true);
			rightMotor.stop(true);
			// done navigating
			navigate = false;

		}

		/**
		 * A method to turn our vehicle to a certain angle
		 * 
		 * @param theta
		 */
		void turnTo(double theta) {

			// set Speed
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			// rotate motors at set speed

			// if angle is negative, turn to the left
			if (theta < 0) {
				leftMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, -(theta * 180) / Math.PI), true);
				rightMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, -(theta * 180) / Math.PI), false);

			} else {
				// angle is positive, turn to the right
				leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, (theta * 180) / Math.PI), true);
				rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, (theta * 180) / Math.PI), false);
			}
		}

		/**
		 * A method to determine whether another thread has called travelTo and turnTo
		 * methods or not
		 * 
		 * @return
		 */
		boolean isNavigating() throws OdometerExceptions {
			return navigate;
		}

		/**
		 * This method allows the conversion of a distance to the total rotation of each
		 * wheel need to cover that distance.
		 * 
		 * @param radius
		 * @param distance
		 * @return
		 */
		private static int convertDistance(double radius, double distance) {
			return (int) ((180.0 * distance) / (Math.PI * radius));
		}

		private static int convertAngle(double radius, double width, double angle) {
			return convertDistance(radius, Math.PI * width * angle / 360.0);
		}
}
