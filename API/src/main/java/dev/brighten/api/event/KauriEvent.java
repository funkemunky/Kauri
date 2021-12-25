package dev.brighten.api.event;

import dev.brighten.api.check.KauriCheck;
import dev.brighten.api.event.result.EventResult;
import dev.brighten.api.event.result.PunishResult;
import org.bukkit.entity.Player;

import java.util.List;

public interface KauriEvent {

    PunishResult onPunish(Player player, KauriCheck check, String broadcastMessage, List<String> commands);

    EventResult onFlag(Player player, KauriCheck check, String information);


}
