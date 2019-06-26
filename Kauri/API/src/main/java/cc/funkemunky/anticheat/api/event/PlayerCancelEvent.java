package cc.funkemunky.anticheat.api.event;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class PlayerCancelEvent extends AtlasEvent implements Cancellable {
    private Player player;
    private Check check;
    @Setter
    private CancelType type;
    @Setter
    private boolean cancelled;

    public PlayerCancelEvent(Player player, Check check, CancelType type) {
        this.player = player;
        this.check = check;
        this.type = type;
    }
}
