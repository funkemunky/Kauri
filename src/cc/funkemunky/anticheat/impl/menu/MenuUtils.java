package cc.funkemunky.anticheat.impl.menu;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.button.ClickAction;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class MenuUtils {
    public static boolean hasModifiedChecks = false;

    private static Button createButton(boolean moveable, ItemStack stack, ClickAction action) {
        return new Button(moveable, stack, action);
    }

    public static void openCheckEditGUI(Player toOpen) {
        ChestMenu menu = new ChestMenu(Color.Gold + "Edit Checks", 6);

        Kauri.getInstance().getCheckManager().getChecks().forEach(check -> menu.addItem(checkButton(check)));

        if(hasModifiedChecks) {
            menu.setItem(menu.getMenuDimension().getSize() - 1, saveChangesButton());
        }

        menu.showMenu(toOpen);
    }

    public static void openLogGUI(Player toOpen, OfflinePlayer target) {
        ChestMenu menu = new ChestMenu(Color.Gold + target.getName() + "'s Logs", 6);

        Map<String, Integer> logs = Kauri.getInstance().getLoggerManager().getViolations(target.getUniqueId());
        String banReason = Kauri.getInstance().getLoggerManager().getBanReason(target.getUniqueId());
        logs.keySet().forEach(key -> {
            menu.addItem(createButton(false, MiscUtils.createItem(key.equalsIgnoreCase(banReason) ? Material.ENCHANTED_BOOK : Material.BOOK, 1, Color.Blue + key, "", "&eViolations&8: &f" + logs.get(key)), null));
        });

        menu.setItem(49, createButton(false, MiscUtils.createItem(Material.REDSTONE, 1, Color.Red + "Clear Logs"),
                ((player, infoPair) -> {
                    logs.clear();
                    Kauri.getInstance().getLoggerManager().clearLogs(target.getUniqueId());
                    infoPair.getMenu().close(player);
                    openLogGUI(player, target);
                })));
        menu.showMenu(toOpen);
    }

    public static void openMainGUI(Player toOpen) {
        ChestMenu menu = new ChestMenu(Color.Gold + "Kauri Menu", 3);

        menu.setItem(11, createButton(false, MiscUtils.createItem(Material.BOOK_AND_QUILL, 1, Color.Blue + "Edit Check Settings"), ((player2, informationPair) -> {
            if(informationPair.getClickType().toString().contains("LEFT")) {
                openCheckEditGUI(player2);
            }
        })));

        menu.setItem(
                13,
                createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Blue + "Kauri Anticheat", "", "&7You are using &ev" + Kauri.getInstance().getDescription().getVersion() + "&7.", "", "&eBanned&8: &f" + Kauri.getInstance().getStatsManager().getBanned(), "&eFlagged&8: &f" + Kauri.getInstance().getStatsManager().getFlagged() , "", "&7If you have any issues or questions, please", "&fLeft Click &7to get the link to our Support Discord."),
                        ((player, infopair) -> {
                            if(infopair.getClickType().toString().contains("LEFT")) {
                                player.sendMessage(Color.translate("&eOur Support Discord&8: &fhttp://discord.me/Kauri"));
                                infopair.getMenu().close(player);
                            }
                        })));

        menu.setItem(15, createButton(false, MiscUtils.createItem(Material.WATCH, 1, Color.Blue + "Reload Kauri"), ((player, infoPair) -> {
            infoPair.getMenu().close(player);
            player.sendMessage(Color.translate("&8[&e&lKauri&8] &7Fully unloading and loading Kauri..."));
            Kauri.getInstance().reloadKauri();
        })));

        menu.showMenu(toOpen);
    }

    private static Button checkButton(Check check) {
        return createButton(false,
                MiscUtils.createItem(
                        Material.PAPER,
                        1,
                        Color.Blue + check.getName(),
                        "",
                        "&eEnabled&7: &f" + check.isEnabled(),
                        "&eExecutable&7: &f" + check.isExecutable(),
                        "&eCancellable&7: &f" + check.isCancellable(),
                        "&eType&7: &f" + check.getType().toString(),
                        "&eInstructions&7:",
                        "&8- &fLeft Click &7to toggle check on/off.",
                        "&8- &fShift + Left Click &7to toggle check executable-abilities.",
                        "&8- &fRight Click &7to toggle check cancellable-abilities."),
                ((player2, infoPair) -> {
                    switch(infoPair.getClickType()) {
                        case LEFT:
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".enabled", !check.isEnabled());

                            check.setEnabled(!check.isEnabled());
                            for (int i = 0; i < Kauri.getInstance().getCheckManager().getChecks().size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(Kauri.getInstance().getCheckManager().getChecks().get(i)));
                            }
                            if(!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton());
                                infoPair.getMenu().buildInventory(false);
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                            break;
                        case SHIFT_LEFT:
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".executable", !check.isExecutable());
                            check.setExecutable(!check.isExecutable());
                            for (int i = 0; i < Kauri.getInstance().getCheckManager().getChecks().size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(Kauri.getInstance().getCheckManager().getChecks().get(i)));
                            }
                            if(!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton());
                                infoPair.getMenu().buildInventory(false);
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                            break;
                        case RIGHT:
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".cancellable", !check.isCancellable());
                            check.setCancellable(!check.isCancellable());
                            for (int i = 0; i < Kauri.getInstance().getCheckManager().getChecks().size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(Kauri.getInstance().getCheckManager().getChecks().get(i)));
                            }
                            if(!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton());
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                            break;
                    }
                }));
    }

    private static Button saveChangesButton() {
        return createButton(false, MiscUtils.createItem(Material.BOOK_AND_QUILL, 1, Color.Red + "Save Changes"), ((player2, infoPair) -> {
            hasModifiedChecks = false;
            Kauri.getInstance().saveConfig();
            Kauri.getInstance().getDataManager().getDataObjects().values().forEach(data -> Kauri.getInstance().getCheckManager().loadChecksIntoData(data));
            Kauri.getInstance().getCheckManager().setChecks(Kauri.getInstance().getCheckManager().loadChecks());
            openCheckEditGUI(player2);
        }));
    }

}
