package dev.brighten.anticheat.check.impl.combat.hitbox;

import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    public float buffer;
}