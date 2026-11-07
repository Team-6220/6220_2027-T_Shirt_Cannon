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
