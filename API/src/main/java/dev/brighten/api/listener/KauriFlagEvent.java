package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.KauriCheck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Getter
@Setter
public class KauriFlagEvent extends AtlasEvent implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final KauriCheck check;
    private final String information;
}
