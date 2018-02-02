// Lab3.java
package lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Lab3 {

  // Motor Objects, and Robot related parameters
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  private static final TextLCD lcd = LocalEV3.get().getTextLCD();
  
  public static final double WHEEL_RAD = 2.1;
  public static final double TRACK = 15.79;
  
  public static void main(String[] args) throws OdometerExceptions {

    int buttonChoice;

    // Odometer related objects
    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete implementation
    OdometryCorrection odometryCorrection = new OdometryCorrection(); // TODO Complete
                                                                      // implementation
    Display odometryDisplay = new Display(lcd); // No need to change
    Navigation navigation = new Navigation(odometer, leftMotor, rightMotor);
    USNavigation usNavigation = new USNavigation();

    do {
      // clear the display
      lcd.clear();

      // ask the user whether the motors should drive in a square or float
      lcd.drawString("< Left | Right >", 0, 0);
      lcd.drawString("       |        ", 0, 1);
      lcd.drawString("UsNavi |  Navi  ", 0, 2);
      lcd.drawString("		 | 	      ", 0, 3);
      lcd.drawString("       | 		  ", 0, 4);

      buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

    if (buttonChoice == Button.ID_LEFT) {
      // Float the motors
      leftMotor.forward();
      leftMotor.flt();
      rightMotor.forward();
      rightMotor.flt();

      // Display changes in position as wheels are (manually) moved
      
      Thread odoThread = new Thread(odometer);
      odoThread.start();
      
      Thread odoDisplayThread = new Thread(odometryDisplay);
      odoDisplayThread.start();
      
      Thread odoCorrectThread = new Thread(odometryCorrection);
      odoCorrectThread.start();
      
      Thread usNavigationThread = new Thread(usNavigation);
      usNavigationThread.start();      
      

    } else {
      // clear the display
      lcd.clear();

      
      // Start odometer and display threads
      Thread odoThread = new Thread(odometer);
      odoThread.start();
      
      Thread odoDisplayThread = new Thread(odometryDisplay);
      odoDisplayThread.start();
      
      Thread odoCorrectThread = new Thread(odometryCorrection);
      odoCorrectThread.start();
      
      Thread navigationThread = new Thread(navigation);
      navigationThread.start();
    }

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
