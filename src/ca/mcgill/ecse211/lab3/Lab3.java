package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {

	// Motor Objects, and Robot related parameters
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static final Port usPort = LocalEV3.get().getPort("S3");

	// Set vehicle constants
	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 15.79;

	public static void main(String[] args) throws OdometerExceptions {

		int buttonChoice;

		// Odometer related objects
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete
																							// implementation
		Display odometryDisplay = new Display(lcd); // No need to change

		@SuppressWarnings("resource") // Because we don't bother to close this resource
		// usSensor is the instance
		SensorModes ultrasonicSensor = new EV3UltrasonicSensor(usPort);
		// usDistance provides samples from this instance
		SampleProvider usDistance = ultrasonicSensor.getMode("Distance");
		do {
			// clear the display
			lcd.clear();

			// ask the user whether the motors should drive with navigation or avoidance
			lcd.drawString("< Left | Right >", 0, 0);
			lcd.drawString("       |        ", 0, 1);
			lcd.drawString("UsNavi |  Navi  ", 0, 2);
			lcd.drawString("       | 	    ", 0, 3);
			lcd.drawString("       | 		", 0, 4);

			buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {

			USNavigation usNavigation = new USNavigation(odometer, rightMotor, leftMotor, usDistance);

			// Start odometer and display threads
			Thread odoThread = new Thread(odometer);
			odoThread.start();

			Thread odoDisplayThread = new Thread(odometryDisplay);
			odoDisplayThread.start();

			// start ultrasonic navigation thread
			Thread usNavigationThread = new Thread(usNavigation);
			usNavigationThread.start();

		} else {
			// clear the display
			lcd.clear();
			Navigation navigation = new Navigation(odometer, leftMotor, rightMotor);

			// Start odometer and display threads
			Thread odoThread = new Thread(odometer);
			odoThread.start();

			Thread odoDisplayThread = new Thread(odometryDisplay);
			odoDisplayThread.start();

			// start navigation thread
			Thread navigationThread = new Thread(navigation);
			navigationThread.start();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}