package dev.brighten.anticheat.utils.lunar.event.impl;

import dev.brighten.anticheat.utils.lunar.event.PlayerEvent;
import org.bukkit.entity.Player;

public class AuthenticateEvent extends PlayerEvent {

    public AuthenticateEvent(Player player) {
        super(player);
    }

}
