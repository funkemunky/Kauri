package dev.brighten.anticheat.premium;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.premium.impl.*;
import dev.brighten.anticheat.premium.impl.autoclicker.AutoclickerD;
import dev.brighten.anticheat.premium.impl.autoclicker.AutoclickerH;
import dev.brighten.anticheat.premium.impl.autoclicker.AutoclickerI;
import dev.brighten.anticheat.premium.impl.autoclicker.AutoclickerJ;
import dev.brighten.anticheat.premium.impl.hitboxes.ReachB;

@Init(priority = Priority.LOWEST)
public class PremiumChecks {

    public PremiumChecks() {
        MiscUtils.printToConsole("&aThanks for purchasing Kauri Ara.");
        Check.register(new VelocityB());
        Check.register(new ReachB());
        //Check.register(new Motion());
        Check.register(new AimI());
        Check.register(new AimG());
        Check.register(new AimH());
        Check.register(new AutoclickerD());
        //Check.register(new AutoclickerE());
        Check.register(new InventoryA());
        Check.register(new InventoryB());
        Check.register(new AutoclickerH());
        Check.register(new AutoclickerI());
        Check.register(new AutoclickerJ());
    }
}
