package cc.funkemunky.anticheat.api.events;

import cc.funkemunky.api.event.system.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TickEvent extends Event {
    private int currentTick;
}
