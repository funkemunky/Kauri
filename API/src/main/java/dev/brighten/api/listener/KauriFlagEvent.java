package dev.brighten.api.listener;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Cancellable;
import dev.brighten.api.check.KauriCheck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class KauriFlagEvent extends AtlasEvent implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;
    public final KauriCheck check;
}
