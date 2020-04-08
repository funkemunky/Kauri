package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.menu.LogsGUI;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.button.ClickAction;
import dev.brighten.anticheat.utils.menu.preset.button.FillerButton;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Init(commands = true)
public class MenuCommand {

    private ChestMenu main, categoryMenu;
    public MenuCommand() {
        main = getMainMenu();
        categoryMenu = getChecksCategoryMenu();
    }

    private static Button createButton(Material material, int amount, String name, ClickAction action, String... lore) {
        return new Button(false, MiscUtils.createItem(material, amount, name, lore), action);
    }

    @Command(name = "kauri.menu", description = "Open the Kauri menu.", display = "menu", usage = "/<command>",
            aliases = {"kauri.gui"}, playerOnly = true, permission = "kauri.command.menu")
    public void onCommand(CommandAdapter cmd) {
        main.showMenu(cmd.getPlayer());
        categoryMenu = getChecksCategoryMenu();
        cmd.getPlayer().sendMessage(Color.Green + "Opened main menu.");
    }

    private ChestMenu getMainMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Kauri Menu", 3);

        menu.setItem(11, createButton(XMaterial.ANVIL.parseMaterial(), 1, "&cEdit Checks",
                (player, info) -> categoryMenu.showMenu(player),
                "", "&7Toggle Kauri checks on or off."));
        menu.setItem(13, createButton(XMaterial.ENCHANTED_BOOK.parseMaterial(), 1, "&cKauri Anticheat",
                (player, info) -> {
                    if (info.getClickType().equals(ClickType.RIGHT)
                            || info.getClickType().equals(ClickType.SHIFT_RIGHT)) {
                        menu.setParent(null);
                        menu.close(player);
                        player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                        player.sendMessage(Color.translate("&6Discord: &fhttps://discord.me/Brighten"));
                        player.sendMessage(Color.translate("&6Website: &fhttps://funkemunky.cc/contact"));
                        player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                    }
                },
                "", "&7You are using &6Kauri Anticheat v" +
                        Kauri.INSTANCE.getDescription().getVersion(), "&e&oRight Click &7&oclick to get support."));
        menu.setItem(15, createButton(XMaterial.PAPER.parseMaterial(), 1, "&cView Recent Violators",
                (player, info) -> {
            Kauri.INSTANCE.executor.execute(() -> {
                player.sendMessage(Color.Gray + "Loading menu...");
                getRecentViolatorsMenu().showMenu(player);
            });
        }, "", "&7View players who flagged checks recently."));
        return menu;
    }

    private ChestMenu getChecksCategoryMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Check Categories", 3);

        menu.setParent(main);

        AtomicInteger amt = new AtomicInteger(0);
        Arrays.stream(CheckType.values())
                .sorted(Comparator.comparing(Enum::name))
                .forEach(type -> {

                    AtomicInteger amount = new AtomicInteger(0),
                            enabled = new AtomicInteger(0),
                            executable = new AtomicInteger(0),
                            cancellable = new AtomicInteger(0),
                            totalCancellable = new AtomicInteger(0);

                    Check.checkSettings.values()
                            .stream()
                            .filter(ci-> ci.type.equals(type))
                            .forEach(test -> {
                                amount.incrementAndGet();
                                if(test.enabled) enabled.incrementAndGet();
                                if(test.executable) executable.incrementAndGet();
                                if(test.cancellable) cancellable.incrementAndGet();
                                if(test.cancelMode != null) totalCancellable.incrementAndGet();
                            });

                    Button button = new Button(false,
                            new ItemBuilder(XMaterial.BOOK.parseMaterial())
                                    .amount(Math.max(1, amount.get()))
                                    .name("&e" + type.name())
                                    .lore("", "&aEnabled&8: &f" + enabled + "&7/&f" + amount,
                                            "&aExecutable&8: &f" + executable + "&7/&f" + amount,
                                            "&aCancellable&8: &f" + cancellable + "&7/&f" + totalCancellable,
                                            "", "&7&oClick to configure in this category.")
                                    .build(),
                            (player, info) -> {
                        menu.setParent(null);
                        menu.close(player);
                        getChecksMenu(type).showMenu(player);
                        menu.setParent(main);
                            });
                    amt.incrementAndGet();
                    menu.addItem(button);
                });

        menu.fill(new FillerButton());

        return menu;
    }

    private ChestMenu getChecksMenu(CheckType type) {
        ChestMenu menu = new ChestMenu(Color.Gold + "Checks", 6);

        menu.setParent(categoryMenu);

        List<CheckSettings> values = Check.checkSettings.values()
                .stream()
                .filter(settings -> settings.type.equals(type))
                .sorted(Comparator.comparing(val -> val.name))
                .collect(Collectors.toList());

        for (int i = 0; i < values.size(); i++) {
            CheckSettings val = values.get(i);

            String enabled = "checks." + val.name + ".enabled";
            String executable = "checks." + val.name + ".executable";
            String cancellable = "checks." + val.name + ".cancellable";

            List<String> lore = new ArrayList<>(Arrays.asList("&7",
                    "&eEnabled&7: &f" + val.enabled,
                    "&eExecutable&7: &f" + val.executable,
                    "&eCancellable&7: &f" + val.cancellable,
                    "&eDescription&7: &f"));

            List<String> description = Arrays.asList(dev.brighten.anticheat.utils.MiscUtils
                    .splitIntoLine(val.description, 35));

            lore.addAll(description);


            Button button = createButton(
                    val.enabled ? (val.executable ? XMaterial.FILLED_MAP.parseMaterial()
                            : XMaterial.MAP.parseMaterial())
                            : XMaterial.PAPER.parseMaterial(),
                    1,
                    (val.enabled ? "&a" : "&c") + val.name,
                    (player, info) -> {
                        switch (info.getClickType()) {
                            case LEFT:
                            case SHIFT_LEFT: {
                                CheckSettings settings = Check.getCheckSettings(val.name);
                                settings.enabled = !settings.enabled;
                                Kauri.INSTANCE.getConfig().set(enabled, settings.enabled);
                                Kauri.INSTANCE.saveConfig();

                                ItemBuilder builder = new ItemBuilder(info.getButton().getStack());
                                if (!settings.enabled) {
                                    builder.type(XMaterial.PAPER.parseMaterial());
                                } else {
                                    builder.type(settings.executable ? XMaterial.FILLED_MAP.parseMaterial()
                                            : XMaterial.MAP.parseMaterial());
                                    if(settings.cancellable) {
                                        builder.enchantment(Enchantment.DURABILITY, 1);
                                    }
                                }

                                List<String> lore2 = new ArrayList<>(Arrays.asList("&7",
                                        "&eEnabled&7: &f" + val.enabled,
                                        "&eExecutable&7: &f" + val.executable,
                                        "&eCancellable&7: &f" + val.cancellable,
                                        "&eDescription&7: &f"));
                                lore2.addAll(description);
                                builder.lore(lore2.stream().map(Color::translate).toArray(String[]::new));
                                builder.name((settings.enabled ? "&a" : "&c") + val.name);
                                info.getButton().setStack(builder.build());
                                menu.buildInventory(false);
                                Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                                        .forEach(data -> data.checkManager.checks.get(val.name)
                                                .enabled = settings.enabled);
                                break;
                            }
                            case RIGHT:
                            case SHIFT_RIGHT: {
                                CheckSettings settings = Check.getCheckSettings(val.name);
                                settings.executable = !settings.executable;
                                Kauri.INSTANCE.getConfig().set(executable, settings.executable);
                                Kauri.INSTANCE.saveConfig();

                                ItemBuilder builder = new ItemBuilder(info.getButton().getStack());
                                if (settings.enabled) {
                                    builder.type(settings.executable ? XMaterial.FILLED_MAP.parseMaterial()
                                            : XMaterial.MAP.parseMaterial());
                                }

                                List<String> lore2 = new ArrayList<>(Arrays.asList("&7",
                                        "&eEnabled&7: &f" + val.enabled,
                                        "&eExecutable&7: &f" + val.executable,
                                        "&eCancellable&7: &f" + val.cancellable,
                                        "&eDescription&7: &f"));
                                lore2.addAll(description);
                                builder.lore(lore2.stream().map(Color::translate).toArray(String[]::new));
                                info.getButton().setStack(builder.build());
                                menu.buildInventory(false);
                                Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                                        .forEach(data -> data.checkManager.checks.get(settings.name)
                                                .executable = settings.executable);
                                break;
                            }
                            case MIDDLE: {
                                CheckSettings settings = Check.getCheckSettings(val.name);
                                settings.cancellable = !settings.cancellable;
                                Kauri.INSTANCE.getConfig().set(cancellable, settings.cancellable);
                                Kauri.INSTANCE.saveConfig();

                                ItemBuilder builder = new ItemBuilder(info.getButton().getStack());
                                if (settings.enabled) {
                                    builder.clearEnchantments();
                                    if(settings.cancellable) {
                                        builder.enchantment(Enchantment.DURABILITY, 1);
                                    }
                                }

                                List<String> lore2 = new ArrayList<>(Arrays.asList("&7",
                                        "&eEnabled&7: &f" + val.enabled,
                                        "&eExecutable&7: &f" + val.executable,
                                        "&eCancellable&7: &f" + val.cancellable,
                                        "&eDescription&7: &f"));
                                lore2.addAll(description);
                                builder.lore(lore2.stream().map(Color::translate).toArray(String[]::new));
                                info.getButton().setStack(builder.build());
                                menu.buildInventory(false);
                                Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                                        .forEach(data -> data.checkManager.checks.get(settings.name)
                                                .cancellable = settings.cancellable);
                                break;
                            }
                        }
                        ((ChestMenu)info.getMenu()).setParent(categoryMenu = getChecksCategoryMenu());
                    }, lore.toArray(new String[]{}));

            if (val.enabled) {
                ItemBuilder builder = new ItemBuilder(button.getStack());
                builder.clearEnchantments();
                if(val.cancellable) {
                    builder.enchantment(Enchantment.DURABILITY, 1);
                }
                button.setStack(builder.build());
            }
            menu.addItem(button);
        }
        return menu;
    }

    private ChestMenu getRecentViolatorsMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Recent Violators", 6);
        menu.setParent(main);
        try {
            Map<UUID, List<Log>> logs = Kauri.INSTANCE.loggerManager
                    .getLogsWithinTimeFrame(TimeUnit.HOURS.toMillis(2));

            List<UUID> sortedIds = logs.keySet().stream()
                    .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                    .sorted(Comparator.comparing(key -> {
                        val logsList =  logs.get(key);
                        return logsList.get(logsList.size() - 1).timeStamp;
                    }))
                    .collect(Collectors.toList());

            for (int i = 0; i < Math.min(45, sortedIds.size()); i++) {
                UUID uuid = sortedIds.get(i);
                Player player = Bukkit.getPlayer(uuid);
                Log vl = logs.get(uuid).get(0);

                ItemBuilder builder = new ItemBuilder(XMaterial.SKULL_ITEM.parseMaterial());

                builder.amount(1);
                builder.durability(3);
                builder.owner(player.getName());
                builder.name(Color.Green + player.getName());
                builder.lore("", "&eCheck&7: &f" + vl.checkName, "&eVL&7: &f" + vl.vl, "&ePing&7: &f" + vl.ping,
                        "&eTPS&7: &f" + MathUtils.round(vl.tps, 2), "",
                        "&f&oShift-Left Click &7&oto view logs.");
                menu.addItem(new Button(false, builder.build(),
                        (target, info) -> {
                            if(info.getClickType().equals(ClickType.SHIFT_LEFT)
                                    && target.hasPermission("kauri.command.logs")) {
                                LogsGUI gui = new LogsGUI(Bukkit.getOfflinePlayer(uuid));
                                menu.setParent(null);
                                menu.close(target);
                                gui.setParent(info.getMenu());
                                menu.setParent(main);
                                gui.showMenu(target);
                            }
                        }));
            }
            return menu;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return menu;
    }

    private static String getLogsFromUUID(UUID uuid) {
        Kauri.INSTANCE.profiler.start("cmd:logs");
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid);
        Kauri.INSTANCE.profiler.stop("cmd:logs");

        if(logs.size() == 0) return "No Logs";

        StringBuilder body = new StringBuilder();

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
        for (Log log : logs) {
            body.append("(").append(format.format(new Date(log.timeStamp))).append("): ").append(pl.getName())
                    .append(" failed ").append(log.checkName).append(" at VL ").append(log.vl)
                    .append(" (tps=").append(MathUtils.round(log.tps, 4)).append(" ping=").append(log.ping)
                    .append(")").append("\n");
        }

        try {
            return Pastebin.makePaste(body.toString(), pl.getName() + "'s Log", Pastebin.Privacy.UNLISTED);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }

}
