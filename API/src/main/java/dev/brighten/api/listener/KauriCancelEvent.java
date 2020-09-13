package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.CancelType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class KauriCancelEvent extends AtlasEvent implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private CancelType cancelType;

    public KauriCancelEvent(Player player, CancelType cancelType) {
        this.player = player;
        this.cancelType = cancelType;
    }
}
