# 6220_2026-Pneumatics

WPILib 2026 command-based robot project (Java) for FRC Team 6220. It holds
the pneumatics subsystem for a CTRE PCM with a digital pressure switch: a
low-pressure flag from the switch and an Alert for PCM compressor faults.
There is no analog PSI reading. Compressor control stays at the PCM's
default closed-loop (pressure switch) mode.

Before deploying, fill in the TODO values in
`src/main/java/frc/robot/Constants.java` (PCM CAN ID).

Build and test:

    ./gradlew build
    ./gradlew test

Not tested on hardware.
