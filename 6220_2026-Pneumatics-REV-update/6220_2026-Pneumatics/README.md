# 6220_2026-Pneumatics

WPILib 2026 (GradleRIO 2026.2.1) command-based Java project for FRC Team 6220.
Pneumatics test code for a **REV Pneumatic Hub** on a roboRIO 2.0.

What it does:
- Runs the compressor in digital (pressure switch) closed-loop mode while the
  robot is enabled; it stops when the switch says full (~120 PSI).
- One single solenoid valve on PH port `VALVE_CHANNEL`:
  - **A** button (Xbox controller, port 0): toggle the valve on/off.
  - **B** button: hold to open, release to close.
- Dashboard keys under `Pneumatics/`: LowPressure, CompressorRunning,
  CompressorFault, ValveOn, ValveFault, SolenoidVoltage, PressurePSI (sensor only).

Before deploying, check `src/main/java/frc/robot/Constants.java`:
- `PH_CAN_ID` (factory default 1; confirm in REV Hardware Client)
- `VALVE_CHANNEL` (the PH port the valve is plugged into, 0-15)

Build and test:

    ./gradlew build

Not tested on hardware. Do not accept "import/upgrade to 2027" prompts.
