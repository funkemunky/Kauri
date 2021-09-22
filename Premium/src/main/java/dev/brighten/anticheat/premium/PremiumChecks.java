package dev.brighten.anticheat.premium;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.premium.impl.*;
import dev.brighten.anticheat.premium.impl.autoclicker.*;
import dev.brighten.anticheat.premium.impl.hitboxes.ReachB;

@Init(priority = Priority.LOWEST)
public class PremiumChecks implements CheckRegister {

    public PremiumChecks() {
        MiscUtils.printToConsole("&aThanks for purchasing Kauri Ara.");
        registerChecks();
        Kauri.INSTANCE.usingAra = true;
    }

    @Override
    public void registerChecks() {
        Check.register(new VelocityB());
        Check.register(new ReachB());
        Check.register(new Motion());
        Check.register(new AimH());
        Check.register(new AimI());
        Check.register(new AimG());
        //Check.register(new AimJ());
        Check.register(new HealthSpoof());
        Check.register(new AutoclickerD());
        Check.register(new AutoclickerF());
        Check.register(new AutoclickerE());
        Check.register(new InventoryA());
        Check.register(new InventoryB());
        Check.register(new InventoryC());
        Check.register(new AutoclickerH());
        Check.register(new AutoclickerJ());
    }
}
