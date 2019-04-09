package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.event.TickEvent;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventMethod;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.utils.Init;

@Init
public class FunkeListeners implements AtlasListener {

    @Listen
    public void onTickEvent(TickEvent event) {
        Atlas.getInstance().executeTask(() -> Kauri.getInstance().getDataManager().getDataObjects().keySet().forEach(key -> {
            PlayerData data = Kauri.getInstance().getDataManager().getDataObjects().get(key);

            data.getActionProcessor().update(data);
        }));
    }
}
