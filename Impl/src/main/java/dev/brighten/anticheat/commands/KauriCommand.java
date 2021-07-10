package dev.brighten.anticheat.commands;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.XMaterial;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletions;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Pastebin;
import lombok.val;
import net.minecraft.server.v1_7_R4.CommandSeed;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Init
@CommandAlias("kauri|anticheat")
@CommandPermission("kauri.command")
public class KauriCommand extends BaseCommand {

    private static List<Player> testers = new ArrayList<>();

    public KauriCommand() {
        //Registering completions
        BukkitCommandCompletions cc = (BukkitCommandCompletions) Kauri.INSTANCE.commandManager
                .getCommandCompletions();

        cc.registerCompletion("checks", (c) ->
            Check.checkClasses.values().stream().map(CheckInfo::name).collect(Collectors.toList()));
        cc.registerCompletion("materials", (c) -> Arrays.stream(Material.values()).map(Enum::name)
                .collect(Collectors.toList()));
    }

    @HelpCommand
    @Syntax("")
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
        if(testers.contains(player)) {
            if(testers.remove(player)) {
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("tester-remove-success", "&cRemoved you from test messaging for developers."));
            } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("tester-remove-error", "&cThere was an error removing you from test messaging."));
        } else {
            testers.add(player);
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
            if(data.alerts = !data.alerts) {
                Kauri.INSTANCE.dataManager.hasAlerts.add(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                        "&aYou are now viewing cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.hasAlerts.remove(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-none",
                        "&cYou are no longer viewing cheat alerts."));
            }
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Subcommand("alerts dev")
    @Syntax("")
    @CommandPermission("kauri.command.alerts.dev")
    @Description("Toggle developer cheat alerts")
    public void onDevAlerts(Player player) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data != null) {
            if(data.devAlerts = !data.devAlerts) {
                Kauri.INSTANCE.dataManager.devAlerts.add(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-on",
                        "&aYou are now viewing developer cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.devAlerts.remove(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-none",
                        "&cYou are no longer viewing developer cheat alerts."));
            }
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Subcommand("debug")
    @Syntax("<check> [player]")
    @CommandPermission("kauri.command.debug")
    @Description("debug a check")
    @CommandCompletion("@checks @players")
    public void onCommand(Player player, @Single String check, OnlinePlayer target) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data == null) {
            player.sendMessage(Color.Red + "There was an error trying to find your data object.");
            return;
        }

        if(target != null) {
            if(check.equalsIgnoreCase("sniff")) {
                val targetData = Kauri.INSTANCE.dataManager.getData(target.getPlayer());
                if(!targetData.sniffing) {
                    player.sendMessage("Sniffing + " + target.getPlayer().getName());
                    targetData.sniffing = true;
                } else {
                    player.sendMessage("Stopped sniff. Pasting...");
                    targetData.sniffing = false;
                    try {
                        player.sendMessage("Paste: " + Pastebin.makePaste(
                                String.join("\n", targetData.sniffedPackets.toArray(new String[0])),
                                "Sniffed from " + target.getPlayer().getName(), Pastebin.Privacy.UNLISTED));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    targetData.sniffedPackets.clear();
                }
            } else {
                if(Check.isCheck(check.replace("_", " "))) {
                    data.debugging = check.replace("_", " ");
                    data.debugged = target.getPlayer().getUniqueId();

                    player.sendMessage(Color.Green + "You are now debugging " + data.debugging
                            + " on target " + target.getPlayer().getName() + "!");
                } else player
                        .sendMessage(Color.Red + "The argument input \"" + check + "\" is not a check.");
            }
        } else player.sendMessage(Color.Red + "Could not find a target to debug.");
    }

    @Subcommand("block")

    @Command(name = "kauri.block", description = "Check the material type information.",
            display = "block [id,name]", permission = "kauri.command.block")
    public void onBlock(CommandAdapter cmd) {
        Material material;
        if(args.length > 0) {
            if(MiscUtils.isInteger(args[0])) {
                material = Material.getMaterial(Integer.parseInt(args[0]));
            } else material = Arrays.stream(Material.values())
                    .filter(mat -> mat.name().equalsIgnoreCase(args[0])).findFirst()
                    .orElse((XMaterial.AIR.parseMaterial()));
        } else if(cmd.getSender() instanceof Player) {
            if(player.getItemInHand() != null) {
                material = player.getItemInHand().getType();
            } else {
                cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("block-no-item-in-hand",
                                "&cPlease hold an item in your hand or use the proper arguments."));
                return;
            }
        } else {
            cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("error-invalid-args", "&cInvalid arguments! Check the help page."));
            return;
        }

        if(material != null) {
            cmd.getSender().sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
            cmd.getSender().sendMessage(Color.Gold + Color.Bold + material.name() + Color.Gray + ":");
            cmd.getSender().sendMessage("");
            cmd.getSender().sendMessage(Color.translate("&eXMaterial: &f" + XMaterial
                    .requestXMaterial(material.name(), (byte)0)));
            cmd.getSender().sendMessage(Color.translate("&eBitmask&7: &f" + Materials.getBitmask(material)));
            WrappedClass wrapped = new WrappedClass(Materials.class);

            wrapped.getFields(field -> field.getType().equals(int.class) && Modifier.isStatic(field.getModifiers()))
                    .stream().sorted(Comparator.comparing(field -> field.getField().getName()))
                    .forEach(field -> {
                        int bitMask = field.get(null);

                        boolean flag = Materials.checkFlag(material, bitMask);
                        cmd.getSender().sendMessage(Color.translate("&e" + field.getField().getName()
                                + "&7: " + (flag ? "&a" : "&c") + flag));
                    });
            cmd.getSender().sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
        } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("block-no-material", "&cNo material was found. Please check your arguments."));
    }

    @Command(name = "kauri.debug.none", aliases = {"debug.none"}, permission = "kauri.command.debug", usage = "/<command>",
            playerOnly = true, display = "debug none", description = "turn off debugging")
    public void onDebugOff(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data == null) {
            player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("error-data-object", "&cThere was an error trying to find your data object."));
            return;
        }

        data.debugging = null;
        data.debugged = null;
        Kauri.INSTANCE.dataManager.debugging.remove(data);

        Kauri.INSTANCE.dataManager.dataMap.values().stream()
                .filter(d -> d.boxDebuggers.contains(player))
                .forEach(d -> d.boxDebuggers.remove(player));
        player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-off", "&aTurned off your debugging."));
    }

    @Command(name = "kauri.debug.box", aliases = {"debug.box"}, permission = "kauri.command.debug", usage = "/<command>",
            playerOnly = true, display = "debug box [player...]", description = "debug the collisions of players.")
    public void onDebugBox(CommandAdapter cmd) {
        String[] debuggingPlayers;
        ObjectData.debugBoxes(false, player);
        if(args.length == 0) {
            ObjectData.debugBoxes(true, player, player.getUniqueId());
            debuggingPlayers = new String[] {player.getName()};
        } else {
            ObjectData.debugBoxes(true, player,
                    debuggingPlayers = args);
        }

        player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-boxes", "&aYou are now debugging the collisions of %players%.")
                .replace("%players%", String.join(", ", debuggingPlayers)));

    }

   /* @Command(name = "kchecksum", permission = "kauri.command.admin.checksum")
    public void onChecksum(CommandAdapter cmd) {
        Method c = J;
        String className = c.getName();
        String classAsPath = ;
        InputStream stream = c.getClassLoader().getResourceAsStream(classAsPath);

        try {
            byte[] array = MiscUtils.toByteArray(stream);

            String hash = GeneralHash.getSHAHash(array, GeneralHash.SHAType.SHA1);

            cmd.getSender().sendMessage("Checksum: " + hash);
            System.out.println("checksum: " + hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

   public static List<Player> getTesters() {
       testers.stream().filter(Objects::isNull).forEach(testers::remove);

       return testers;
   }
}
