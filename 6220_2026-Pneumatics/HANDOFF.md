# Handoff: FRC 6220 pneumatics test bench (read this first)

Paste this whole file to Claude on the new computer. It replaces the earlier
conversation.

## Goal
Make a compressor on a test bench run from robot code. Nothing else.
Keep the project tiny: 6 Java files. Do not add subsystems, commands,
vendor libraries, or "improvements". Do not rewrite files that already build.

## Hardware (confirmed from photos)
- Controller: NI roboRIO 2.0 (boots from its microSD card, must be in the slot)
- Pneumatics: CTRE Pneumatics Control Module (PCM), NOT a REV Pneumatic Hub
- Pressure sensing: a digital pressure switch wired to the PCM. There is NO
  analog pressure sensor, so there is no PSI reading in code.
- Power: battery -> REV Power Distribution Hub (PDH) -> roboRIO and PCM
- Team number: 6220

## Software
- WPILib 2026 (GradleRIO 2026.2.1), Java, command-based template.
- DECIDED: this bench uses the roboRIO 2.0, so the project stays on WPILib 2026.
  WPILib 2027 alpha 7 cannot run on a roboRIO: it only ships `linuxsystemcore`
  native libraries and deploys to a SystemCore target (checked on the installed
  2027 alpha 7). Do not switch to 2027, and do NOT accept "import/upgrade
  project" prompts.
- The team's main robot project (6220_2026-Imported) is on 2027 alpha 7 for
  SystemCore. That is separate; don't copy code or build files from it.
- Windows laptop needs NI FRC Game Tools 2026 (Driver Station + roboRIO
  Imaging Tool).
- WPILib 2026 packages are `edu.wpi.first.*` (2027 uses `org.wpilib.*`; that
  mismatch causes hundreds of errors).
- Only vendordep needed: WPILibNewCommands (comes with the template).

## How it works
`pcm.makeCompressor()` puts the PCM in closed-loop mode: the PCM runs the
compressor by itself while the roboRIO is ENABLED in the FRC Driver Station
and the pressure switch says "not full", and stops at about 120 PSI.
Do not call `disable()`, `enableAnalog()`, or `enableHybrid()`.

## Build it on a fresh computer
1. WPILib VS Code 2026 -> W icon -> "Create a new project" -> Template -> Java
   -> Command Robot. Team 6220. Folder name: 6220_2026-Pneumatics.
2. Delete the template's `src/main/java/frc/robot/subsystems/ExampleSubsystem.java`,
   `src/main/java/frc/robot/commands/` (ExampleCommand, Autos) - nothing uses them.
3. Replace/create exactly the files below (contents at the end of this file).
4. Terminal inside WPILib VS Code: `.\gradlew build` (Windows) or
   `./gradlew build` (Mac). Expect BUILD SUCCESSFUL and 2 tests passed.

| File | Path |
|---|---|
| Main.java | src/main/java/frc/robot/Main.java |
| Robot.java | src/main/java/frc/robot/Robot.java |
| RobotContainer.java | src/main/java/frc/robot/RobotContainer.java |
| Constants.java | src/main/java/frc/robot/Constants.java |
| PneumaticsSubsystem.java | src/main/java/frc/robot/subsystems/Pneumatics/PneumaticsSubsystem.java |
| PneumaticsSubsystemTest.java | src/test/java/frc/robot/subsystems/Pneumatics/PneumaticsSubsystemTest.java |

The template's build.gradle already includes JUnit; if `src/test` gives
"package org.junit does not exist", check build.gradle has
`testImplementation 'org.junit.jupiter:junit-jupiter:...'` and `test { useJUnitPlatform() }`.

## Open TODO
- `PCM_CAN_ID` in Constants.java is -1 (placeholder, crashes at startup).
  Set it to the PCM's real CAN ID (factory default is 0; check the roboRIO web
  dashboard or Phoenix Tuner).

## Where we are (hardware status)
1. Code written, builds, tests pass (on the Mac). DONE
2. Ethernet from laptop to roboRIO: link is up. DONE
3. roboRIO boots: NOT YET. Status light stayed solid orange and the roboRIO
   did not answer `ping roborio-6220-FRC.local`. The microSD card had been
   left in a USB card reader; it must be in the roboRIO's slot. If the Status
   light stays solid with the card in, the card needs the roboRIO 2.0 2026
   image (roboRIO Imaging Tool in NI FRC Game Tools, Windows only).
4. Set PCM_CAN_ID. TODO
5. Deploy: `.\gradlew deploy` (needs step 3). TODO
6. Enable in FRC Driver Station (Windows), compressor should run and stop at
   ~120 PSI. TODO

## How to check it works
- Driver Station: Communications and Robot Code lights green.
- Dashboard (SmartDashboard keys): `Pneumatics/LowPressure` true while filling,
  false when full; `Pneumatics/CompressorRunning`; `Pneumatics/CompressorFault`
  should be false.
- Bench: compressor starts on Enable, stops at ~120 PSI on the gauge, stops on
  Disable. Not yet tested on hardware.

---

## File contents

### Main.java
```java
package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
```

### Robot.java
```java
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class Robot extends TimedRobot {
  private Command m_autonomousCommand;

  private final RobotContainer m_robotContainer;

  public Robot() {
    m_robotContainer = new RobotContainer();
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
```

### RobotContainer.java
```java
package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.Pneumatics.PneumaticsSubsystem;

public class RobotContainer {
  /* Subsystems */
  private final PneumaticsSubsystem pneumatics = PneumaticsSubsystem.getInstance();

  public RobotContainer() {
    configureBindings();
  }

  private void configureBindings() {}

  public Command getAutonomousCommand() {
    return Commands.none();
  }
}
```

### Constants.java
```java
package frc.robot;

public final class Constants {

  /** Pneumatics: CTRE PCM with a digital pressure switch. No analog pressure sensor. */
  public static final class PneumaticsConstants {
    // TODO: set the PCM CAN ID (check Phoenix Tuner). -1 is a placeholder and is not a valid ID.
    public static final int PCM_CAN_ID = -1;
  }
}
```

### PneumaticsSubsystem.java
```java
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
```

### PneumaticsSubsystemTest.java
```java
package frc.robot.subsystems.Pneumatics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PneumaticsSubsystemTest {

  @Test
  void noFaultWhenAllClear() {
    assertFalse(PneumaticsSubsystem.hasCompressorFault(false, false, false));
  }

  @Test
  void eachFaultTripsAlert() {
    assertTrue(PneumaticsSubsystem.hasCompressorFault(true, false, false));
    assertTrue(PneumaticsSubsystem.hasCompressorFault(false, true, false));
    assertTrue(PneumaticsSubsystem.hasCompressorFault(false, false, true));
  }
}
```
