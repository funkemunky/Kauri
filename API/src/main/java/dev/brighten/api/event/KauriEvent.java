package dev.brighten.api.event;

import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.KauriCheck;
import dev.brighten.api.event.result.CancelResult;
import dev.brighten.api.event.result.FlagResult;
import dev.brighten.api.event.result.PunishResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import java.util.List;

public interface KauriEvent {

    PunishResult onPunish(Player player, KauriCheck check, String broadcastMessage, List<String> commands, boolean cancelled);

    FlagResult onFlag(Player player, KauriCheck check, String information, boolean cancelled);

    CancelResult onCancel(Player player, CancelType cancelType, boolean cancelled);

    EventPriority priority();


}
