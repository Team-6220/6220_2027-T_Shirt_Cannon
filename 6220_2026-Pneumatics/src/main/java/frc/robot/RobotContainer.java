// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.Pneumatics.PneumaticsSubsystem;

public class RobotContainer {
  /* Subsystems */
  private final PneumaticsSubsystem pneumatics = PneumaticsSubsystem.getInstance();

  private final CommandXboxController driver =
      new CommandXboxController(OperatorConstants.DRIVER_CONTROLLER_PORT);

  public RobotContainer() {
    configureBindings();
  }

  private void configureBindings() {
    // A: toggle the valve (on/off) each press.
    driver.a().onTrue(pneumatics.toggleValveCommand());
    // B: hold to open the valve, release to close it.
    driver.b().whileTrue(pneumatics.holdValveOpenCommand());
  }

  public Command getAutonomousCommand() {
    return Commands.none();
  }
}
