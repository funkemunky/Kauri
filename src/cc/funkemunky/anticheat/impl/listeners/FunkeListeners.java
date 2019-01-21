package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.events.TickEvent;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventMethod;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.utils.Init;

@Init
public class FunkeListeners implements Listener {

    @EventMethod
    public void onTickEvent(TickEvent event) {
        Atlas.getInstance().executeTask(() -> Kauri.getInstance().getDataManager().getDataObjects().forEach(data -> data.getActionProcessor().update(data)));
    }
}
