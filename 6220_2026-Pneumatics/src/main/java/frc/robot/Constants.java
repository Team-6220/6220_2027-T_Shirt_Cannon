// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

public final class Constants {

  /** Pneumatics: REV Pneumatic Hub (PH) with a digital pressure switch. */
  public static final class PneumaticsConstants {
    // REV Pneumatic Hub CAN ID. Factory default is 1. TODO: confirm in REV Hardware Client.
    public static final int PH_CAN_ID = 15;

    // PH solenoid port the valve is plugged into (0-15). TODO: set to the real port.
    public static final int VALVE_CHANNEL = 0;

    // Analog port 0 on the PH. Only meaningful if a REV analog pressure sensor is plugged in.
    public static final int PRESSURE_SENSOR_CHANNEL = 0;
  }

  /** Driver controller. */
  public static final class OperatorConstants {
    public static final int DRIVER_CONTROLLER_PORT = 0;
  }
}
