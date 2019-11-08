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

    private void setButtons(int page) {
        List<Log> subList = logs.subList(Math.min((page - 1) * 45, logs.size()), Math.min(page * 45, logs.size()));

        subList.forEach(log -> addItem(buttonFromLog(log)));

        //Setting the next page option if possible.
        if(Math.min(page * 45, logs.size()) < logs.size()) {
            Button next = new Button(false, new ItemBuilder(Material.BOOK)
                    .amount(1).name(Color.Red + "Next Page &7(&e" + (page + 1) + "&7)").build(),
                    (player, info) -> {
                        if(info.getClickType().isLeftClick()) {
                            setButtons(currentPage.incrementAndGet());
                            buildInventory(false);
                        }
                    });
            setItem(50, next);
        }

        Button getPastebin = new Button(false, new ItemBuilder(Material.COMPASS)
                .amount(1).name(Color.Red + "Share Logs")
                .lore("", "&7&oThis will return an &f&ounlisted &7&opastebin link.").build(),
                (player, info) -> {
                    if (info.getClickType().isLeftClick()) {
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
                            setButtons(currentPage.decrementAndGet());
                            buildInventory(false);
                        }
                    });
            setItem(50, back);
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
        RunUtils.taskTimerAsync(() -> {
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
                .amount(1).name(Color.Gold + MiscUtils.timeStampToDate(log.timeStamp))
                .lore("", "&eCheck&8: &f" + log.checkName,
                        "&eData&8: &f" + log.info,
                        "&eViolation Level&8: &f" + MathUtils.round(log.vl, 3),
                        "&ePing&8: &f" + log.ping, "&e&8: &f" + MathUtils.round(log.tps, 2))
                .build(), null);
    }
}
