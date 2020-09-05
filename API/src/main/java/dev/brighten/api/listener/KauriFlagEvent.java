package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.KauriCheck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class KauriFlagEvent extends AtlasEvent implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;
    public final Player player;
    public final KauriCheck check;
    public final String information;
}
