package dev.brighten.anticheat.menu;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.commands.LogCommand;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.preset.button.FillerButton;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LogsGUI extends ChestMenu {

    private List<Log> logs = new ArrayList<>();
    private BukkitTask updaterTask;
    private AtomicInteger currentPage = new AtomicInteger(1);
    private OfflinePlayer player;
    private Player shown;

    public LogsGUI(OfflinePlayer player) {
        super(player.getName() + "'s Violations", 6);

        this.player = player;
        updateLogs();
        setButtons(1);
        buildInventory(true);
    }

    public LogsGUI(OfflinePlayer player, int page) {
        super(player.getName() + "'s Violations", 6);

        this.player = player;
        currentPage.set(page);
        updateLogs();
        setButtons(page);
        buildInventory(true);
    }

    private void setButtons(int page) {
        List<Log> subList = logs.subList(Math.min((page - 1) * 45, logs.size()), Math.min(page * 45, logs.size()));

        subList.forEach(log -> addItem(buttonFromLog(log)));

        //Setting the next page option if possible.
        if(Math.min(page * 45, logs.size()) < logs.size()) {
            Button next = new Button(false, new ItemBuilder(Material.BOOK)
                    .amount(1).name(Color.Red + "Next Page &7(&e" + (page + 1) + "&7)").build(),
                    (player, info) -> {
                        if(info.getClickType().isLeftClick()) {
                            close(player);
                            new LogsGUI(LogsGUI.this.player, page + 1).showMenu(player);
                        }
                    });
            setItem(50, next);
        }

        val punishments = Kauri.INSTANCE.loggerManager.getPunishments(player.getUniqueId());

        Button getPastebin = new Button(false, new ItemBuilder(Material.SKULL_ITEM).owner(player.getName())
                .amount(1).name(Color.Red + "funkemunky")
                .lore("", "&6Punishments&8: &f" + punishments.size(), "",
                        "&e&oRight Click &7&oto get an &f&ounlisted &7&opastebin link of the logs.").build(),
                (player, info) -> {
                    if (player.hasPermission("kauri.logs.pastebin") && info.getClickType().isRightClick()) {
                        close(player);
                        player.sendMessage(Color.Green + "Logs: "
                                + LogCommand.getLogsFromUUID(LogsGUI.this.player.getUniqueId()));
                    }
                });

        setItem(49, getPastebin);

        //Setting the previous page option if possible.
        if(page > 1) {
            Button back = new Button(false, new ItemBuilder(Material.BOOK)
                    .amount(1).name(Color.Red + "Previous Page &7(&e" + (page - 1) + "&7)").build(),
                    (player, info) -> {
                        if(info.getClickType().isLeftClick()) {
                            close(player);
                            new LogsGUI(LogsGUI.this.player, page - 1).showMenu(player);
                        }
                    });
            setItem(48, back);
        }

        //Setting all empty slots with a filler.
        IntStream.range(0, 54).filter(index -> {
            val optional = getButtonByIndex(index);

            return !optional.isPresent() || optional.get().getStack().getType().equals(Material.AIR);
        }).forEach(index -> setItem(index, new FillerButton()));
    }

    private void updateLogs() {
        logs = Kauri.INSTANCE.loggerManager.getLogs(player.getUniqueId())
                .stream()
                .sorted(Comparator.comparing(log -> log.timeStamp, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private void runUpdater() {
        updaterTask = RunUtils.taskTimerAsync(() -> {
            if(shown != null
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
        updaterTask.cancel();
    }

    @Override
    public void showMenu(Player player) {
        this.shown = player;
        runUpdater();

        super.showMenu(player);
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
