package dev.brighten.anticheat.check.impl.combat.hitbox;

import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Hitboxes (B)", description = "Very sensitive hitboxes check", punishVL = 20,
        checkType = CheckType.HITBOX, devStage = DevStage.ALPHA)
@Cancellable(cancelType = CancelType.ATTACK)
public class HitboxesB extends Check {
}
