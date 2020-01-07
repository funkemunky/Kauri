package dev.brighten.anticheat.menu;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.commands.LogCommand;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.button.ClickAction;
import dev.brighten.anticheat.utils.menu.preset.button.FillerButton;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LogsGUI extends ChestMenu {

    private List<Log> logs = new ArrayList<>();
    private BukkitTask updaterTask;
    private AtomicInteger currentPage = new AtomicInteger(1);
    private OfflinePlayer player;
    private Player shown;
    private Set<String> filtered = new HashSet<>();

    public LogsGUI(OfflinePlayer player) {
        super("Violations", 6);

        this.player = player;
        updateLogs();

        setTitle(Color.Gray + player.getName() + "'s Logs");

        setButtons(1);
        buildInventory(true);
    }

    public LogsGUI(OfflinePlayer player, int page) {
        super(player.getName() + "'s Logs", 6);

        this.player = player;
        currentPage.set(page);
        updateLogs();
        setButtons(page);

        setTitle(Color.Gray + player.getName() + "'s Logs");

        buildInventory(true);
    }

    private void setButtons(int page) {
        if (getMenuDimension().getSize() <= 0) return;

        if (updaterTask == null || !Bukkit.getScheduler().isCurrentlyRunning(updaterTask.getTaskId())) {
            runUpdater();
        }

        List<Log> filteredLogs = (filtered.size() > 0 ? logs.stream()
                .filter(log -> {
                    for (String s : filtered) {
                        if (s.equalsIgnoreCase(log.checkName)) {
                            return true;
                        }
                    }
                    return false;
                }).sequential()
                .sorted(Comparator.comparing(log -> log.timeStamp, Comparator.reverseOrder()))
                .collect(Collectors.toList()) : logs);

        List<Log> subList = filteredLogs.subList(Math.min((page - 1) * 45, filteredLogs.size()),
                Math.min(page * 45, filteredLogs.size()));
        for (int i = 0; i < subList.size(); i++) setItem(i, buttonFromLog(subList.get(i)));

        if(subList.size() < 45) {
            for(int i = subList.size() ; i < 45 ; i++) {
                setItem(i, new FillerButton());
            }
        }

        //Setting the next page option if possible.
        if (Math.min(page * 45, filteredLogs.size()) < filteredLogs.size()) {
            Button next = new Button(false, new ItemBuilder(Material.BOOK)
                    .amount(1).name(Color.Red + "Next Page &7(&e" + (page + 1) + "&7)").build(),
                    (player, info) -> {
                        if (info.getClickType().isLeftClick()) {
                            setButtons(page + 1);
                            buildInventory(false);
                            currentPage.set(page + 1);
                        }
                    });
            setItem(50, next);
        } else setItem(50, new FillerButton());

        val punishments = Kauri.INSTANCE.loggerManager.getPunishments(player.getUniqueId());

        Button getPastebin = new Button(false, new ItemBuilder(Material.SKULL_ITEM).amount(1)
                .durability(3)
                .owner(player.getName())
                .name(Color.Red + player.getName())
                .lore("", "&7Page: &f" + page, "", "&6Punishments&8: &f" + punishments.size(), "",
                        "&e&oLeft click &7&oto view a summary of logs.",
                        (shown == null || shown.hasPermission("kauri.logs.share")
                                ? "&e&oRight Click &7&oto get an &f&ounlisted &7&opastebin link of the logs."
                                : "&c&o(No Permission) &e&o&mRight Click &7&o&mto get an &f&o&munlisted &7&o&mpastebin link of the logs."),
                        (shown == null || shown.hasPermission("kauri.logs.clear")
                                ? "&e&oShift Left Click &7&oto &f&oclear &7&othe logs of " + player.getName()
                                : "&c&o(No Permission) &e&o&mShift Right Click &7&o&mto &f&o&mclear &7&o&mthe logs of " + player.getName())).build(),
                (player, info) -> {
                    if (player.hasPermission("kauri.logs.share")) {
                        if (info.getClickType().isRightClick()) {
                            runFunction(info, "kauri.logs.share", () -> {
                                close(player);
                                player.sendMessage(Color.Green + "Logs: "
                                        + LogCommand.getLogsFromUUID(LogsGUI.this.player.getUniqueId()));
                            });
                        } else if (info.getClickType().isLeftClick() && info.getClickType().isShiftClick()) {
                            runFunction(info, "kauri.logs.clear",
                                    () -> player.performCommand("kauri logs clear " + this.player.getName()));
                        } else if (info.getClickType().isLeftClick()) {
                            getSummary().showMenu(player);
                        }
                    }
                });

        setItem(49, getPastebin);

        //Setting the previous page option if possible.
        if (page > 1) {
            Button back = new Button(false, new ItemBuilder(Material.BOOK)
                    .amount(1).name(Color.Red + "Previous Page &7(&e" + (page - 1) + "&7)").build(),
                    (player, info) -> {
                        if (info.getClickType().isLeftClick()) {
                            setButtons(page - 1);
                            currentPage.set(page - 1);
                            buildInventory(false);
                        }
                    });
            setItem(48, back);
        } else setItem(48, new FillerButton());

        if(filtered.size() > 0) {
            List<String> lore = new ArrayList<>(Arrays.asList("", Color.translate("&eFilters:")));

            for (String s : filtered) {
                lore.add(Color.translate("&7- &f" + s));
            }
            Button stopFilter = new Button(false,
                    new ItemBuilder(Material.REDSTONE).amount(1)
                            .name(Color.Red + "Stop Filter").lore(lore).build(),
                    (player, info) -> {
                       filtered.clear();
                       setButtons(currentPage.get());
                       buildInventory(false);
                    });

            setItem(47, stopFilter);
            setItem(51, stopFilter);
        } else {
            setItem(47, new FillerButton());
            setItem(51, new FillerButton());
        }

        //Setting all empty slots with a filler.
        fill(new FillerButton());
    }

    private ChestMenu getSummary() {
        ChestMenu summary = new ChestMenu(player.getName() + "'s Summary", 6);

        summary.setParent(this);
        summary.fill(new FillerButton());
        Map<String, List<Log>> sortedLogs = new HashMap<>();

        logs.forEach(log -> {
            List<Log> list = sortedLogs.getOrDefault(log.checkName, new ArrayList<>());

            list.add(log);
            sortedLogs.put(log.checkName, list);
        });

        sortedLogs.forEach((check, list) -> {
            Button button = new Button(false,
                    new ItemBuilder(Material.PAPER).amount(1).name((filtered.contains(check) ? Color.Red + Color.Italics : Color.Gold)
                            + check)
                            .lore("", "&7Alerts: &f" + list.size(),
                                    "&7Highest VL: &f" +
                                            list.stream()
                                                    .max(Comparator.comparing(log -> log.vl))
                                                    .map(log -> log.vl).orElse(0f),
                                    "&7Type: &f" + Check.getCheckSettings(check).type.name(),
                                    "", "&a&oLeft click &7&oto view vls from this check.").build(),
                    (player, info) -> {
                        filtered.add(check);
                        setButtons(1);
                        currentPage.set(1);
                        buildInventory(false);
                        info.getMenu().close(player);
                        getSummary().showMenu(player);
                        info.getButton().getStack().setItemMeta(new ItemBuilder(info.getButton().getStack())
                                .name((filtered.contains(check) ? Color.Red + Color.Italics : Color.Gold) + check)
                                .build().getItemMeta());
                        List<String> lore = new ArrayList<>(Arrays.asList("", Color.translate("&eFilters:")));

                        for (String s : filtered) {
                            lore.add(Color.translate("&7- &f" + s));
                        }
                        Button stopFilter = new Button(false,
                                new ItemBuilder(Material.REDSTONE).amount(1)
                                        .name(Color.Red + "Stop Filter").lore(lore).build(),
                                (player2, info2) -> {
                                    filtered.clear();
                                    setButtons(1);
                                    buildInventory(false);
                                    info.getMenu().close(player2);
                                    getSummary().showMenu(player2);
                                });

                        info.getMenu().setItem(53, stopFilter);
                        info.getMenu().buildInventory(false);
                    });
            summary.addItem(button);
        });

        if(filtered.size() > 0) {
            List<String> lore = new ArrayList<>(Arrays.asList("", Color.translate("&eFilters:")));

            for (String s : filtered) {
                lore.add(Color.translate("&7- &f" + s));
            }
            Button stopFilter = new Button(false,
                    new ItemBuilder(Material.REDSTONE).amount(1)
                            .name(Color.Red + "Stop Filter").lore(lore).build(),
                    (player, info) -> {
                        filtered.clear();
                        setButtons(1);
                        buildInventory(false);
                        info.getMenu().close(player);
                        getSummary().showMenu(player);
                    });

            summary.setItem(53, stopFilter);
        } else {
            summary.setItem(53, new FillerButton());
        }

        return summary;
    }

    private void updateLogs() {
        logs = Kauri.INSTANCE.loggerManager.getLogs(player.getUniqueId())
                .stream()
                .sorted(Comparator.comparing(log -> log.timeStamp, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private void runUpdater() {
        updaterTask = RunUtils.taskTimerAsync(() -> {
            if (shown != null
                    && shown.getOpenInventory() != null
                    && shown.getOpenInventory().getTopInventory() != null
                    && shown.getOpenInventory().getTopInventory().getTitle().equals(getTitle())) {
                updateLogs();
                setButtons(currentPage.get());
                buildInventory(false);
            } else cancelTask();
        }, Kauri.INSTANCE, 80L, 40L);
    }

    private void cancelTask() {
        if (updaterTask == null) return;
        updaterTask.cancel();
    }

    @Override
    public void showMenu(Player player) {
        this.shown = player;
        runUpdater();

        super.showMenu(player);
    }

    private void runFunction(ClickAction.InformationPair info, String permission, Runnable function) {
        if (shown == null) return;

        if (shown.hasPermission(permission)) {
            function.run();
        } else {
            String oldName = info.getButton().getStack().getItemMeta().getDisplayName();
            List<String> oldLore = info.getButton().getStack().getItemMeta().getLore();
            ItemMeta meta = info.getButton().getStack().getItemMeta();

            meta.setDisplayName(Color.Red + "No permission");
            meta.setLore(new ArrayList<>());
            info.getButton().getStack().setItemMeta(meta);
            RunUtils.taskLater(() -> {
                if (info.getButton() != null
                        && info.getButton().getStack().getItemMeta()
                        .getDisplayName().equals(Color.Red + "No permission")) {
                    ItemMeta newMeta = info.getButton().getStack().getItemMeta();
                    newMeta.setDisplayName(oldName);
                    newMeta.setLore(oldLore);
                    info.getButton().getStack().setItemMeta(newMeta);
                }
            }, Kauri.INSTANCE, 20L);
        }
    }

    @Override
    public void handleClose(Player player) {
        cancelTask();
    }

    private Button buttonFromLog(Log log) {
        return new Button(false, new ItemBuilder(Material.PAPER)
                .amount(1).name(Color.Gold + log.checkName)
                .lore("", "&eTime&8: &f" + MiscUtils.timeStampToDate(log.timeStamp),
                        "&eData&8: &f" + log.info,
                        "&eViolation Level&8: &f" + MathUtils.round(log.vl, 3),
                        "&ePing&8: &f" + log.ping,
                        "&eTPS&8: &f" + MathUtils.round(log.tps, 2))
                .build(), null);
    }
}
