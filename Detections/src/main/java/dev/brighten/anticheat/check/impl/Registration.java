package dev.brighten.anticheat.check.impl;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.utils.ClassScanner;

@Init(priority = Priority.LOWEST)
public class Registration implements CheckRegister {

    public Registration() {
        MiscUtils.printToConsole("&aLoading Kauri Free checks...");
        registerChecks();
    }

    @Override
    public void registerChecks() {
        for (WrappedClass aClass : ClassScanner.getClasses(CheckInfo.class,
                "dev.brighten.anticheat.check.impl")) {
            Check.register(aClass.getParent());
        }
    }
}
