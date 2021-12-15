package dev.brighten.anticheat.check.impl;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.CheckRegister;

@Init(priority = Priority.LOWEST)
public class PremiumChecks implements CheckRegister {

    public PremiumChecks() {
        MiscUtils.printToConsole("&aThanks for purchasing Kauri Ara.");
        registerChecks();
        Kauri.INSTANCE.usingAra = true;
    }

    @Override
    public void registerChecks() {

    }
}
