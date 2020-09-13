package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.KauriCheck;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;


@Getter
@Setter
public class KauriPunishEvent extends AtlasEvent implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final KauriCheck check;

    public KauriPunishEvent(Player player, KauriCheck check) {
        this.player = player;
        this.check = check;
    }
}
