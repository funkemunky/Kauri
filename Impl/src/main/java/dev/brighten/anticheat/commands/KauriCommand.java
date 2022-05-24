package dev.brighten.anticheat.commands;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.*;
import co.aikar.commands.*;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.listeners.generalChecks.BukkitListener;
import dev.brighten.anticheat.menu.PlayerInformationGUI;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.thread.PlayerThread;
import dev.brighten.anticheat.processing.thread.ThreadHandler;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.StringUtils;
import dev.brighten.anticheat.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Init(priority = Priority.LOW)
@CommandAlias("kauri|anticheat")
@CommandPermission("kauri.command")
public class KauriCommand extends BaseCommand {

    public KauriCommand() {
        //Registering completions
        BukkitCommandCompletions cc = (BukkitCommandCompletions) Kauri.INSTANCE.commandManager
                .getCommandCompletions();

        cc.registerCompletion("checks", (c) ->
            Check.checkClasses.values().stream().sorted(Comparator.comparing(CheckInfo::name))
                    .map(ci -> ci.name().replace(" ", "_"))
                    .collect(Collectors.toList()));
        cc.registerCompletion("materials", (c) -> Arrays.stream(Material.values()).map(Enum::name)
                .collect(Collectors.toList()));

        BukkitCommandContexts contexts = (BukkitCommandContexts) Kauri.INSTANCE.commandManager.getCommandContexts();

        contexts.registerOptionalContext(Integer.class, c -> {
            String arg = c.popFirstArg();

            if(arg == null) return null;
            try {
                return Integer.parseInt(arg);
            } catch(NumberFormatException e) {
                throw new InvalidCommandArgument(String.format(Color.Red + "Argument \"%s\" is not an integer", arg));
            }
        });
    }

