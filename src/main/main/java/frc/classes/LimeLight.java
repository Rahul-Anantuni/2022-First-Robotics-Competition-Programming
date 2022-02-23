// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.classes;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/** Add your docs here. */
public class LimeLight {
    private static final double TX_MAX      = 29.80;
    private static final double TY_MAX      = 24.85;
    private static final double X_TOLERANCE =  1.0;
    private static final double Y_TOLERANCE =  1.0;
    private static final int MAX_FAILURES   = 10;

    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry tv = table.getEntry("tv");

    private static final double MAX_DRIVE         =  0.85; //0.75 PracticeBot
	private static final double MIN_DRIVE         =  0.50; //0.40 PracticeBot
	private static final double MIN_STEER         =  0.30; //0.30 PracticeBot
	private static final double MIN_STEER_STOPPED =  0.45; //0.45 PracticeBot

    private double driveCommand = 0.0;
    private double steerCommand = 0.0;
    private int failureCount = 0;

    public double getSteerCommand(){
        //Left/Right Steering Speed
        return steerCommand;
    }

    public double getDriveCommand(){
        //Forward/Reverse Driving Speed
        return driveCommand;
    }
  
    public double getTX(){
        //X axis error from crosshair
        //  Used to compute Steering Speed
        return tx.getDouble(0.0);
    }
                                    
    public double getTY(){
        //Y axis error from crosshair
        //  Used to compute Drive Speed
        return ty.getDouble(0.0);
    }
  
    public boolean getTV(){
        //tv = 1.0, then target aquired
        return (tv.getDouble(0.0) > 0.0);
    }
  
    public void setLEDs(boolean on){
        //Value of 3 turns LEDs on, 1 turns LEDs off
        if (on){
            table.getEntry("ledMode").setNumber(3);
        }else{
            table.getEntry("ledMode").setNumber(1);
        }
    }

    public void setVisionMode(boolean on){
        //Value of 0 is vision processing, 1 is Drive mode
        if (on){
            table.getEntry("camMode").setNumber(0);
        }else{
            table.getEntry("camMode").setNumber(1);
        }
        //Set LED state based on Mode
        setLEDs(on);
        //Reset Failure Count
        failureCount = 0;
    }

    public boolean calcSpeeds(){
        double minSteer = MIN_STEER;
        boolean validTarget = getTV();
        //Test for Valid Target
        if (validTarget){
            failureCount = 0;
            //Normalize speeds to -1.0 to 1.0
            steerCommand = getTX()/TX_MAX;
            driveCommand = getTY()/TY_MAX;

            if (Math.abs(getTY()) > Y_TOLERANCE){
                //Normalize between min & max driving speeds
                if (Math.abs(driveCommand) < MIN_DRIVE){
                    driveCommand += Math.signum(driveCommand)*MIN_DRIVE; 
                }
                else if (Math.abs(driveCommand)>MAX_DRIVE){
                    driveCommand = Math.signum(driveCommand)*MAX_DRIVE;
                }

                //If rotating, test for minimum speed
                if (Math.abs(getTX()) > X_TOLERANCE){
                   if (Math.abs(steerCommand) < minSteer ){
                       steerCommand += Math.signum(steerCommand)*minSteer;
                   } 
                }else{
                    steerCommand = 0.0;
                }
            }else{
                //Stop forward/reverse speed
                driveCommand = 0.0;
                //Robot is not moving update minimum steering speed
                minSteer = MIN_STEER_STOPPED;
            }
        }else{
            failureCount++;
            driveCommand=0.0;
            steerCommand=0.0;
        }
        return (validTarget || failureCount <= MAX_FAILURES);
    }

    public boolean withinTolerance(){
        return (getTV() &&
                Math.abs(getTX()) < X_TOLERANCE &&
                Math.abs(getTY()) < Y_TOLERANCE);
    }
}
