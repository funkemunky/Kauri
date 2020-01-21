package dev.brighten.anticheat.commands;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.utils.AtomicDouble;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.preset.button.FillerButton;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import lombok.val;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitTask;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Init(commands = true)
public class ProfilerCommand {

    private Map<String, BukkitTask> taskMap = new HashMap<>();

    @Command(name = "kauri.profile", display = "profile", description = "run a profile on Kauri.",
            permission = {"kauri.profile"})
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getSender() instanceof Player) {
            int page = 1;
            if(cmd.getArgs().length > 1) {
                try {
                    page = Integer.parseInt(cmd.getArgs()[1]);
                } catch(NumberFormatException e) {
                    cmd.getSender().sendMessage(Color.Red + "The page number provided must be an integer.");
                    return;
                }
            }

            final int finalPage = page;

            ChestMenu menu = new ChestMenu("Profile Page: " + page, 6);
            menu.buildInventory(true);

            BukkitTask task = RunUtils.taskTimerAsync(() -> {
               menu.fill(new FillerButton());
                final List<Tuple<Double, Button>> buttons = new ArrayList<>();

                AtomicDouble samples = new AtomicDouble(0);

                val map = Kauri.INSTANCE.profiler.results(ResultsType.TICK);

                long total = map.keySet()
                        .stream()
                        .mapToLong(key -> Math.round(map.get(key).two))
                        .sum();

                map.forEach((key, result) -> {
                    Button button = new Button(false, new ItemBuilder(Material.REDSTONE)
                            .amount(1)
                            .name(Color.Gold + key).lore("",
                                    "&7Weighted Usage: " + dev.brighten.anticheat.utils.MiscUtils
                                            .drawUsage(total, result.two),
                                    "&7MS: &f" + dev.brighten.anticheat.utils.MiscUtils
                                            .format(Kauri.INSTANCE.profiler.total.get(key) / 1000000D, 3),
                                    "&7Samples: &f" + dev.brighten.anticheat.utils.MiscUtils
                                            .format(result.two / 1000000D, 3),
                                    "&7Deviation: &f" + dev.brighten.anticheat.utils.MiscUtils
                                            .format(Kauri.INSTANCE.profiler.stddev
                                                    .getOrDefault(key, 0L), 3) / 1000000D).build());

                    buttons.add(new Tuple<>(result.two, button));
                    samples.addAndGet(result.two / 1000000D);
                });

                buttons.sort(Comparator.comparing(tuple -> tuple.one, Comparator.reverseOrder()));

                double totalMs = total / 1000000D;
                if(finalPage > 1) {
                    menu.setItem(48, new Button(false,
                            new ItemBuilder(Material.BOOK).amount(1).name("&cBack").build(),
                            (player, info) -> Bukkit.dispatchCommand(player,
                                    "kauri profile testGUI " + (finalPage - 1))));
                }
                menu.setItem(49, new Button(false,
                        new ItemBuilder(Material.REDSTONE)
                                .amount(1)
                                .name(Color.Gold + "Total").lore("",
                                "&7Usage: " + dev.brighten.anticheat.utils.MiscUtils.drawUsage(50,
                                        dev.brighten.anticheat.utils.MiscUtils.format(samples.get(), 3)),
                                "&7Total: &f" + dev.brighten.anticheat.utils.MiscUtils.format(totalMs, 3),
                                "&7Samples: &f" + dev.brighten.anticheat.utils.MiscUtils.format(samples.get(), 3),
                                "",
                                "&7&oRight click to reset data.")
                                .build(),
                        (player, info) -> {
                            if (info.getClickType().equals(ClickType.RIGHT)) {
                                Kauri.INSTANCE.profiler.reset();
                            }
                        }));
                menu.setItem(50, new Button(false,
                        new ItemBuilder(Material.BOOK).amount(1).name("&cNext").build(),
                        (player, info) -> Bukkit.dispatchCommand(player,
                                "kauri profile testGUI " + (finalPage + 1))));
                for (int i = (finalPage - 1) * 45; i < Math.min(finalPage * 45, buttons.size()); i++) {
                    menu.setItem(i, buttons.get(i).two);
                }

                menu.buildInventory(false);
            }, Kauri.INSTANCE, 0L, 4L);

            menu.setCloseHandler((player, menuConsumer) -> task.cancel());

            menu.showMenu(cmd.getPlayer());

        } else {
            cmd.getSender().sendMessage("-------------------------------------------------");
            Map<String, Long> sorted = dev.brighten.anticheat.utils.MiscUtils
                    .sortByValue(Kauri.INSTANCE.profiler.total);
            int size = sorted.size();
            AtomicLong total = new AtomicLong();
            List<Map.Entry<String, Long>> entries = new ArrayList<>(sorted.entrySet());
            IntStream.range(size - Math.min(size - 10, 10), size).mapToObj(entries::get).forEach(entry -> {
                String name = entry.getKey();
                Long time = entry.getValue();
                total.addAndGet(time);
                cmd.getSender().sendMessage(dev.brighten.anticheat.utils.MiscUtils.drawUsage(total.get(), time)
                        + " §c" + name
                        + "§7: " + dev.brighten.anticheat.utils.MiscUtils.format(time / 1000000D, 3)
                        + ", " + dev.brighten.anticheat.utils.MiscUtils
                        .format(Kauri.INSTANCE.profiler.samples
                                .getOrDefault(name, new Tuple<>(0L, 0L)).one / 1000000D, 3)
                        + ", " + dev.brighten.anticheat.utils.MiscUtils
                        .format(Kauri.INSTANCE.profiler.stddev.getOrDefault(name, 0L) / 1000000D, 3));
            });
            double totalMs = total.get() / 1000000D;
            long totalTime = Kauri.INSTANCE.profiler.totalCalls * 50;
            cmd.getSender().sendMessage(dev.brighten.anticheat.utils.MiscUtils
                    .drawUsage(total.get(), dev.brighten.anticheat.utils.MiscUtils.format(totalMs / totalTime, 3))
                    + " §cTotal§7: " + dev.brighten.anticheat.utils.MiscUtils.format(totalMs, 3)
                    + " §f- §c" + dev.brighten.anticheat.utils.MiscUtils.format(totalMs / totalTime, 3) + "%");
            cmd.getSender().sendMessage("-------------------------------------------------");
        }
    }

    @Command(name = "kauri.profile.chat", display = "profile chat", description = "send updated profiling in chat.",
            permission = "kauri.command.profile")
    public void onChatCmd(CommandAdapter cmd) {
        if(taskMap.containsKey(cmd.getSender().getName())) {
            taskMap.get(cmd.getSender().getName()).cancel();
            taskMap.remove(cmd.getSender().getName());
        } else {
            val task = RunUtils.taskTimerAsync(() -> {
                if(taskMap.containsKey(cmd.getSender().getName())) {
                    cmd.getSender().sendMessage("-------------------------------------------------");
                    Map<String, Long> sorted = dev.brighten.anticheat.utils.MiscUtils
                            .sortByValue(Kauri.INSTANCE.profiler.total);
                    val map = Kauri.INSTANCE.profiler.results(ResultsType.TICK);

                    long total = map.keySet()
                            .stream()
                            .mapToLong(key -> Math.round(map.get(key).two))
                            .sum();
                    List<Map.Entry<String, Long>> entries = new ArrayList<>(sorted.entrySet());
                    AtomicDouble samples = new AtomicDouble(0);
                    map.keySet().stream().forEach(key -> {
                        val entry = map.get(key);
                        String name = key;
                        double time = entry.two / 1000000D;
                        samples.addAndGet(time);
                        cmd.getSender().sendMessage(dev.brighten.anticheat.utils.MiscUtils.drawUsage(50,
                                time)
                                + " §c" + name
                                + "§7: " + dev.brighten.anticheat.utils.MiscUtils.format(time / 1000000D, 3)
                                + ", " + dev.brighten.anticheat.utils.MiscUtils
                                .format(Kauri.INSTANCE.profiler.samples
                                        .getOrDefault(name, new Tuple<>(0L, 0L)).one / 1000000D, 3)
                                + ", " + dev.brighten.anticheat.utils.MiscUtils
                                .format(Kauri.INSTANCE.profiler.stddev.getOrDefault(name, 0L) / 1000000D, 3));
                    });
                    double totalMs = total / 1000000D;
                    cmd.getSender().sendMessage(dev.brighten.anticheat.utils.MiscUtils
                            .drawUsage(50, dev.brighten.anticheat.utils.MiscUtils.format(totalMs, 3))
                            + " §cTotal§7: " + dev.brighten.anticheat.utils.MiscUtils.format(totalMs, 3)
                            + " §f- §c" + dev.brighten.anticheat.utils.MiscUtils.format(samples.get(), 3) + "%");
                    cmd.getSender().sendMessage("-------------------------------------------------");
                }
            }, Kauri.INSTANCE, 30L, 3L);
            taskMap.put(cmd.getSender().getName(), task);
        }
    }

    @Command(name = "kauri.profile.paste", display = "profile paste [type]",
            description = "make a detailed profile with pastebin.", permission = "kauri.command.profile.paste")
    public void onPaste(CommandAdapter cmd) {
        ResultsType type = cmd.getArgs().length > 0 ? Arrays.stream(ResultsType.values())
                .filter(rt -> rt.name().equalsIgnoreCase(cmd.getArgs()[0])).findFirst().orElse(ResultsType.TOTAL)
                : ResultsType.TOTAL;

        makePaste(cmd.getSender(), type);
    }

    private void makePaste(CommandSender sender, ResultsType type) {
        List<String> body = new ArrayList<>();
        body.add(MiscUtils.lineNoStrike());
        double total = 0;
        Map<String, Tuple<Integer, Double>> results = Kauri.INSTANCE.profiler.results(type);


        for (String key : results.keySet()
                .stream()
                .sorted(Comparator.comparing(key -> results.get(key).two, Comparator.reverseOrder()))
                .collect(Collectors.toList())) {
            //Converting nanoseconds to millis to be more readable.
            double amount = results.get(key).two / 1000000D;

            total+= amount;
            body.add(key + ": " + amount + "ms (" + results.get(key).one + " calls, std="
                    + Kauri.INSTANCE.profiler.stddev.get(key) + ")");
        }
        body.add(" ");
        body.add("Total: " + total + "ms");
        body.add("Total Calls: " + Kauri.INSTANCE.profiler.totalCalls);
        body.add("Current Ticks: " + Atlas.getInstance().getCurrentTicks());
        StringBuilder builder = new StringBuilder();
        body.forEach(aBody -> builder.append(aBody).append(";"));

        builder.deleteCharAt(body.size() - 1);

        String bodyString = builder.toString().replaceAll(";", "\n");

        try {
            sender.sendMessage(Color.Green + "Results: " + Pastebin.makePaste(bodyString,
                    "Kauri Profile: "
                            + DateFormatUtils.format(System.currentTimeMillis(),
                            ", ",
                            TimeZone.getTimeZone("604")),
                    Pastebin.Privacy.UNLISTED));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
