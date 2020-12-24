package dev.brighten.anticheat.check;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerA;
import dev.brighten.anticheat.check.impl.combat.hitbox.ReachA;
import dev.brighten.anticheat.check.impl.movement.fly.FlyA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallA;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedA;
import dev.brighten.anticheat.check.impl.packets.badpackets.*;
import org.bukkit.scheduler.BukkitTask;

@Init(priority = Priority.LOWEST)
public class FreeChecks implements CheckRegister {

    private BukkitTask msgingTask;

    public FreeChecks() {
        registerChecks();

        msgingTask = RunUtils.taskTimerAsync(() -> {
            if (!Kauri.INSTANCE.usingPremium && !Kauri.INSTANCE.usingAra) {

                MiscUtils.printToConsole("&fWe appreciate you using Kauri. If you would like to help us out " +
                        "while unlocking more, please consider purchasing a " +
                        "premium package at &e&ohttps://funkemunky.cc/shop" );
            } else msgingTask.cancel();
        }, Kauri.INSTANCE, 40, 18000);
    }

    @Override
    public void registerChecks() {
        Check.register(new AutoclickerA());
        Check.register(new FlyA());
        Check.register(new NoFallA());
        Check.register(new ReachA());
        Check.register(new SpeedA());
        Check.register(new BadPacketsA());
        Check.register(new BadPacketsB());
        Check.register(new BadPacketsC());
        Check.register(new BadPacketsD());
        Check.register(new BadPacketsE());
        Check.register(new BadPacketsF());
        Check.register(new BadPacketsG());
        Check.register(new BadPacketsH());
        Check.register(new BadPacketsI());
        Check.register(new BadPacketsK());
        Check.register(new BadPacketsL());
        Check.register(new BadPacketsM());
        Check.register(new BadPacketsN());
        Check.register(new Timer());
    }
}
