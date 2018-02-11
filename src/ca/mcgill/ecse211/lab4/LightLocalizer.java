package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {

	// vehicle constants
	public static int ROTATION_SPEED = 100;
	private double SENSOR_LENGTH = 8;

	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	SampleProvider colorValue;
	float[] colorData;
	public Navigation navigation;

	double[] lineData;

	public LightLocalizer(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			SampleProvider colorValue, float[] colorData) {

		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.colorValue = colorValue;
		this.colorData = colorData;

		lineData = new double[4];
		navigation = new Navigation(odometer, leftMotor, rightMotor);
	}

	/**
	 * This method localizes the robot using the light sensor to precisely move to the right location
	 */
	public void localize() {

		int index = 0;
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);

		moveToOrigin();
		
		while (index < 4) {

			leftMotor.forward();
			rightMotor.backward();

			colorValue.fetchSample(colorData, 0);

			if (colorData[0] > 0.25) {

				leftMotor.stop(true);
				rightMotor.stop();
				
				lineData[index] = odometer.getXYT()[2];
				Sound.beep();
			}
		}
		
		double deltax, deltay, thetax, thetay;
		
		thetay = lineData[3] - lineData[1];
		thetax = lineData[2] - lineData[0];
		
		deltax = -SENSOR_LENGTH*Math.cos(Math.toRadians(thetay/2));
		deltay = -SENSOR_LENGTH*Math.cos(Math.toRadians(thetax/2));	
		
		odometer.setXYT(deltay, deltay, 270);
		navigation.travelTo(0.0, 0.0);
		
		navigation.turnTo(Math.toRadians(odometer.getXYT()[2]));
		
	}
	
	/**
	 * This method moves the robot towards the origin
	 */
	public void moveToOrigin() {

		navigation.turnTo(Math.PI / 4);
		
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);

		this.colorValue.fetchSample(colorData, 0);

		while (colorData[0] < 0.25) {

			leftMotor.forward();
			rightMotor.forward();

		}
		leftMotor.stop(true);
		rightMotor.stop();

		// Move backwards so our origin is close to origin
		leftMotor.rotate(convertDistance(Lab4.WHEEL_RAD, -6), true);
		rightMotor.rotate(convertDistance(Lab4.WHEEL_RAD, -6), false);

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

	/**
	 * This method allows the conversion of a angle to the total rotation of each
	 * wheel need to cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @param angle
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
