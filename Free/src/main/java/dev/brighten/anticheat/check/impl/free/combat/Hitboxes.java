package dev.brighten.anticheat.check.impl.free.combat;

import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 10, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    public float buffer;
}