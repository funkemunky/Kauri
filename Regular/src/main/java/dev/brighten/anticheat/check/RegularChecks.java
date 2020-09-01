package dev.brighten.anticheat.check;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.check.impl.combat.aim.*;
import dev.brighten.anticheat.check.impl.combat.hand.*;
import dev.brighten.anticheat.check.impl.combat.killaura.*;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerC;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerG;
import dev.brighten.anticheat.check.impl.combat.hitbox.Hitboxes;
import dev.brighten.anticheat.check.impl.movement.fly.FlyB;
import dev.brighten.anticheat.check.impl.movement.fly.FlyC;
import dev.brighten.anticheat.check.impl.movement.fly.FlyD;
import dev.brighten.anticheat.check.impl.movement.fly.FlyE;
import dev.brighten.anticheat.check.impl.movement.general.FastLadder;
import dev.brighten.anticheat.check.impl.movement.general.OmniSprint;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallB;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedB;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedC;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityA;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityC;
import dev.brighten.anticheat.check.impl.packets.Timer;
import dev.brighten.anticheat.check.impl.packets.exploits.*;
import dev.brighten.anticheat.check.impl.world.HealthSpoof;

@Init(priority = Priority.LOWEST)
public class RegularChecks implements CheckRegister {

    public RegularChecks() {
        MiscUtils.printToConsole("&aThanks for purchasing Kauri Premium.");
        registerChecks();
    }

    @Override
    public void registerChecks() {
        Check.register(new AutoclickerC());
        Check.register(new AutoclickerG());
        Check.register(new FlyB());
        Check.register(new FlyC());
        Check.register(new FlyD());
        Check.register(new FlyE());
        Check.register(new FastLadder());
        Check.register(new NoFallB());
        Check.register(new Hitboxes());
        Check.register(new AimA());
        Check.register(new AimB());
        Check.register(new AimC());
        Check.register(new AimD());
        //Check.register(new AimE());
        //Check.register(new AimF());
        Check.register(new SpeedB());
        Check.register(new SpeedC());
        Check.register(new KillauraA());
        Check.register(new KillauraB());
        Check.register(new KillauraC());
        Check.register(new KillauraD());
        Check.register(new KillauraE());
        Check.register(new KillauraF());
        Check.register(new OmniSprint());
        Check.register(new Timer());
        Check.register(new VelocityA());
        Check.register(new VelocityC());
        Check.register(new HandA());
        Check.register(new HandB());
        Check.register(new HandC());
        Check.register(new HandD());
        Check.register(new HandE());
        Check.register(new HandF());
        Check.register(new HealthSpoof());
        Check.register(new BookOp());
        Check.register(new BookEnchant());
        Check.register(new PacketSpam());
        Check.register(new SignOp());
        Check.register(new SignCrash());
    }
}
