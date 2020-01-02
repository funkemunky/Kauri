package dev.brighten.anticheat.check.impl.combat.hand;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (D)", description = "Identifies common blocking patterns", checkType = CheckType.HAND,
        developer = true)
public class HandD extends Check {
}
