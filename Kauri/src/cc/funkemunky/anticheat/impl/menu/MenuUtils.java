package cc.funkemunky.anticheat.impl.menu;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.menu.Menu;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.button.ClickAction;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Init
public class
MenuUtils {
    public static boolean hasModifiedChecks = false;

    @ConfigSetting(path = "data.gui", name = "enabled")
    private static boolean enabled;

    @ConfigSetting(path = "data.gui", name = "executable")
    private static boolean executable;

    @ConfigSetting(path = "data.gui", name = "cancellable")
    private static boolean cancellable;

    private static Button createButton(boolean moveable, ItemStack stack, ClickAction action) {
        return new Button(moveable, stack, action);
    }

    public static void openCheckEditGUI(Player toOpen, int page) {
        CheckType type = CheckType.values()[Math.min(CheckType.values().length, page) - 1];
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + "Edit Checks: " + Color.Blue + type.toString(), 6);

        boolean isBeginning = page <= 1, isEnd = page >= CheckType.values().length;
        Kauri.getInstance().getCheckManager().getChecks().stream().filter(check -> check.getType().equals(type)).forEach(check -> menu.addItem(checkButton(check, page)));

        menu.setItem(45, getModifyAllButton(page));
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

        menu.setItem(53, getModifyAllButton(page));

        if (hasModifiedChecks) {
            menu.setItem(menu.getMenuDimension().getSize() - 1, saveChangesButton(page));
        }

        menu.showMenu(toOpen);
    }

    public static void openLogGUI(Player toOpen, OfflinePlayer target) {
        openLogGUI(toOpen, target, 1);
    }

    public static void openLogGUI(Player toOpen, OfflinePlayer target, int page) {
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + target.getName() + "'s Logs", 6);

        Map<String, Integer> logs = Kauri.getInstance().getLoggerManager().getViolations(target.getUniqueId());

        String banReason = Kauri.getInstance().getLoggerManager().getBanReason(target.getUniqueId());

        int pageMax = Math.min(logs.size(), page * 36);

        List<String> keys = new ArrayList<>(logs.keySet());

        for (int i = (page - 1) * 36; i < pageMax; i++) {
            String key = keys.get(i);

            menu.addItem(createButton(false, MiscUtils.createItem(key.equalsIgnoreCase(banReason) ? Material.ENCHANTED_BOOK : Material.BOOK, 1, Color.Blue + key, "", "&eViolations&8: &f" + logs.get(key)), null));
        }

        if(page > 1) {
            menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair) -> {
                openLogGUI(player, target, page - 1);
            }));
        }

        if(logs.size() > pageMax) {
            menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair) -> {
                openLogGUI(player, target, page + 1);
            }));
        }

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

    private static Button getModifyAllButton(int page) {
        return createButton(false, MiscUtils.createItem(Material.REDSTONE, 1, Color.Green + "All Checks", "", "&fLeft Click &7to toggle all checks.", "&fMiddle &7to toggle all executable abilities in checks.", "&fRight Click &7to toggle all cancelling abilities in checks."), ((player, infoPair) -> {
            switch(infoPair.getClickType()) {
                case LEFT:
                    Kauri.getInstance().getCheckManager().getChecks().forEach(check -> {
                        Kauri.getInstance().getConfig().set("checks." + check.getName() + ".enabled", enabled);
                    });

                    hasModifiedChecks = true;
                    openCheckEditGUI(player, page);
                    updateData(UpdateDataType.ENABLED);
                    break;
                case MIDDLE:
                    Kauri.getInstance().getCheckManager().getChecks().forEach(check -> {
                        Kauri.getInstance().getConfig().set("checks." + check.getName() + ".executable", executable);
                    });

                    hasModifiedChecks = true;
                    openCheckEditGUI(player, page);
                    updateData(UpdateDataType.EXECUTABLE);
                    break;
                case RIGHT:
                    Kauri.getInstance().getCheckManager().getChecks().forEach(check -> {
                        Kauri.getInstance().getConfig().set("checks." + check.getName() + ".cancellable", cancellable);
                    });

                    hasModifiedChecks = true;
                    openCheckEditGUI(player, page);
                    updateData(UpdateDataType.CANCELLABLE);
                    break;
            }
        }));
    }

    private static void updateData(UpdateDataType type) {
        switch(type) {
            case ENABLED:
                enabled = !enabled;
                Kauri.getInstance().getConfig().set("data.gui.enabled", enabled);
                break;
            case EXECUTABLE:
                executable = !executable;
                Kauri.getInstance().getConfig().set("data.gui.executable", executable);
                break;
            case CANCELLABLE:
                cancellable = !cancellable;
                Kauri.getInstance().getConfig().set("data.gui.cancellable", cancellable);
                break;
        }
    }
}
