package cc.funkemunky.anticheat.api.event;

import cc.funkemunky.anticheat.api.checks.Check;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PlayerCheatEvent extends Event implements Cancellable {
    private Player player;
    private Check check;
    @Setter
    private boolean cancelled;
    @Getter
    private static HandlerList handlers = new HandlerList();

    public PlayerCheatEvent(Player player, Check check) {
        this.player = player;
        this.check = check;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
