package cc.funkemunky.anticheat.api.log;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.ItemBuilder;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class LogHandler {

    public void viewLoggedVLByText(CommandSender sender, UUID toView) {
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        if(!Kauri.getInstance().getLoggerManager().getLogs().containsKey(toView)) {
            sender.sendMessage(Color.Red + "No logs found for this player.");
        } else {
            Log log = Kauri.getInstance().getLoggerManager().getLogs().get(toView);

            log.getViolations().keySet().stream()
                    .filter(key -> log.getViolations().get(key) > 0)
                    .forEach(key -> sender.sendMessage(Color.Yellow + key + ": " + Color.White + log.getViolations().get(key) + (log.isBanned() && log.getBannedCheck().equals(key) ? Color.Red + Color.Italics + "[BANNED]" : "")));
        }
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    public void viewLoggedVLByGUI(Player player, UUID toView) {
        if(!Kauri.getInstance().getLoggerManager().getLogs().containsKey(toView)) {
            player.sendMessage(Color.Red + "No logs found for this player.");
        } else {
            Log log = Kauri.getInstance().getLoggerManager().getLogs().get(toView);

            Map<Check, Integer> violations = new HashMap<>();

            log.getViolations().keySet().stream()
                    .filter(key -> log.getViolations().get(key) > 0)
                    .forEach(key -> violations.put(Kauri.getInstance().getCheckManager().getCheck(key), log.getViolations().get(key)));
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(toView);
            ChestMenu menu = new ChestMenu(oPlayer.getName() + "'s Logs", Math.max(Math.min(6, (int) Math.ceil(violations.size() / 9D)), 1));

            violations.keySet().stream().sorted(Comparator.comparingInt(violations::get).reversed()).forEachOrdered(check -> {
                menu.addItem(buildButton(log, check, violations.get(check)));
            });

            menu.showMenu(player);
        }
    }

    public void getRecentlyFlagged(Player player) {
        ChestMenu menu = new ChestMenu("Recently Flagged", 6);

        for (int i = 0; i < Math.min(45, Kauri.getInstance().getLoggerManager().getRecentlyFlagged().size()); i++) {
            UUID uuid = Kauri.getInstance().getLoggerManager().getRecentlyFlagged().get(i);
            menu.addItem(buildButton(Kauri.getInstance().getLoggerManager().getLogs().get(uuid), uuid));
        }

        menu.showMenu(player);
    }

    public void viewLoggedAlerts(CommandSender sender, UUID toView, String check, int page) {
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        if(!Kauri.getInstance().getLoggerManager().getLogs().containsKey(toView)) {
            sender.sendMessage(Color.Red + "No logs found for this player.");
        } else {
            Log log = Kauri.getInstance().getLoggerManager().getLogs().get(toView);

            List<String> alerts = log.getAlertLog().getOrDefault(check, new ArrayList<>());

            if(alerts.size() > 0) {
                int pageMin = Math.min(alerts.size() - 1, (page - 1) * 10), pageMax = Math.min(alerts.size() - 1, page * 10);

                for (int i = pageMin; i < pageMax; i++) {
                    sender.sendMessage(Color.translate("&7- &f" + alerts.get(i)));
                }
            } else {
                sender.sendMessage(Color.Red + "This player never set off any alerts for this check.");
            }
        }
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    public void viewLoggedAlerts(CommandSender sender, UUID toView, int page) {
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        if(!Kauri.getInstance().getLoggerManager().getLogs().containsKey(toView)) {
            sender.sendMessage(Color.Red + "No logs found for this player.");
        } else {
            Log log = Kauri.getInstance().getLoggerManager().getLogs().get(toView);

            List<String> alerts = new ArrayList<>();

            log.getAlertLog().keySet().forEach(key -> alerts.addAll(log.getAlertLog().get(key)));

            if(alerts.size() > 0) {
                int pageMin = Math.min(alerts.size() - 1, (page - 1) * 10), pageMax = Math.min(alerts.size() - 1, page * 10);

                for (int i = pageMin; i < pageMax; i++) {
                    sender.sendMessage(Color.translate("&7- &f" + alerts.get(i)));
                }
            } else {
                sender.sendMessage(Color.Red + "This player never set off any alerts for this check.");
            }
        }
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    private Button buildButton(Log log, Check check, int vl) {
        ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY);
        boolean banned = log.isBanned() && log.getBannedCheck().equals(check.getName());


        builder.durability(banned ? 14 : 4);

        builder.name(Color.Gold + check.getName());
        builder.lore("", "&bVL: &f" + vl, "&bBanned: &f" + banned);

        return new Button(false, builder.build());
    }

    private Button buildButton(Log log, UUID uuid) {
        ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        SkullMeta meta = (SkullMeta) stack.getItemMeta();

        val player = Bukkit.getOfflinePlayer(uuid);
        meta.setOwner(player.getName());

        stack.setItemMeta(meta);


        ItemBuilder builder = new ItemBuilder(stack);

        builder.name(Color.Green + player.getName());

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add(Color.translate("&7Flagged:"));
        log.getViolations().keySet().stream().filter(key -> log.getViolations().get(key) > 0).sorted(Comparator.comparingInt(key -> log.getViolations().get(key)).reversed()).forEach(key -> {
            lore.add(Color.translate("&6" + key + "&8: &c" + log.getViolations().get(key)));
        });

        builder.lore(lore);

        return new Button(false, builder.build());
    }
}
