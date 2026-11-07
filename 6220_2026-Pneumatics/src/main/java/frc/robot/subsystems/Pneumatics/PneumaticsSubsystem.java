// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Pneumatics;

import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsControlModule;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.PneumaticsConstants;

/**
 * Pneumatics on a CTRE PCM with a digital pressure switch. The low-pressure flag comes from the
 * switch; there is no analog PSI reading. Compressor control is left at the PCM's default
 * closed-loop (pressure switch) mode, and a compressor fault never changes it.
 */
public class PneumaticsSubsystem extends SubsystemBase {

  private static PneumaticsSubsystem INSTANCE = null;

  private final String tableKey = "Pneumatics/";

  private final PneumaticsControlModule pcm;
  private final Compressor compressor;
  private final Alert compressorFaultAlert =
      new Alert(
          "Pneumatics: compressor fault (current too high, shorted, or not connected)",
          AlertType.kError);

  private boolean lowPressure = false;

  public PneumaticsSubsystem() {
    pcm = new PneumaticsControlModule(PneumaticsConstants.PCM_CAN_ID);
    compressor = pcm.makeCompressor();
  }

  @Override
  public void periodic() {
    // The switch reads true when the system is not full.
    lowPressure = compressor.getPressureSwitchValue();
    boolean compressorFault =
        hasCompressorFault(
            pcm.getCompressorCurrentTooHighFault(),
            pcm.getCompressorShortedFault(),
            pcm.getCompressorNotConnectedFault());

    compressorFaultAlert.set(compressorFault);

    SmartDashboard.putBoolean(tableKey + "LowPressure", lowPressure);
    SmartDashboard.putBoolean(tableKey + "CompressorRunning", compressor.isEnabled());
    SmartDashboard.putBoolean(tableKey + "CompressorFault", compressorFault);
  }

  /** True when the pressure switch says the system is not full. */
  public boolean isLowPressure() {
    return lowPressure;
  }

  /** Any active PCM compressor fault. */
  public static boolean hasCompressorFault(
      boolean currentTooHigh, boolean shorted, boolean notConnected) {
    return currentTooHigh || shorted || notConnected;
  }

  public static synchronized PneumaticsSubsystem getInstance() {
    if (INSTANCE == null) INSTANCE = new PneumaticsSubsystem();
    return INSTANCE;
  }
}