    @HelpCommand
    @Syntax("")
    @Description("View the help page")
    public static void onHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
        help.showHelp();
        sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
    }
    
    @Subcommand("test")
    @Syntax("")
    @CommandPermission("kauri.command.test")
    @Description("Toggle test debug alerts")
    public void onTest(Player player) {
        if(MiscUtils.testers.contains(player.getUniqueId())) {
            if(MiscUtils.testers.remove(player.getUniqueId())) {
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("tester-remove-success", "&cRemoved you from test messaging for developers."));
            } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("tester-remove-error", "&cThere was an error removing you from test messaging."));
        } else {
            MiscUtils.testers.add(player.getUniqueId());
            player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("testers-added", "&aYou have been added to the test messaging list for developers."));
        }
    }

    @Subcommand("alerts")
    @Syntax("")
    @CommandPermission("kauri.command.alerts")
    @Description("Toggle your cheat alerts")
    public void onCommand(Player player) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data != null) {
            synchronized (Kauri.INSTANCE.dataManager.hasAlerts) {
                boolean hasAlerts = Kauri.INSTANCE.dataManager.hasAlerts.contains(data.uuid.hashCode());

                if(!hasAlerts) {
                    Kauri.INSTANCE.dataManager.hasAlerts.add(data.uuid.hashCode());
                    player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                            "&aYou are now viewing cheat alerts."));
                } else {
                    Kauri.INSTANCE.dataManager.hasAlerts.remove(data.uuid.hashCode());
                    player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-none",
                            "&cYou are no longer viewing cheat alerts."));
                }
                Kauri.INSTANCE.loggerManager.storage.updateAlerts(data.getUUID(), !hasAlerts);
            }
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Subcommand("alerts dev")
    @Syntax("")
    @CommandPermission("kauri.command.alerts.dev")
    @Description("Toggle developer cheat alerts")
    public void onDevAlertsMain(Player player) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data != null) {
            synchronized (Kauri.INSTANCE.dataManager.devAlerts) {
                boolean hasDevAlerts = Kauri.INSTANCE.dataManager.devAlerts.contains(data.uuid.hashCode());
                if(!hasDevAlerts) {
                    Kauri.INSTANCE.dataManager.devAlerts.add(data.uuid.hashCode());
                    player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-on",
                            "&aYou are now viewing developer cheat alerts."));
                } else {
                    Kauri.INSTANCE.dataManager.devAlerts.remove(data.uuid.hashCode());
                    player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-none",
                            "&cYou are no longer viewing developer cheat alerts."));
                }
                Kauri.INSTANCE.loggerManager.storage.updateDevAlerts(data.getUUID(), !hasDevAlerts);
            }
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Subcommand("debug")
    @Syntax("<check> [player]")
    @CommandPermission("kauri.command.debug")
    @Description("Debug a check")
    @CommandCompletion("@checks|none @players")
    public void onCommand(Player player, @Single String check, @Optional OnlinePlayer target) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data == null) {
            player.sendMessage(Color.Red + "There was an error trying to find your data object.");
            return;
        }

        if(check.equalsIgnoreCase("none")) {
            for (ObjectData tdata : Kauri.INSTANCE.dataManager.dataMap.values()) {
                synchronized (tdata.boxDebuggers) {
                    tdata.boxDebuggers.remove(player);
                }
                synchronized (tdata.debugging) {
                    tdata.debugging.remove(player.getUniqueId());
                }
            }

            player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("debug-off", "&aTurned off your debugging."));
        } else {
            final Player targetPlayer = target != null ? target.getPlayer() : player;
            final ObjectData targetData = Kauri.INSTANCE.dataManager.getData(targetPlayer);
            if(check.equalsIgnoreCase("sniff")) {
                if(!targetData.sniffing) {
                    player.sendMessage("Sniffing + " + targetPlayer.getName());
                    targetData.sniffing = true;
                } else {
                    player.sendMessage("Stopped sniff. Pasting...");
                    targetData.sniffing = false;
                    try {
                        player.sendMessage("Paste: " + Pastebin.makePaste(
                                String.join("\n", targetData.sniffedPackets.toArray(new String[0])),
                                "Sniffed from " + targetPlayer.getName(), Pastebin.Privacy.UNLISTED));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    targetData.sniffedPackets.clear();
                }
            } else {
                if(Check.isCheck(check.replace("_", " "))) {
                    targetData.debugging.put(player.getUniqueId(), check.replace("_", " "));

                    player.sendMessage(Color.Green + "You are now debugging " + check
                            + " on target " + targetPlayer.getName() + "!");
                } else player
                        .sendMessage(Color.Red + "The argument input \"" + check + "\" is not a check.");
            }
        }
    }

    @Subcommand("block")
    @Description("Check the material type information")
    @CommandCompletion("@materials")
    @Syntax("[id,name]")
    @CommandPermission("kauri.command.block")
    public void onBlock(CommandSender sender, @Optional String block) {
        Material material;
        if(block != null) {
            if(MiscUtils.isInteger(block)) {
                material = Material.getMaterial(Integer.parseInt(block));
            } else material = Arrays.stream(Material.values())
                    .filter(mat -> mat.name().equalsIgnoreCase(block)).findFirst()
                    .orElse((XMaterial.AIR.parseMaterial()));
        } else if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.getItemInHand() != null) {
                material = player.getItemInHand().getType();
            } else {
                sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("block-no-item-in-hand",
                                "&cPlease hold an item in your hand or use the proper arguments."));
                return;
            }
        } else {
            sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("error-invalid-args", "&cInvalid arguments! Check the help page."));
            return;
        }

        if(material != null) {
            sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.Gold + Color.Bold + material.name() + Color.Gray + ":");
            sender.sendMessage("");
            sender.sendMessage(Color.translate("&eXMaterial: &f" + XMaterial
                    .matchXMaterial(material.name()).map(Enum::name).orElse("None")));
            sender.sendMessage(Color.translate("&eBitmask&7: &f" + Materials.getBitmask(material)));
            WrappedClass wrapped = new WrappedClass(Materials.class);

            wrapped.getFields(field -> field.getType().equals(int.class) && Modifier.isStatic(field.getModifiers()))
                    .stream().sorted(Comparator.comparing(field -> field.getField().getName()))
                    .forEach(field -> {
                        int bitMask = field.get(null);

                        boolean flag = Materials.checkFlag(material, bitMask);
                        sender.sendMessage(Color.translate("&e" + field.getField().getName()
                                + "&7: " + (flag ? "&a" : "&c") + flag));
                    });
            sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
        } else sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("block-no-material", "&cNo material was found. Please check your arguments."));
    }

    @Subcommand("debug box")
    @CommandPermission("kauri.command.debug")
    @Description("debug the collisions of players")
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onDebugBox(Player player, @Optional OnlinePlayer target) {
        String[] debuggingPlayers;
        ObjectData.debugBoxes(false, player);
        if(target == null) {
            ObjectData.debugBoxes(true, player, player.getUniqueId());
            debuggingPlayers = new String[] {player.getName()};
        } else {
            debuggingPlayers = new String[] {target.getPlayer().getName()};
            ObjectData.debugBoxes(true, player, target.getPlayer().getUniqueId());
        }

        player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-boxes", "&aYou are now debugging the collisions of %players%.")
                .replace("%players%", String.join(", ", debuggingPlayers)));
    }

    @Subcommand("delay")
    @Description("change the delay between alerts")
    @Syntax("[ms]")
    @CommandPermission("kauri.command.delay")
    public void onCommand(CommandSender sender, long delay) {
        sender.sendMessage(Color.Gray + "Setting delay to "
                + Color.White + delay + "ms" + Color.Gray + "...");

        Config.alertsDelay = delay;
        Kauri.INSTANCE.getConfig().set("alerts.delay", delay);
        Kauri.INSTANCE.saveConfig();
        sender.sendMessage(Color.Green + "Delay set!");
    }

    @Subcommand("forceban")
    @Description("force ban a player")
    @Syntax("<player> [reason]")
    @CommandCompletion("@players")
    @CommandPermission("kauri.command.forceban")
    public void onForceBan(CommandSender sender, OnlinePlayer target, @Optional @Split(" ") String reason) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(target.getPlayer().getPlayer());

        MiscUtils.forceBanPlayer(data, reason != null ? reason : "N/A");
        sender.sendMessage(Color.Green + "Force banned the player.");
    }

    @Subcommand("bugreport")
    @Description("get information for reporting bugs")
    @CommandPermission("kauri.command.bugreport")
    public void onBugReport(CommandSender sender) {
        Kauri.INSTANCE.executor.execute(() -> {
            StringBuilder txt = new StringBuilder("Plugins: ");

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                txt.append("\n- ").append(plugin.getName()).append(", ").append(plugin.getDescription().getVersion());
            }

            final String separatorLine = cc.funkemunky.api.utils.MiscUtils.lineNoStrike();
            txt.append("\n").append(separatorLine).append("\nConfig:\n");

            try {
                String line = null;
                BufferedReader br = new BufferedReader(new FileReader(
                        Kauri.INSTANCE.getDataFolder().getPath() + File.separatorChar + "config.yml"));

                while((line = br.readLine()) != null) {
                    txt.append("\n").append(line);
                }

                txt.append("\n").append(separatorLine).append("\nLatest Log:");

                br = new BufferedReader(new FileReader("logs" + File.separatorChar + "latest.log"));
                while ((line = br.readLine()) != null) {
                    txt.append("\n").append(line);
                }

                txt.append("\n").append(separatorLine);

                sender.sendMessage(getMsg("command-bugreport-paste", "&aBug Report Information: &f%pastebin%")
                        .replace("%pastebin%", Pastebin.makePaste(txt.toString(), "Bug Report Paste by "
                                        + sender.getName(),
                                Pastebin.Privacy.UNLISTED)));
            } catch (IOException e) {
                sender.sendMessage(getMsg("unknown-error", "An unknown error occurred. Check console."));
                e.printStackTrace();
            }
        });
    }

    private static String getMsg(String name, String def) {
        return Kauri.INSTANCE.msgHandler.getLanguage().msg("command.lag." + name, def);
    }

    @Subcommand("lag")
    @Description("view important lag information")
    @Syntax("")
    @CommandPermission("kauri.command.lag")
    public void onCommand(CommandSender sender) {
        StringUtils.Messages.LINE.send(sender);
        MiscUtils.sendMessage(sender, getMsg("main.title",
                Color.Gold + Color.Bold + "Server Lag Information"));
        sender.sendMessage("");
        MiscUtils.sendMessage(sender, getMsg("main.tps", "&eTPS&8: &f%.2f%%"),
                Kauri.INSTANCE.getTps());
        AtomicLong chunkCount = new AtomicLong(0);
        Bukkit.getWorlds().forEach(world -> chunkCount.addAndGet(world.getLoadedChunks().length));
        MiscUtils.sendMessage(sender, getMsg("main.chunks", "&eChunks&8: &f%s"), chunkCount.get());
        MiscUtils.sendMessage(sender, getMsg("main.threads", "&eKauri Threads: &f%s"),
                ThreadHandler.INSTANCE.threadCount());
        int currentThread = 1;
        for (PlayerThread service : ThreadHandler.INSTANCE.getServices()) {
            MiscUtils.sendMessage(sender, getMsg("main.threads.list", "&8- &f%name%&7: &c%count%")
                    .replace("%name%", "Kauri Player Thread " + currentThread)
                    .replace("%count%", String.valueOf(service.getCount())));
        }
        MiscUtils.sendMessage(sender, getMsg("main.memory",
                "&eMemory &7(&f&oFree&7&o/&f&oTotal&7&o/&f&oAllocated&7)&8: &f%.2fGB&7/&f%.2fGB&7/&f%.2fGB"),
                Runtime.getRuntime().freeMemory() / 1E9,
                Runtime.getRuntime().totalMemory() / 1E9, Runtime.getRuntime().maxMemory() / 1E9);
        StringUtils.Messages.LINE.send(sender);
    }

    @Subcommand("lag gc")
    @CommandPermission("kauri.command.lag.gc")
    @Description("run a java garbage collector")
    public void onLagGc(CommandSender sender) {
        sender.sendMessage(getMsg("start-gc", "&7Starting garbage collector..."));

        long stamp = System.nanoTime();
        double time;
        Runtime.getRuntime().gc();
        time = (System.nanoTime() - stamp) / 1E6D;

        StringUtils.Messages.GC_COMPLETE.send(sender, time);
    }

    @Subcommand("lag player")
    @Description("view a player's connection info")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @CommandPermission("kauri.command.lag.player")
    public void onLagPlayer(CommandSender sender, OnlinePlayer target) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(target.getPlayer());

        if(data != null) {
            StringUtils.Messages.LINE.send(sender);
            StringUtils.sendMessage(sender, Color.Gold + Color.Bold + target.getPlayer().getName()
                    + "'s Lag Information");
            StringUtils.sendMessage(sender, "");
            StringUtils.sendMessage(sender, "&ePing&7: &f"
                    + data.lagInfo.ping + "ms&7/&f" + data.lagInfo.transPing + " tick");
            StringUtils.sendMessage(sender, "&eLast Skip&7: &f" + data.lagInfo.lastPacketDrop.getPassed());
            StringUtils.sendMessage(sender, "&eLagging&7: &f" + data.lagInfo.lagging);
            StringUtils.Messages.LINE.send(sender);
        } else StringUtils.Messages.DATA_ERROR.send(sender);
    }

    @Subcommand("wand")
    @Description("Debug collision boxes")
    @CommandPermission("kauri.command.wand")
    public void onWand(Player player) {
        if(Arrays.stream(player.getInventory().getContents())
                .anyMatch(item -> item == null || item.getType().equals(XMaterial.AIR.parseMaterial()))) {
            player.getInventory().addItem(BukkitListener.MAGIC_WAND);
            player.updateInventory();
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), BukkitListener.MAGIC_WAND);
            player.sendMessage(Color.Red + Color.Italics + "Your inventory was full. Item dropped onto ground.");
        }
        player.sendMessage(Color.Green + "Added a magic wand to your inventory. Use it wisely.");
    }

    @Subcommand("info|pi|playerinfo")
    @Description("Get the information of a player.")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @CommandPermission("kauri.command.info")
    public void onCommand(Player player, String[] args) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);

                if(target != null) {
                    ObjectData targetData = Kauri.INSTANCE.dataManager.getData(target);

                    if(targetData != null) {
                        PlayerInformationGUI info = new PlayerInformationGUI(targetData);

                        RunUtils.task(() -> {
                            info.showMenu(player);
                            player.sendMessage(Color.Green + "Opened menu.");
                        }, Kauri.INSTANCE);
                    } else player
                            .sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                                    "&cThere was an error trying to find your data."));
                } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("player-not-online", "&cThe player provided is not online!"));
            } else player.sendMessage(Color.Red + "Invalid arguments.");
        });
    }

    @Subcommand("simlag")
    @Syntax("[amount]")
    @Description("Simulate lag on the netty threads")
    @CommandPermission("kauri.command.simlag")
    public void onSimLag(CommandSender sender, @Optional Integer amount) {
        PacketProcessor.simLag = !PacketProcessor.simLag;
        if(amount != null)
        PacketProcessor.amount = amount;

        sender.sendMessage(String.format(Color.translate("&aSimLag (%s): "
                + (PacketProcessor.simLag ? "&aenabled" : "&cdisabled")), PacketProcessor.amount));
    }

    @Subcommand("recentlogs")
    @Description("View the latest violations")
    @CommandPermission("kauri.command.recentlogs")
    public void onRecentLogs(Player player) {
        player.sendMessage(Color.Green + "Finding recent violators...");
        Kauri.INSTANCE.executor.execute(() -> {
            Menu menu = MenuCommand.getRecentViolatorsMenu(false);

            RunUtils.task(() -> menu.showMenu(player));
        });
    }

    @Subcommand("reload")
    @Description("Reload Kauri")
    @CommandPermission("kauri.command.reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage(Color.Red + "Reloading Kauri...");
        Kauri.INSTANCE.reload();
        sender.sendMessage(Color.Green + "Completed!");
    }

    @Subcommand("toggle")
    @Description("Toggle a detection on or off")
    @CommandPermission("kauri.command.toggle")
    @Syntax("<check>")
    @CommandCompletion("@checks")
    public void onCommand(CommandSender sender, @Single String check) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(Check.isCheck(check)) {
                CheckInfo checkInfo = Check.getCheckInfo(check.replace("_", " "));

                String path = "checks." + checkInfo.name() + ".enabled";

                boolean toggleState = !Kauri.INSTANCE.getConfig().getBoolean(path);

                sender.sendMessage(Color.Gray + "Setting check state to "
                        + (toggleState ? Color.Green : Color.Red) + toggleState + Color.Gray + "...");
                sender.sendMessage(Color.Red + "Setting in config...");
                Kauri.INSTANCE.getConfig().set(path, toggleState);
                Kauri.INSTANCE.saveConfig();

                sender.sendMessage(Color.Red + "Refreshing data objects with updated information...");
                synchronized (Kauri.INSTANCE.dataManager.dataMap) {
                    Kauri.INSTANCE.dataManager.dataMap.values().iterator()
                            .forEachRemaining(data ->
                                    data.checkManager.checks.get(checkInfo.name()).enabled = toggleState);
                }
                sender.sendMessage(Color.Green + "Completed!");
            } else sender.sendMessage(Color.Red + "\"" + check
                    .replace("_", " ") + "\" is not a check.");
        });
    }

    @Subcommand("users")
    @Description("View the online players")
    @CommandPermission("kauri.command.users")
    public void onUsersCmd(CommandSender sender) {
        Kauri.INSTANCE.executor.execute(() -> {
            sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.Yellow + "Forge Users:");
            sender.sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.modData != null)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            sender.sendMessage("");
            sender.sendMessage(Color.Yellow + "Lunar Client Users:");
            sender.sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.usingLunar)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            sender.sendMessage("");
            sender.sendMessage(Color.Yellow + "Misc Users:");
            sender.sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.modData == null && !data.usingLunar)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            sender.sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
        });
    }
}
