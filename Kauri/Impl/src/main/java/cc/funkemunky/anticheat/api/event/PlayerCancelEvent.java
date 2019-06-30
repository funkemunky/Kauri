package cc.funkemunky.anticheat.api.event;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.events.AtlasEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PlayerCancelEvent extends Event implements Cancellable {
    private Player player;
    private Check check;
    @Setter
    private CancelType type;
    @Setter
    private boolean cancelled;
    private static HandlerList handlers = new HandlerList();

    public PlayerCancelEvent(Player player, Check check, CancelType type) {
        this.player = player;
        this.check = check;
        this.type = type;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
