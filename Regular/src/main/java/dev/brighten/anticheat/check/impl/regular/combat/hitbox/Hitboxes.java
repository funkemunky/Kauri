package dev.brighten.anticheat.check.impl.regular.combat.hitbox;

import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    public float buffer;
}