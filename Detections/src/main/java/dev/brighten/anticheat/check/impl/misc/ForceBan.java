package dev.brighten.anticheat.check.impl.misc;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "ForceBan", description = "Force ban detection", punishVL = 0, executable = true,
        checkType = CheckType.GENERAL)
public class ForceBan extends Check {


}
