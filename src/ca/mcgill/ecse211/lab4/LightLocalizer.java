package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class LightLocalizer {

	// vehicle constants
	public static int ROTATION_SPEED = 100;
	private double SENSOR_LENGTH = 11.8;

	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	public Navigation navigation;
	private static final EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1")); // Instantiate
																										// the EV3 Color
																										// Sensor
	private float sample;

	private SensorMode idColour;

	double[] lineData;

	public LightLocalizer(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {

		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		idColour = lightSensor.getRedMode(); // set the sensor light to red
		lineData = new double[4];
		navigation = new Navigation(odometer, leftMotor, rightMotor);
	}

	/**
	 * This method localizes the robot using the light sensor to precisely move to
	 * the right location
	 */
	public void localize() {

		int index = 0;
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);

		moveToOrigin();

		while (index < 4) {

			leftMotor.forward();
			rightMotor.backward();

			sample = fetchSample();

			if (sample < 0.38) {

				lineData[index] = odometer.getXYT()[2];
				Sound.beepSequenceUp();
				index++;
			}
		}
		
		leftMotor.stop(true);
		rightMotor.stop();

		double deltax, deltay, thetax, thetay;

		thetay = lineData[3] - lineData[1];
		thetax = lineData[2] - lineData[0];

		deltax = -1 * SENSOR_LENGTH * Math.cos(Math.toRadians(thetay / 2));
		deltay = -1 * SENSOR_LENGTH * Math.cos(Math.toRadians(thetax / 2));

		odometer.setXYT(deltax, deltay, odometer.getXYT()[2] + 6);
		navigation.travelTo(0.0, 0.0);
		
		leftMotor.setSpeed(ROTATION_SPEED/2);
		rightMotor.setSpeed(ROTATION_SPEED/2);		

		if(odometer.getXYT()[2] <= 350 && odometer.getXYT()[2] >= 10.0) {
			Sound.beep();
			leftMotor.rotate(convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, -odometer.getXYT()[2]), true);
			rightMotor.rotate(-convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, -odometer.getXYT()[2]), false);
		}
		
		leftMotor.stop(true);
		rightMotor.stop();

	}

	/**
	 * This method moves the robot towards the origin
	 */
	public void moveToOrigin() {

		navigation.turnTo(Math.PI / 4);

		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);

		sample = fetchSample();

		while (sample > 0.38) {
			sample = fetchSample();
			leftMotor.forward();
			rightMotor.forward();

		}
		leftMotor.stop(true);
		rightMotor.stop();
		Sound.beep();

		// Move backwards so our origin is close to origin
		leftMotor.rotate(convertDistance(Lab4.WHEEL_RAD, -12), true);
		rightMotor.rotate(convertDistance(Lab4.WHEEL_RAD, -12), false);

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

	private float fetchSample() {
		float[] colorValue = new float[idColour.sampleSize()];
		idColour.fetchSample(colorValue, 0);
		return colorValue[0];
	}

	/**
	 * This method determines whether the colour of the line is black
	 * 
	 * @param RGB
	 */
	private boolean isLine(float[] RGB) {
		if ((RGB[0] < 0.10) && (RGB[1] < 0.10) && (RGB[2] < 0.10)) {
			return true;
		} else {
			return false;
		}
	}

}
