package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.button.ClickAction;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Init(commands = true)
public class MenuCommand {

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
        getMainMenu().showMenu(cmd.getPlayer());
        cmd.getPlayer().sendMessage(Color.Green + "Opened main menu.");
    }

    private ChestMenu getMainMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Kauri Menu", 3);

        menu.setItem(11, createButton(Material.ANVIL, 1, "&cEdit Checks", (player, info) -> {
            getChecksMenu().showMenu(player);
        }, "", "&7Toggle Kauri checks on or off."));
        menu.setItem(13, createButton(Material.ENCHANTED_BOOK, 1, "&cKauri Anticheat",
                (player, info) -> {
                    player.closeInventory();
                    player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                    player.sendMessage(Color.translate("&6Discord: &fhttps://discord.me/Brighten"));
                    player.sendMessage(Color.translate("&6Website: &fhttps://funkemunky.cc/contact"));
                    player.sendMessage(MiscUtils.line(Color.Dark_Gray));
                },
                "", "&7You are using &6Kauri Anticheat v" +
                        Kauri.INSTANCE.getDescription().getVersion(), "&e&oRight Click &7&oclick to get support."));
        menu.setItem(15, createButton(Material.PAPER, 1, "&cView Recent Violators", (player, info) -> {
            //TODO open recent violators menu.
        }, "", "&7View players who flagged checks recently."));
        menu.buildInventory(true);
        return menu;
    }

    private ChestMenu getChecksMenu() {
        ChestMenu menu = new ChestMenu(Color.Gold + "Checks", 6);

        List<CheckSettings> values = new ArrayList<>(Check.checkSettings.values());

        values.sort(Comparator.comparing(val -> val.name));

        for (int i = 0; i < values.size(); i++) {
            CheckSettings val = values.get(i);

            String enabled = "checks." + val.name+ ".enabled";
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

                                if(!settings.enabled) {
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

                                if(settings.enabled) {
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

}
