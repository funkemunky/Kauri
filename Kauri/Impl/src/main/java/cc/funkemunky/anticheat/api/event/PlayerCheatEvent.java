package cc.funkemunky.anticheat.api.event;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class PlayerCheatEvent extends AtlasEvent implements Cancellable {
    private Player player;
    private Check check;
    @Setter
    private boolean cancelled;

    public PlayerCheatEvent(Player player, Check check) {
        this.player = player;
        this.check = check;
    }
}
