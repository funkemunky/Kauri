package cc.funkemunky.anticheat.api.lunar.event;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class AuthenticateEvent extends AtlasEvent implements Cancellable {

    private Player player;
    @Setter
    private boolean cancelled;

    public AuthenticateEvent(Player player) {
        this.player = player;
    }
}
