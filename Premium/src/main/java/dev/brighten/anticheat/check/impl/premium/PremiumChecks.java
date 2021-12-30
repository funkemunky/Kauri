package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.utils.ClassScanner;

@Init(priority = Priority.LOWEST)
public class PremiumChecks implements CheckRegister {

    public PremiumChecks() {
        MiscUtils.printToConsole("&aThanks for purchasing Kauri Ara.");
        registerChecks();
        Kauri.INSTANCE.usingAra = true;
    }

    @Override
    public void registerChecks() {
        for (WrappedClass aClass : ClassScanner.getClasses(CheckInfo.class,
                "dev.brighten.anticheat.check.impl.premium")) {
            Check.register(aClass.getParent());
        }
    }
}