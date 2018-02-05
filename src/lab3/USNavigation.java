// Lab2.javaAVIbstpackage lab3;
package lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class USNavigation extends Thread {

	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private SampleProvider us;
	private float[] usData;

	private double deltax;
	private double deltay;

	private double currx;
	private double curry;
	private double currTheta;

	private static final int FORWARD_SPEED = 180;
	private static final int ROTATE_SPEED = 100;
	private static final double TILE_SIZE = 30.48;

	private boolean navigate = true;

	public USNavigation(Odometer odo, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor leftMotor,
			SampleProvider us) {
		this.odometer = odo;
		this.rightMotor = rightMotor;
		this.leftMotor = leftMotor;
		this.us = us;
		this.usData = new float[us.sampleSize()];
	}

	/**
	 * A method to get the distance from our sensor
	 * 
	 * @return
	 */
	private int fetchUS() {
		us.fetchSample(usData, 0);
		return (int) (usData[0] * 100);
	}

	/**
	 * A method to process the sensor data and adjust our vehicles course
	 */
	public void adjustPath() {

		double position[] = odometer.getXYT();
		if (position[2] > 180 && position[2] < 355) {
			if (position[0] < TILE_SIZE) {
				rightTurn();

			} else {
				leftTurn();
			}
		} else {
			if (position[0] > TILE_SIZE) {
				rightTurn();

			} else {
				leftTurn();
			}
		}
	}

	/**
	 * A method to drive our vehicle around the block by turning right
	 */
	public void rightTurn() {

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

	}

	/**
	 * A method to drive our vehicle around the block by turning left
	 */
	public void leftTurn() {

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);
	}

	// main run method for USnavigation
	public void run() {

		travelTo(0.0, 2 * TILE_SIZE);
	}

	/**
	 * A method to determine the direction the robot is facing
	 * 
	 * @param x
	 *            X-Coordinate
	 * @param y
	 *            Y-Coordinate
	 */
	private void determineHeading(double x, double y) {
		currx = odometer.getXYT()[0];
		curry = odometer.getXYT()[1];

		deltax = x - currx;
		deltay = y - curry;

		// Calculate the minimum angle to turn around
		currTheta = (odometer.getXYT()[2]) * Math.PI / 180;
		double minTheta = Math.atan2(deltax, deltay) - currTheta;
		turnTo(minTheta);

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
		determineHeading(x, y);

		while (navigate) {

			int dist = fetchUS();

			if (dist < 15) {
				leftMotor.stop();
				rightMotor.stop();
				adjustPath();
				determineHeading(x, y);
				continue;
			}

			currx = odometer.getXYT()[0];
			curry = odometer.getXYT()[1];

			deltax = x - currx;
			deltay = y - curry;

			double hypot = Math.hypot(deltax, deltay);

			// Stop when vehicle is at waypoint
			if (hypot < 0.5) {
				navigate = false;
			}

			// Turn to correct angle towards the endpoint

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);

			leftMotor.forward();
			rightMotor.forward();
			// leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, hypot), true);
			// rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, hypot), false);

			// stop vehicle
			// leftMotor.stop(true);
			// rightMotor.stop(true);
		}
		leftMotor.stop();
		rightMotor.stop();
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

	// @Override
	// public void processUSData(int distance) {
	// TODO Auto-generated method stub

	// }
}