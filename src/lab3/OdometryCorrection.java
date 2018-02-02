/*
 * OdometryCorrection.java
 */
package lab3;


import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.hardware.Sound; 




public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private static final EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1")); // Instantiate the EV3 Color Sensor

  private double vCenter = 3.8;
  private double squareSize = 30;
  
  private double currentx = 0.0;
  private double currenty = 0.0;
  private double currentT = 0.0;
  private int countx = 0, county = 0;

  
  
  /**
   * 
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions {

    this.odometer = Odometer.getOdometer();
    Sound.setVolume(100);
    lightSensor.setFloodlight(Color.WHITE);     // set the sensor floodlight to white ( can be any colour)
  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;

    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      	float [] RGB = new float[3];
      	lightSensor.getRGBMode().fetchSample(RGB, 0);
      	      	
      	currentx = odometer.getXYT()[0];
      	currenty = odometer.getXYT()[1];
      	currentT = odometer.getXYT()[2];
      	
      	
      	if(isLine(RGB)){
      		Sound.beep();
			
      		if ((currentT > 0 && currentT < 10) || (currentT < 360 && currentT > 350)) {
      			currenty = county*squareSize - vCenter;
      			county++;
				
			} else if(currentT > 80 && currentT < 100) {
				currentx = countx*squareSize - vCenter;
				countx++;

				
			} else if(currentT > 170 && currentT < 190) {
				county--;
				currenty = county*squareSize + vCenter;
	
			} else if(currentT > 260 && currentT < 280) {	
				countx--;
				currentx = countx*squareSize + vCenter;
				
			}
      		// Displays line count
            LCD.drawString("countx =" + countx, 0, 4);
            LCD.drawString("county =" + county, 0, 5);
    		
      	}
      
      odometer.setXYT(currentx, currenty, currentT);


      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
  
	
	/**
	 * A method to determine if black line
	 * 
	 * @return 
	 */ 
  private boolean isLine(float[] RGB) {
	  if((RGB[0] < 0.12) && (RGB[1] < 0.12) && (RGB[2] <0.12)) {
		  return true;
	  } else {
		  return false;
	  }
  }
 
}

