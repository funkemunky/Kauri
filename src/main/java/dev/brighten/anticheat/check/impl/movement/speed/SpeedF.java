package dev.brighten.anticheat.check.impl.movement.speed;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;

@CheckInfo(name = "Speed (F)", description = "Checks for impossibly large movements.", punishVL = 3)
public class SpeedF extends Check {
}
