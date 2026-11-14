// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Pneumatics;

import edu.wpi.first.hal.REVPHFaults;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticHub;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.PneumaticsConstants;

/**
 * Pneumatics on a REV Pneumatic Hub. The compressor runs in digital (pressure switch) closed-loop
 * mode: the PH runs it while the robot is enabled and the switch says "not full". One single
 * solenoid valve can be toggled or held open from the controller.
 */
public class PneumaticsSubsystem extends SubsystemBase {

  private static PneumaticsSubsystem INSTANCE = null;

  private final String tableKey = "Pneumatics/";

  private final PneumaticHub ph;
  private final Compressor compressor;
  private final Solenoid valve;

  private final Alert compressorFaultAlert =
      new Alert(
          "Pneumatics: compressor fault (over current or not connected)", AlertType.kError);
  private final Alert valveFaultAlert =
      new Alert(
          "Pneumatics: valve output disabled (short / over current) - check coil wiring",
          AlertType.kError);

  private final Timer compressorRunTimer = new Timer();
  private boolean compressorTimedOut = false;
  private final Alert compressorTimeoutAlert =
      new Alert(
          "Pneumatics: compressor ran too long without reaching pressure - check the switch and"
              + " air leaks. Restart robot code to reset.",
          AlertType.kError);

  private boolean lowPressure = false;

  public PneumaticsSubsystem() {
    ph = new PneumaticHub(PneumaticsConstants.PH_CAN_ID);
    compressor = ph.makeCompressor();
    compressor.enableDigital();
    valve = ph.makeSolenoid(PneumaticsConstants.VALVE_CHANNEL);
  }

  @Override
  public void periodic() {
    updateCompressorWatchdog();

    // The switch reads true when the system is not full.
    lowPressure = compressor.getPressureSwitchValue();

    REVPHFaults faults = ph.getFaults();
    boolean compressorFault =
        hasCompressorFault(faults.CompressorOverCurrent, faults.CompressorOpen);
    boolean valveFault = faults.SolenoidOverCurrent || valve.isDisabled();

    compressorFaultAlert.set(compressorFault);
    valveFaultAlert.set(valveFault);

    SmartDashboard.putBoolean(tableKey + "LowPressure", lowPressure);
    SmartDashboard.putBoolean(tableKey + "CompressorRunning", compressor.isEnabled());
    SmartDashboard.putBoolean(tableKey + "CompressorFault", compressorFault);
    SmartDashboard.putBoolean(tableKey + "CompressorTimedOut", compressorTimedOut);
    SmartDashboard.putBoolean(tableKey + "ValveOn", valve.get());
    SmartDashboard.putBoolean(tableKey + "ValveFault", valveFault);
    SmartDashboard.putNumber(tableKey + "SolenoidVoltage", ph.getSolenoidsVoltage());
    // Only a real number if a REV analog pressure sensor is on PH analog port 0.
    SmartDashboard.putNumber(
        tableKey + "PressurePSI (sensor only)",
        ph.getPressure(PneumaticsConstants.PRESSURE_SENSOR_CHANNEL));
  }

  /** Turns the compressor off if it runs too long without the switch saying "full". */
  private void updateCompressorWatchdog() {
    if (compressor.isEnabled()) {
      compressorRunTimer.start();
    } else {
      compressorRunTimer.stop();
      compressorRunTimer.reset();
    }
    if (!compressorTimedOut
        && compressorRunTimer.hasElapsed(PneumaticsConstants.COMPRESSOR_MAX_RUN_SECONDS)) {
      compressor.disable();
      compressorTimedOut = true;
    }
    compressorTimeoutAlert.set(compressorTimedOut);
  }

  /** True when the pressure switch says the system is not full. */
  public boolean isLowPressure() {
    return lowPressure;
  }

  /** Each press flips the valve on/off. */
  public Command toggleValveCommand() {
    return runOnce(valve::toggle).withName("ToggleValve");
  }

  /** Valve open while the command runs, closed when it ends. */
  public Command holdValveOpenCommand() {
    return startEnd(() -> valve.set(true), () -> valve.set(false)).withName("HoldValveOpen");
  }

  /** Any active PH compressor fault. */
  public static boolean hasCompressorFault(boolean overCurrent, boolean notConnected) {
    return overCurrent || notConnected;
  }

  public static synchronized PneumaticsSubsystem getInstance() {
    if (INSTANCE == null) INSTANCE = new PneumaticsSubsystem();
    return INSTANCE;
  }
}
