package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_7;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutHeldItemSlot;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Pastebin;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

@Init(commands = true)
public class DebugCommand {

    @Command(name = "kauri.setslot", permission = "kauri.command.debug", usage = "/<command> <slot>",
            display = "setslot <slot>", description = "Set your slot.", playerOnly = true)
    public void onSlot(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            if(!MiscUtils.isInteger(cmd.getArgs()[0])) {
                cmd.getSender().sendMessage(Color.Red + "Argument provided is not a slot.");
                return;
            }

            int slot = Integer.parseInt(cmd.getArgs()[0]);

            if(slot < 0 || slot > 8) {
                cmd.getSender().sendMessage(Color.Red + "You must provide a number between 0 and 8.");
                return;
            }

            WrappedOutHeldItemSlot packet = new WrappedOutHeldItemSlot(slot);
            TinyProtocolHandler.sendPacket(cmd.getPlayer(), packet);

            cmd.getSender().sendMessage(Color.Green + "Set your item slot to slot " + slot);
        } else cmd.getSender().sendMessage(Color.Red + "Invalid arguments.");
    }

    @Command(name = "kauri.debug", aliases = {"debug"}, permission = "kauri.command.debug",
            usage = "/<command> <check> [player]", display = "debug", description = "debug a check", playerOnly = true)
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

            if(data == null) {
                cmd.getPlayer().sendMessage(Color.Red + "There was an error trying to find your data object.");
                return;
            }
            UUID target = cmd.getArgs().length > 1
                    ? Bukkit.getOfflinePlayer(cmd.getArgs()[1]).getUniqueId() : cmd.getPlayer().getUniqueId();

            if(target != null) {
                if(cmd.getArgs()[0].equalsIgnoreCase("sniff")) {
                    Player targetPl;
                    if((targetPl = Bukkit.getPlayer(target)) != null) {
                        val targetData = Kauri.INSTANCE.dataManager.getData(targetPl);
                        if(!targetData.sniffing) {
                            cmd.getSender().sendMessage("Sniffing + " + targetPl.getName());
                            targetData.sniffing = true;
                        } else {
                            cmd.getSender().sendMessage("Stopped sniff. Pasting...");
                            targetData.sniffing = false;
                            try {
                                cmd.getSender().sendMessage("Paste: " + Pastebin.makePaste(
                                        String.join("\n", targetData.sniffedPackets.stream().toArray(String[]::new)),
                                        "Sniffed from " + targetPl.getName(), Pastebin.Privacy.UNLISTED));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            targetData.sniffedPackets.clear();
                        }
                    }
                } else {
                    if(Check.isCheck(cmd.getArgs()[0].replace("_", " "))) {
                        data.debugging = cmd.getArgs()[0].replace("_", " ");
                        data.debugged = target;

                        cmd.getPlayer().sendMessage(Color.Green + "You are now debugging " + data.debugging
                                + " on target " + Bukkit.getOfflinePlayer(target).getName() + "!");
                    } else cmd.getPlayer()
                            .sendMessage(Color.Red + "The argument input \"" + cmd.getArgs()[0] + "\" is not a check.");
                }
            } else cmd.getPlayer().sendMessage(Color.Red + "Could not find a target to debug.");
        } else cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("error-invalid-args", "&cInvalid arguments! Check the help page."));
    }

    @Command(name = "kauri.block", description = "Check the material type information.",
            display = "block [id,name]", permission = "kauri.command.block")
    public void onBlock(CommandAdapter cmd) {
        Material material;
        if(cmd.getArgs().length > 0) {
            if(MiscUtils.isInteger(cmd.getArgs()[0])) {
                material = Material.getMaterial(Integer.parseInt(cmd.getArgs()[0]));
            } else material = Arrays.stream(Material.values())
                    .filter(mat -> mat.name().equalsIgnoreCase(cmd.getArgs()[0])).findFirst()
                    .orElse((XMaterial.AIR.parseMaterial()));
        } else if(cmd.getSender() instanceof Player) {
            if(cmd.getPlayer().getItemInHand() != null) {
                material = cmd.getPlayer().getItemInHand().getType();
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
            cmd.getSender().sendMessage(Color.translate("&eBitmask&7: &f" + Materials.getBitmask(material)));
            WrappedClass wrapped = new WrappedClass(Materials.class);

            wrapped.getFields(field -> field.getType().equals(int.class) && Modifier.isStatic(field.getModifiers()))
                    .stream().sorted(Comparator.comparing(field -> field.getField().getName()))
                    .forEach(field -> {
                        int bitMask = field.get(null);

                        cmd.getSender().sendMessage(Color.translate("&e" + field.getField().getName()
                                + "&7: &f" + Materials.checkFlag(material, bitMask)));
                    });
            cmd.getSender().sendMessage(cc.funkemunky.api.utils.MiscUtils.line(Color.Dark_Gray));
        } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("block-no-material", "&cNo material was found. Please check your arguments."));
    }

    @Command(name = "kauri.debug.none", aliases = {"debug.none"}, permission = "kauri.command.debug", usage = "/<command>",
            playerOnly = true, display = "debug none", description = "turn off debugging")
    public void onDebugOff(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data == null) {
            cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("error-data-object", "&cThere was an error trying to find your data object."));
            return;
        }

        data.debugging = null;
        data.debugged = null;
        Kauri.INSTANCE.dataManager.debugging.remove(data);

        Kauri.INSTANCE.dataManager.dataMap.values().stream()
                .filter(d -> d.boxDebuggers.contains(cmd.getPlayer()))
                .forEach(d -> d.boxDebuggers.remove(cmd.getPlayer()));
        cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-off", "&aTurned off your debugging."));
    }

    @Command(name = "kauri.debug.box", aliases = {"debug.box"}, permission = "kauri.command.debug", usage = "/<command>",
            playerOnly = true, display = "debug box [player...]", description = "debug the collisions of players.")
    public void onDebugBox(CommandAdapter cmd) {
        String[] debuggingPlayers;
        ObjectData.debugBoxes(false, cmd.getPlayer());
        if(cmd.getArgs().length == 0) {
            ObjectData.debugBoxes(true, cmd.getPlayer(), cmd.getPlayer().getUniqueId());
            debuggingPlayers = new String[] {cmd.getPlayer().getName()};
        } else {
            ObjectData.debugBoxes(true, cmd.getPlayer(),
                    debuggingPlayers = cmd.getArgs());
    }

        cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-boxes", "&aYou are now debugging the collisions of %players%.")
        .replace("%players%", String.join(", ", debuggingPlayers)));

    }

}
