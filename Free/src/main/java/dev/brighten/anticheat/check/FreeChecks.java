package dev.brighten.anticheat.check;

import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerA;
import dev.brighten.anticheat.check.impl.combat.hitbox.ReachA;
import dev.brighten.anticheat.check.impl.movement.fly.FlyA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallA;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedA;
import dev.brighten.anticheat.check.impl.packets.badpackets.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Init(priority = Priority.LOWEST)
public class FreeChecks implements CheckRegister {

    private BukkitTask msgingTask;

    public FreeChecks() {
        registerChecks();

        ConfigDefault<Boolean> paidMsgingToOps =
                new ConfigDefault<>(true, "misc.upgradeMessaging.messageOps", Kauri.INSTANCE);

        TextComponent message = new TextComponent();
        TextComponent lBracket = new TextComponent("["), kauri = new TextComponent("Kauri"),
                rbracket = new TextComponent("]");
        lBracket.setColor(ChatColor.DARK_GRAY);
        kauri.setColor(ChatColor.GOLD);
        kauri.setBold(true);
        rbracket.setColor(ChatColor.DARK_GRAY);

        message.addExtra(lBracket);
        message.addExtra(kauri);
        message.addExtra(rbracket);

        TextComponent buyMessage = new TextComponent("We appreciate you using Kauri." +
                " If you would like to help us out while unlocking more, please consider ");

        buyMessage.setColor(ChatColor.GRAY);

        TextComponent purchasing = new TextComponent("purchasing a premium package");
        purchasing.setColor(ChatColor.WHITE);
        purchasing.setItalic(true);
        purchasing.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new BaseComponent[]{new TextComponent(Color.Yellow + Color.Italics + "Open URL")}));
        purchasing.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://funkemunky.cc/shop"));

        message.addExtra(buyMessage);
        message.addExtra(purchasing);

        msgingTask = RunUtils.taskTimerAsync(() -> {
            if (!Kauri.INSTANCE.usingPremium && !Kauri.INSTANCE.usingAra) {
                if (paidMsgingToOps.get()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isOp()) continue;

                        player.spigot().sendMessage(message);
                    }
                }

                MiscUtils.printToConsole("&fWe appreciate you using Kauri. If you would like to help us out " +
                        "while unlocking more, please consider purchasing a " +
                        "premium package at &e&ohttps://funkemunky.cc/shop" );
            } else msgingTask.cancel();
        }, Kauri.INSTANCE, 20 * 30, 20 * 240);
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
        //Check.register(new BadPacketsE());
        Check.register(new BadPacketsF());
        Check.register(new BadPacketsG());
        Check.register(new BadPacketsH());
        Check.register(new BadPacketsI());
        Check.register(new BadPacketsK());
        Check.register(new BadPacketsL());
        Check.register(new BadPacketsM());
        //Check.register(new BadPacketsN())
    }
}
