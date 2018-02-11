// Lab3.javaAVIbstpackage lab3;
package ca.mcgill.ecse211.lab3;

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

	// diving constants
	private static final int FORWARD_SPEED = 170;
	private static final int ROTATE_SPEED = 100;
	private static final double TILE_SIZE = 30.48;

	private boolean navigate = true;

	// constructor for USnav
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
				leftTurn();
			} else {
				if (position[2] < 100 && position[2] > 80) {
					leftTurn();
				} else {
					rightTurn();
				}
			}
		}
	}

	/**
	 * A method to drive our vehicle around the block by turning right
	 */
	public void rightTurn() {

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// turn 90 degrees to avoid obstacle
		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		// move to the left by a tile size
		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// turn again 90 back and move past the block
		leftMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE + 10), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE + 10), false);

	}

	/**
	 * A method to drive our vehicle around the block by turning left
	 */
	public void leftTurn() {

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// turn 90 degrees to avoid obstacle
		leftMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		// move to the left by a tile size
		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE), false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// turn again 90 back and move past the block
		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE + 10), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, TILE_SIZE + 10), false);
	}

	// main run method for USnavigation
	public void run() {

		travelTo(TILE_SIZE, 0.0);
		travelTo(2*TILE_SIZE, TILE_SIZE);
		travelTo(2 * TILE_SIZE, 2 * TILE_SIZE);
		travelTo(0.0, 2 * TILE_SIZE);
		travelTo(TILE_SIZE, TILE_SIZE);

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
	private void travelTo(double x, double y) {

		navigate = true;
		determineHeading(x, y);

		// while navigating to waypoint we want to continuously check for blocks
		while (navigate) {

			int dist = fetchUS();

			// when 10cm away we will stop and readjust our course
			if (dist < 10) {

				leftMotor.stop(true);
				rightMotor.stop();
				adjustPath();
				determineHeading(x, y);
				continue;
			}

			currx = odometer.getXYT()[0];
			curry = odometer.getXYT()[1];

			deltax = x - currx;
			deltay = y - curry;

			// determine our distance from final position
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

		}
		leftMotor.stop(true);
		rightMotor.stop();
	}

	/**
	 * A method to turn our vehicle to a certain angle
	 * 
	 * @param theta
	 */
	private void turnTo(double theta) {

		if (theta > Math.PI) {
			theta -= 2 * Math.PI;
		} else if (theta < -Math.PI) {
			theta += 2 * Math.PI;
		}

		// rotate motors at set speed
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

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