package dev.brighten.anticheat.commands;

import cc.funkemunky.api.utils.*;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
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
import dev.brighten.anticheat.utils.mojang.MojangAPI;
import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CheckType;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Init(priority = Priority.LOW)
@CommandAlias("kauri|anticheat")
@CommandPermission("kauri.command")
public class MenuCommand extends BaseCommand {

    private static ChestMenu main, categoryMenu;
    private static long lastReset;

    public MenuCommand() {
        main = getMainMenu();
        categoryMenu = getChecksCategoryMenu();
    }

    private static ChestMenu getMain() {
        if (System.currentTimeMillis() - lastReset > 10000L) {
            lastReset = System.currentTimeMillis();
            return main = getMainMenu();
        }

        return main;
    }

    private static Button createButton(Material material, int amount, String name, ClickAction action, String... lore) {
        return new Button(false, MiscUtils.createItem(material, amount, name, lore), action);
    }

    @Subcommand("menu")
    @Description("Open the Kauri menu.")
    @CommandPermission("kauri.command.menu")
    public void onCommand(Player player) {
        getMain().showMenu(player);
        categoryMenu = getChecksCategoryMenu();
        player.sendMessage(Color.Green + "Opened main menu.");
    }

    private static ChestMenu getMainMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Kauri Menu", 3);

        menu.setItem(11, createButton(XMaterial.ANVIL.parseMaterial(), 1, "&cEdit Checks",
                (player, info) -> categoryMenu.showMenu(player),
                "", "&7Toggle Kauri checks on or off."));

        menu.setItem(13, createButton(XMaterial.ENCHANTED_BOOK.parseMaterial(), 1, "&cKauri Anticheat",
                (player, info) -> {
                    switch (info.getClickType()) {
                        case RIGHT:
                        case SHIFT_RIGHT: {
                            menu.setParent(null);
                            menu.close(player);
                            player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                            player.sendMessage(Color.translate("&6Discord: &fhttps://discord.me/Brighten"));
                            player.sendMessage(Color.translate("&6Website: &fhttps://funkemunky.cc/contact"));
                            player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                            break;
                        }
                        case LEFT:
                        case SHIFT_LEFT: {
                            menu.setParent(null);
                            menu.close(player);
                            break;
                        }
                    }
                },
                getKauriLore()));
        menu.setItem(15, createButton(XMaterial.PAPER.parseMaterial(), 1, "&cView Recent Violators",
                (player, info) -> {
                    player.sendMessage(Color.Gray + "Loading menu...");
                    getRecentViolatorsMenu(true).showMenu(player);
                }, "", "&7View players who flagged checks recently."));

