package dev.brighten.anticheat.listeners.generalChecks;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.impl.TickEvent;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;

@Init
public class LagCheck extends AtlasEvent {

    @ConfigSetting(path = "general", name = "lagCheck")
    private static boolean enabled = true;

    @Listen
    public void onEvent(TickEvent event) {
        if(!enabled || !Kauri.INSTANCE.enabled) return;

        //We do this to ensure no one is abusing fake lag attempts.
        Kauri.INSTANCE.dataManager.dataMap.values()
                .parallelStream()
                .forEach(data -> {
                    if(data.lagInfo.lagging) {
                        data.lagTicks++;

                        if(data.lagTicks > 400) {
                            RunUtils.task(() ->
                                    data.getPlayer().kickPlayer("Kicked for timing out. (Extreme lag?)"));
                        }
                    } else {
                        data.noLagTicks++;
                        data.lagTicks = 0;
                    }
                });
    }
}
