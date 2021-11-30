package dev.brighten.anticheat.check.impl.premium.hitboxes;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Hitboxes (B)", description = "Very sensitive hitboxes check", punishVL = 20,
        checkType = CheckType.HITBOX, devStage = DevStage.ALPHA)
public class HitboxesB extends Check {
}