        menu.fill(new FillerButton());
        return menu;
    }

    private static String[] getKauriLore() {
        return new String[]{"", "&7You are using &6Kauri Anticheat v" +
                KauriAPI.INSTANCE.getVersion(),
                "", "&7Your Plan: &eFree",
                "&e&oRight Click &7&oclick to get support."};
    }

    private static ChestMenu getChecksCategoryMenu() {
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
                            .filter(ci -> ci.type.equals(type))
                            .forEach(test -> {
                                amount.incrementAndGet();
                                if (test.enabled) enabled.incrementAndGet();
                                if (test.executable) executable.incrementAndGet();
                                if (test.cancellable) cancellable.incrementAndGet();
                                if (test.cancelMode != null) totalCancellable.incrementAndGet();
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

    private static ChestMenu getChecksMenu(CheckType type) {
        ChestMenu menu = new ChestMenu(Color.Gold + "Checks", 6);

        List<CheckSettings> values = Check.checkSettings.values()
                .stream()
                .filter(settings -> settings.type.equals(type))
                .sorted(Comparator.comparing(val -> val.name))
                .collect(Collectors.toList());

        menu.setParent(getChecksCategoryMenu());

        for (CheckSettings val : values) {
            ItemBuilder checkMapBuilder = new ItemBuilder(val.enabled
                    ? (val.cancellable ? XMaterial.FILLED_MAP.parseMaterial()
                    : XMaterial.MAP.parseMaterial()) : XMaterial.PAPER.parseMaterial());

            if (val.executable) {
                checkMapBuilder = checkMapBuilder.enchantment(Enchantment.DURABILITY, 1);
            }

            String enabled = "checks." + val.name + ".enabled";
            String executable = "checks." + val.name + ".executable";
            String cancellable = "checks." + val.name + ".cancellable";

            ItemStack item = checkMapBuilder.name((val.enabled ? "&a" : "&c") + val.name).lore("",
                    "&eStatus:",
                    (val.enabled ? Color.Green : Color.Gray) + "Enabled",
                    (val.executable ? Color.Green : Color.Gray) + "Executable",
                    (val.cancelMode != null
                            ? (val.cancellable ? Color.Green : Color.Gray) + "Cancellable"
                            : Color.Red + Color.Italics + "Cannot Cancel"), "",
                    "&f&oShift + Left Click &7&oto toggle detection",
                    "&f&oShift + Right Click &7&oto toggle executable",
                    "&f&oMiddle Click &7&oto toggle cancelling").build();

            Button button = new Button(false,
                    item, (player, info) -> {
                Kauri.INSTANCE.executor.execute(() -> {
                    switch(info.getClickType()) {
                        //Toggle the detectio on/off.
                        case SHIFT_LEFT: {
                            val.enabled = !val.enabled;

                            Kauri.INSTANCE.getConfig().set(enabled, val.enabled);
                            Kauri.INSTANCE.saveConfig();
                            info.getButton().setStack(new ItemBuilder(val.enabled
                                    ? (val.cancellable ? XMaterial.FILLED_MAP.parseMaterial()
                                    : XMaterial.MAP.parseMaterial()) : XMaterial.PAPER.parseMaterial())
                                    .name((val.enabled ? "&a" : "&c") + val.name).lore("",
                                            "&eStatus:",
                                            (val.enabled ? Color.Green : Color.Gray) + "Enabled",
                                            (val.executable ? Color.Green : Color.Gray) + "Executable",
                                            (val.cancelMode != null
                                                    ? (val.cancellable ? Color.Green : Color.Gray) + "Cancellable"
                                                    : Color.Red + Color.Italics + "Cannot Cancel"), "",
                                            "&f&oShift + Left Click &7&oto toggle detection",
                                            "&f&oShift + Right Click &7&oto toggle executable",
                                            "&f&oMiddle Click &7&oto toggle cancelling").build());
                            menu.buildInventory(false);

                            synchronized (Kauri.INSTANCE.dataManager.dataMap) {
                                Kauri.INSTANCE.dataManager.dataMap.values()
                                        .forEach(data -> {
                                            Check check = data.checkManager.checks.get(val.name);

                                            if(check == null) {
                                                Kauri.INSTANCE.getLogger()
                                                        .warning("Check " + val.name + " is null for player "
                                                                + data.getPlayer());
                                            } else {
                                                check.enabled = val.enabled;
                                            }
                                        });
                            }
                            break;
                        }
                        case SHIFT_RIGHT: {
                            val.executable = !val.executable;

                            Kauri.INSTANCE.getConfig().set(executable, val.executable);
                            Kauri.INSTANCE.saveConfig();

                            info.getButton().setStack(new ItemBuilder(val.enabled
                                    ? (val.cancellable ? XMaterial.FILLED_MAP.parseMaterial()
                                    : XMaterial.MAP.parseMaterial()) : XMaterial.PAPER.parseMaterial())
                                    .name((val.enabled ? "&a" : "&c") + val.name).lore("",
                                            "&eStatus:",
                                            (val.enabled ? Color.Green : Color.Gray) + "Enabled",
                                            (val.executable ? Color.Green : Color.Gray) + "Executable",
                                            (val.cancelMode != null
                                                    ? (val.cancellable ? Color.Green : Color.Gray) + "Cancellable"
                                                    : Color.Red + Color.Italics + "Cannot Cancel"), "",
                                            "&f&oShift + Left Click &7&oto toggle detection",
                                            "&f&oShift + Right Click &7&oto toggle executable",
                                            "&f&oMiddle Click &7&oto toggle cancelling").build());
                            menu.buildInventory(false);

                            synchronized (Kauri.INSTANCE.dataManager.dataMap) {
                                Kauri.INSTANCE.dataManager.dataMap.values()
                                        .forEach(data -> {
                                            Check check = data.checkManager.checks.get(val.name);

                                            if(check == null) {
                                                Kauri.INSTANCE.getLogger()
                                                        .warning("Check " + val.name + " is null for player "
                                                                + data.getPlayer());
                                            } else {
                                                check.executable = val.executable;
                                            }
                                        });
                            }

                            break;
                        }
                        case MIDDLE: {
                            val.cancellable = !val.cancellable;

                            Kauri.INSTANCE.getConfig().set(cancellable, val.cancellable);
                            Kauri.INSTANCE.saveConfig();

                            info.getButton().setStack(new ItemBuilder(val.enabled
                                    ? (val.cancellable ? XMaterial.FILLED_MAP.parseMaterial()
                                    : XMaterial.MAP.parseMaterial()) : XMaterial.PAPER.parseMaterial())
                                    .name((val.enabled ? "&a" : "&c") + val.name).lore("",
                                            "&eStatus:",
                                            (val.enabled ? Color.Green : Color.Gray) + "Enabled",
                                            (val.executable ? Color.Green : Color.Gray) + "Executable",
                                            (val.cancelMode != null
                                                    ? (val.cancellable ? Color.Green : Color.Gray) + "Cancellable"
                                                    : Color.Red + Color.Italics + "Cannot Cancel"), "",
                                            "&f&oShift + Left Click &7&oto toggle detection",
                                            "&f&oShift + Right Click &7&oto toggle executable",
                                            "&f&oMiddle Click &7&oto toggle cancelling").build());
                            menu.buildInventory(false);

                            synchronized (Kauri.INSTANCE.dataManager.dataMap) {
                                Kauri.INSTANCE.dataManager.dataMap.values()
                                        .forEach(data -> {
                                            Check check = data.checkManager.checks.get(val.name);

                                            if(check == null) {
                                                Kauri.INSTANCE.getLogger()
                                                        .warning("Check " + val.name + " is null for player "
                                                                + data.getPlayer());
                                            } else {
                                                check.cancellable = val.cancellable;
                                            }
                                        });
                            }

                            break;
                        }
                        default: {
                            ChestMenu toOpen = getCheckEdit(val);

                            toOpen.showMenu(player);
                            break;
                        }
                    }
                });
            });
            menu.addItem(button);
        }

        menu.fill(new FillerButton());
        return menu;
    }

    public static ChestMenu getCheckEdit(CheckSettings settings) {
        ChestMenu menu = new ChestMenu(Color.Yellow + settings.name, 3);

        menu.fill(new FillerButton());
        //Setting up middle book item
        String title = Color.Yellow + "Check State: " + Color.White + settings.devStage;
        List<String> description = Arrays.asList(MiscUtils
                .splitIntoLine(Color.translate(settings.description), 35));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(description);

        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, Color.translate(lore.get(i)));
        }

        ItemStack descItem = new ItemBuilder(XMaterial.BOOK.parseMaterial()).amount(1)
                .name(title).lore(lore).build();

        Button button = new Button(false, descItem);

        menu.setItem(13, button);

        String enabled = "checks." + settings.name + ".enabled";
        String executable = "checks." + settings.name + ".executable";
        String cancellable = "checks." + settings.name + ".cancellable";

        menu.setParent(getChecksMenu(settings.type));

        Button buttonEnabled = new Button(false,
                new ItemBuilder(settings.enabled
                        ? XMaterial.LIME_DYE.parseMaterial()
                        : XMaterial.RED_DYE.parseMaterial()).amount(1)
                        .durability(settings.enabled ? 10 : 8)
                        .name((settings.enabled ? Color.Green : Color.Gray) + "Enabled").build(),
                (player, info) -> {
                    settings.enabled = !settings.enabled;
                    Kauri.INSTANCE.getConfig().set(enabled, settings.enabled);
                    Kauri.INSTANCE.saveConfig();

                    info.getButton().setStack(new ItemBuilder(settings.enabled
                            ? XMaterial.LIME_DYE.parseMaterial()
                            : XMaterial.RED_DYE.parseMaterial()).amount(1)
                            .durability(settings.enabled ? 10 : 8)
                            .name((settings.enabled ? Color.Green : Color.Gray) + "Enabled").build());
                    menu.buildInventory(false);
                    Kauri.INSTANCE.executor.execute(() -> Kauri.INSTANCE.dataManager.dataMap.values()
                            .forEach(data -> {
                                Check check = data.checkManager.checks.get(settings.name);

                                if(check == null) {
                                    Kauri.INSTANCE.getLogger()
                                            .warning("Check " + settings.name + " is null for player "
                                                    + data.getPlayer());
                                } else {
                                    check.enabled = settings.enabled;
                                }
                            }));
                    menu.setParent(getChecksMenu(settings.type));
                });

        Button buttonExecutable = new Button(false,
                new ItemBuilder(settings.executable
                        ? XMaterial.LIME_DYE.parseMaterial()
                        : XMaterial.RED_DYE.parseMaterial()).amount(1)
                        .durability(settings.executable ? 10 : 8)
                        .name((settings.executable ? Color.Green : Color.Gray) + "Executable").build(),
                (player, info) -> {
                    settings.executable = !settings.executable;
                    Kauri.INSTANCE.getConfig().set(executable, settings.executable);
                    Kauri.INSTANCE.saveConfig();

                    info.getButton().setStack(new ItemBuilder(settings.executable
                            ? XMaterial.LIME_DYE.parseMaterial()
                            : XMaterial.RED_DYE.parseMaterial()).amount(1)
                            .durability(settings.executable ? 10 : 8)
                            .name((settings.executable ? Color.Green : Color.Gray) + "Executable").build());
                    menu.buildInventory(false);
                    Kauri.INSTANCE.executor.execute(() -> Kauri.INSTANCE.dataManager.dataMap.values()
                            .forEach(data -> {
                                Check check = data.checkManager.checks.get(settings.name);

                                if(check == null) {
                                    Kauri.INSTANCE.getLogger()
                                            .warning("Check " + settings.name + " is null for player "
                                                    + data.getPlayer());
                                } else {
                                    check.executable = settings.executable;
                                }
                            }));
                    menu.setParent(getChecksMenu(settings.type));
                });

        Button buttonCancellable = new Button(false,
                settings.cancelMode != null ? new ItemBuilder(settings.cancellable
                        ? XMaterial.LIME_DYE.parseMaterial()
                        : XMaterial.RED_DYE.parseMaterial()).amount(1)
                        .durability(settings.cancellable ? 10 : 8)
                        .name((settings.cancellable ? Color.Green : Color.Gray) + "Cancellable").build()
                        : new ItemBuilder(XMaterial.REDSTONE.parseMaterial())
                        .name(Color.Red + "Cancellable Not Allowed").build(),
                (player, info) -> {
                    if (settings.cancelMode == null) return;
                    settings.cancellable = !settings.cancellable;
                    Kauri.INSTANCE.getConfig().set(cancellable, settings.cancellable);
                    Kauri.INSTANCE.saveConfig();

                    info.getButton().setStack(new ItemBuilder(XMaterial.INK_SAC.parseMaterial()).amount(1)
                            .durability(settings.cancellable ? 10 : 8)
                            .name((settings.cancellable ? Color.Green : Color.Gray) + "Cancellable").build());
                    menu.buildInventory(false);
                    Kauri.INSTANCE.executor.execute(() -> Kauri.INSTANCE.dataManager.dataMap.values()
                            .forEach(data -> {
                                Check check = data.checkManager.checks.get(settings.name);

                                if(check == null) {
                                    Kauri.INSTANCE.getLogger()
                                            .warning("Check " + settings.name + " is null for player "
                                                    + data.getPlayer());
                                } else {
                                    check.cancellable = settings.cancellable;
                                }
                            }));
                    menu.setParent(getChecksMenu(settings.type));
                });

        //21, 22, 23
        menu.setItem(21, buttonEnabled);
        menu.setItem(22, buttonExecutable);
        menu.setItem(23, buttonCancellable);

        return menu;
    }

    public static ChestMenu getRecentViolatorsMenu(boolean fromMain) {
        ChestMenu menu = new ChestMenu(Color.Gold + "Recent Violators", 6);
        if (fromMain)
            menu.setParent(main);
        try {
            Map<UUID, List<Log>> logs = Kauri.INSTANCE.loggerManager
                    .getLogsWithinTimeFrame(TimeUnit.HOURS.toMillis(2));

            List<UUID> sortedIds = logs.keySet().stream()
                    .sorted(Comparator.comparing(key -> {
                        val logsList = logs.get(key);
                        return logsList.get(logsList.size() - 1).timeStamp;
                    }))
                    .collect(Collectors.toList());

            for (int i = 0; i < Math.min(45, sortedIds.size()); i++) {
                UUID uuid = sortedIds.get(i);
                String name = Optional.ofNullable(Bukkit.getOfflinePlayer(uuid)).map(OfflinePlayer::getName)
                        .orElse(MojangAPI.getUsername(uuid));
                if (name == null) name = "null";
                Log vl = logs.get(uuid).get(0);

                ItemBuilder builder = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());

                builder.amount(1);
                builder.durability(3);
                builder.owner(name);
                builder.name(Color.Green + name);
                builder.lore("", "&eCheck&7: &f" + vl.checkName, "&eVL&7: &f" + vl.vl, "&ePing&7: &f" + vl.ping,
                        "&eTPS&7: &f" + MathUtils.round(vl.tps, 2), "",
                        "&f&oShift-Left Click &7&oto view logs.");
                String finalName = name;
                menu.addItem(new Button(false, builder.build(),
                        (target, info) -> {
                            if (info.getClickType().equals(ClickType.SHIFT_LEFT)
                                    && target.hasPermission("kauri.command.logs")) {
                                LogsGUI gui = new LogsGUI(finalName, uuid);
                                menu.setParent(null);
                                menu.close(target);
                                gui.setParent(info.getMenu());
                                menu.setParent(main);
                                gui.showMenu(target);
                            }
                        }));
            }

            menu.fill(new FillerButton());

            return menu;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return menu;
    }

    private static String getLogsFromUUID(UUID uuid) {
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid);

        if (logs.size() == 0) return "No Logs";

        StringBuilder body = new StringBuilder();

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        String name = MojangAPI.getUsername(uuid);

        if (name == null) name = "null";
        for (Log log : logs) {
            body.append("(").append(format.format(new Date(log.timeStamp))).append("): ").append(name)
                    .append(" failed ").append(log.checkName).append(" at VL ").append(log.vl)
                    .append(" (tps=").append(MathUtils.round(log.tps, 4)).append(" ping=").append(log.ping)
                    .append(")").append("\n");
        }

        try {
            return Pastebin.makePaste(body.toString(), name + "'s Log", Pastebin.Privacy.UNLISTED);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }

}
