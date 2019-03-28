package cc.funkemunky.anticheat.impl.menu;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.menu.Menu;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.button.ClickAction;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MenuUtils {
    public static boolean hasModifiedChecks = false;

    private static Button createButton(boolean moveable, ItemStack stack, ClickAction action) {
        return new Button(moveable, stack, action);
    }

    public static void openCheckEditGUI(Player toOpen, int page) {
        CheckType type = CheckType.values()[Math.min(CheckType.values().length, page) - 1];
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + "Edit Checks: " + Color.Blue + type.toString(), 6);

        boolean isBeginning = page <= 1, isEnd = page >= CheckType.values().length;
        Kauri.getInstance().getCheckManager().getChecks().stream().filter(check -> check.getType().equals(type)).forEach(check -> menu.addItem(checkButton(check, page)));

        if (!isBeginning) {
            menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair) -> {
                openCheckEditGUI(player, page - 1);
            }));
        }

        menu.setItem(49, createButton(false, MiscUtils.createItem(Material.COMPASS, 1, Color.Red + "Back to Main Menu", "&7&oShift Click"), (player, infoPair) -> {
            switch (infoPair.getClickType()) {
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    openMainGUI(player);
                    break;
            }
        }));

        if (!isEnd) {
            menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair) -> {
                openCheckEditGUI(player, page + 1);
            }));
        }

        if (hasModifiedChecks) {
            menu.setItem(menu.getMenuDimension().getSize() - 1, saveChangesButton(page));
        }

        menu.showMenu(toOpen);
    }

    public static void openLogGUI(Player toOpen, OfflinePlayer target) {
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + target.getName() + "'s Logs", 6);

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
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + "Kauri Menu", 3);

        menu.setItem(11, createButton(false, MiscUtils.createItem(Material.BOOK_AND_QUILL, 1, Color.Gold + "Edit Check Settings"), ((player2, informationPair) -> {
            if (informationPair.getClickType().toString().contains("LEFT")) {
                openCheckEditGUI(player2, 1);
            }
        })));

        menu.setItem(
                13,
                createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gold + "Kauri Anticheat", "", "&7You are using &ev" + Kauri.getInstance().getDescription().getVersion() + "&7.", "", "&eBanned&8: &f" + Kauri.getInstance().getStatsManager().getBanned(), "&eFlagged&8: &f" + Kauri.getInstance().getStatsManager().getFlagged(), "", "&7If you have any issues or questions, please", "&fLeft Click &7to get the link to our Support Discord."),
                        ((player, infopair) -> {
                            if (infopair.getClickType().toString().contains("LEFT")) {
                                player.sendMessage(Color.translate("&eOur Support Discord&8: &fhttp://discord.me/Kauri"));
                                infopair.getMenu().close(player);
                            }
                        })));

        menu.setItem(15, createButton(false, MiscUtils.createItem(Material.WATCH, 1, Color.Gold + "Reload Kauri"), ((player, infoPair) -> {
            infoPair.getMenu().close(player);
            player.sendMessage(Color.translate("&8[&e&lKauri&8] &7Fully unloading and loading Kauri..."));
            Kauri.getInstance().reloadKauri();
        })));

        menu.showMenu(toOpen);
    }

    private static Button checkButton(Check check, int page) {
        List<String> lore = new ArrayList<>(Arrays.asList("&eEnabled&7: &f" + check.isEnabled(),
                "&eExecutable&7: &f" + check.isExecutable(),
                "&eCancellable&7: &f" + check.isCancellable(),
                "&eDescription&7: &f"));

        lore.addAll(Arrays.asList(splitIntoLine(check.getDescription(), 35)));
        lore.addAll(Arrays.asList("&eInstructions&7:",
                "&8- &fLeft Click &7to toggle check on/off.",
                "&8- &fShift + Left Click &7to toggle check executable-abilities.",
                "&8- &fRight Click &7to toggle check cancellable-abilities."));

        return createButton(false,
                MiscUtils.createItem(
                        Material.PAPER,
                        1,
                        Color.Gold + check.getName(), lore.toArray(new String[]{})),
                ((player2, infoPair) -> {
                    switch (infoPair.getClickType()) {
                        case LEFT: {
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".enabled", !check.isEnabled());

                            check.setEnabled(!check.isEnabled());
                            boolean isBeginning = page <= 1, isEnd = page >= CheckType.values().length;
                            Menu menu = infoPair.getMenu();
                            List<Check> checks = new ArrayList<>();
                            Kauri.getInstance().getCheckManager().getChecks().stream().filter(check2 -> check2.getType().equals(check.getType())).forEach(checks::add);

                            for (int i = 0; i < checks.size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(checks.get(i), page));
                            }
                            if (!isBeginning) {
                                menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page - 1);
                                }));
                            }

                            menu.setItem(49, createButton(false, MiscUtils.createItem(Material.COMPASS, 1, Color.Red + "Back to Main Menu", "&7&oShift Click"), (player, infoPair2) -> {
                                switch (infoPair.getClickType()) {
                                    case SHIFT_LEFT:
                                    case SHIFT_RIGHT:
                                        openMainGUI(player);
                                        break;
                                }
                            }));

                            if (!isEnd) {
                                menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page + 1);
                                }));
                            }
                            if (!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton(page));
                                infoPair.getMenu().buildInventory(false);
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                        }
                        break;
                        case SHIFT_LEFT: {
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".executable", !check.isExecutable());
                            check.setExecutable(!check.isExecutable());
                            boolean isBeginning = page <= 1, isEnd = page >= CheckType.values().length;
                            Menu menu = infoPair.getMenu();

                            List<Check> checks = new ArrayList<>();
                            Kauri.getInstance().getCheckManager().getChecks().stream().filter(check2 -> check2.getType().equals(check.getType())).forEach(checks::add);

                            for (int i = 0; i < checks.size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(checks.get(i), page));
                            }

                            if (!isBeginning) {
                                menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page - 1);
                                }));
                            }

                            menu.setItem(49, createButton(false, MiscUtils.createItem(Material.COMPASS, 1, Color.Red + "Back to Main Menu", "&7&oShift Click"), (player, infoPair2) -> {
                                switch (infoPair2.getClickType()) {
                                    case SHIFT_LEFT:
                                    case SHIFT_RIGHT:
                                        openMainGUI(player);
                                        break;
                                }
                            }));

                            if (!isEnd) {
                                menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page + 1);
                                }));
                            }
                            if (!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton(page));
                                infoPair.getMenu().buildInventory(false);
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                            break;
                        }
                        case RIGHT: {
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".cancellable", !check.isCancellable());
                            check.setCancellable(!check.isCancellable());
                            boolean isBeginning = page <= 1, isEnd = page >= CheckType.values().length;
                            Menu menu = infoPair.getMenu();
                            List<Check> checks = new ArrayList<>();
                            Kauri.getInstance().getCheckManager().getChecks().stream().filter(check2 -> check2.getType().equals(check.getType())).forEach(checks::add);

                            for (int i = 0; i < checks.size(); i++) {
                                infoPair.getMenu().setItem(i, checkButton(checks.get(i), page));
                            }
                            if (!isBeginning) {
                                menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page - 1);
                                }));
                            }

                            menu.setItem(49, createButton(false, MiscUtils.createItem(Material.COMPASS, 1, Color.Red + "Back to Main Menu", "&7&oShift Click"), (player, infoPair2) -> {
                                switch (infoPair2.getClickType()) {
                                    case SHIFT_LEFT:
                                    case SHIFT_RIGHT:
                                        openMainGUI(player);
                                        break;
                                }
                            }));

                            if (!isEnd) {
                                menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair2) -> {
                                    openCheckEditGUI(player, page + 1);
                                }));
                            }

                            if (!hasModifiedChecks) {
                                infoPair.getMenu().setItem(infoPair.getMenu().getMenuDimension().getSize() - 1, saveChangesButton(page));
                            }
                            infoPair.getMenu().buildInventory(false);
                            hasModifiedChecks = true;
                            break;
                        }
                    }
                }));
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

    private static Button saveChangesButton(int page) {
        return createButton(false, MiscUtils.createItem(Material.BOOK_AND_QUILL, 1, Color.Red + "Save Changes"), ((player2, infoPair) -> {
            hasModifiedChecks = false;
            Kauri.getInstance().saveConfig();
            Kauri.getInstance().reloadKauri();
            openCheckEditGUI(player2, page);
        }));
    }

}
