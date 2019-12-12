package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.button.ClickAction;
import dev.brighten.anticheat.utils.menu.preset.button.FillerButton;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import dev.brighten.api.check.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;

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

    private static String[] splitIntoLine(String input, int maxCharInLine) {

        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            while (word.length() > maxCharInLine) {
                output.append(word.substring(0, maxCharInLine - lineLen) + "\n");
                word = word.substring(maxCharInLine - lineLen);
                lineLen = 0;
            }

            if (lineLen + word.length() > maxCharInLine) {
                output.append("\n");
                lineLen = 0;
            }
            output.append("&f" + word + " ");

            lineLen += word.length() + 1;
        }
        // output.split();
        // return output.toString();
        return output.toString().split("\n");
    }

    @Command(name = "kauri.menu", description = "Open the Kauri menu.", display = "menu", usage = "/<command>",
            aliases = {"kauri.gui"}, playerOnly = true, permission = "kauri.menu")
    public void onCommand(CommandAdapter cmd) {
        main.showMenu(cmd.getPlayer());
        cmd.getPlayer().sendMessage(Color.Green + "Opened main menu.");
    }

    private ChestMenu getMainMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Kauri Menu", 3);

        menu.setItem(11, createButton(Material.ANVIL, 1, "&cEdit Checks",
                (player, info) -> categoryMenu.showMenu(player),
                "", "&7Toggle Kauri checks on or off."));
        menu.setItem(13, createButton(Material.ENCHANTED_BOOK, 1, "&cKauri Anticheat",
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
        menu.setItem(15, createButton(Material.PAPER, 1, "&cView Recent Violators",
                (player, info) -> {
            getRecentViolatorsMenu().showMenu(player);
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
                    Button button = new Button(false, new ItemBuilder(Material.BOOK)
                            .amount(1).name("&e" + type.name()).build(),
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

            List<String> lore = new ArrayList<>(Arrays.asList("&7",
                    "&eEnabled&7: &f" + val.enabled,
                    "&eExecutable&7: &f" + val.executable,
                    "&eDescription&7: &f"));

            lore.addAll(Arrays.asList(splitIntoLine(val.description, 35)));

            Button button = createButton(
                    val.enabled ? (val.executable ? Material.MAP : Material.EMPTY_MAP) : Material.PAPER,
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

                                if (!settings.enabled) {
                                    info.getButton().getStack().setType(Material.PAPER);
                                } else {
                                    info.getButton().getStack().setType(settings.executable ? Material.MAP : Material.EMPTY_MAP);
                                }

                                ItemMeta meta = info.getButton().getStack().getItemMeta();
                                List<String> loreList = meta.getLore();
                                loreList.set(1, Color.translate("&eEnabled&7: &f" + settings.enabled));
                                meta.setDisplayName(Color.translate((settings.enabled ? "&a" : "&c") + val.name));
                                meta.setLore(loreList);
                                info.getButton().getStack().setItemMeta(meta);
                                menu.buildInventory(false);
                                Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                                        .forEach(data -> data.checkManager.checks.get(val.name).enabled = settings.enabled);
                                break;
                            }
                            case RIGHT:
                            case SHIFT_RIGHT: {
                                CheckSettings settings = Check.getCheckSettings(val.name);
                                settings.executable = !settings.executable;
                                Kauri.INSTANCE.getConfig().set(executable, settings.executable);
                                Kauri.INSTANCE.saveConfig();

                                if (settings.enabled) {
                                    info.getButton().getStack().setType(settings.executable ? Material.MAP : Material.EMPTY_MAP);
                                }

                                ItemMeta meta = info.getButton().getStack().getItemMeta();
                                List<String> loreList = meta.getLore();
                                loreList.set(2, Color.translate("&eExecutable&7: &f" + settings.executable));
                                meta.setLore(loreList);
                                info.getButton().getStack().setItemMeta(meta);
                                menu.buildInventory(false);
                                Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                                        .forEach(data -> data.checkManager.checks.get(settings.name).executable = settings.executable);
                                break;
                            }
                        }
                    }, lore.toArray(new String[]{}));

            menu.addItem(button);
        }
        return menu;
    }

    private ChestMenu getRecentViolatorsMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Recent Violators", 6);

        menu.setParent(main);

        Map<UUID, List<Log>> logs = Kauri.INSTANCE.loggerManager.getLogsWithinTimeFrame(TimeUnit.DAYS.toMillis(1));

        List<UUID> sortedIds = logs.keySet().stream().sorted(Comparator.comparing(key -> logs.get(key).get(0).timeStamp)).collect(Collectors.toList());

        for (int i = 0; i < Math.min(45, sortedIds.size()); i++) {
            UUID uuid = sortedIds.get(i);
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Log vl = logs.get(uuid).get(0);

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);

            builder.amount(1);
            builder.durability(3);
            builder.owner(player.getName());
            builder.name(Color.Green + player.getName());
            builder.lore("", "&eCheck&7: &f" + vl.checkName, "&eVL&7: &f" + vl.vl, "&ePing&7: &f" + vl.ping,
                    "&eTPS&7: &f" + MathUtils.round(vl.tps, 2), "",
                    "&f&oShift-Left Click &7&oto view logs.");
            menu.addItem(new Button(false, builder.build(),
                    (target, info) -> {
                if(info.getClickType().equals(ClickType.SHIFT_LEFT)) {
                    menu.setParent(null);
                    menu.close(target);
                    Bukkit.dispatchCommand(target, "kauri logs " + player.getName());
                }
            }));
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
