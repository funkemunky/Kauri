package dev.brighten.anticheat.utils.lunar.event;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class PlayerEvent extends BaseEvent {
	private Player player;

	public PlayerEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return player.getUniqueId();
	}
}
