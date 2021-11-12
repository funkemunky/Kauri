package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.KauriCheck;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KauriPunishEvent extends AtlasEvent implements Cancellable {
    private boolean cancelled;
    private final Player player;
    private final KauriCheck check;
    private String broadcastMessage;
    private List<String> commands;

    public KauriPunishEvent(Player player, KauriCheck check, String broadcastMessage, List<String> commands) {
        this.player = player;
        this.check = check;
        this.broadcastMessage = broadcastMessage;
        this.commands = new ArrayList<>(commands);
    }
}
