package cc.funkemunky.anticheat.impl.menu;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.ItemBuilder;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.button.ClickAction;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@Init
public class
MenuUtils {
    public static boolean hasModifiedChecks = false;

    /* Removed these because I was annoyed about it working properly. It didn't feel right and natural. Without it saving
       the state it should be much more natural and intuitive. -funkemunky
     */
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
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + "Edit Checks: " + Color.Blue + type.toString(), 6);;
        boolean buildAtEnd = false;

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

        if(buildAtEnd) {
            menu.buildInventory(false);
        } else menu.showMenu(toOpen);
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

        if (page > 1) {
            menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair) -> {
                openLogGUI(player, target, page - 1);
            }));
        }

        if (logs.size() > pageMax) {
            menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair) -> {
                openLogGUI(player, target, page + 1);
            }));
        }

        val clearLogsButton = createButton(false, MiscUtils.createItem(Material.REDSTONE, 1, Color.Red + "Clear Logs"),
                ((player, infoPair) -> {
                    logs.clear();
                    Kauri.getInstance().getLoggerManager().clearLogs(target.getUniqueId());
                    infoPair.getMenu().close(player);
                    openLogGUI(player, target);
                }));
        menu.setItem(53, clearLogsButton);
        menu.setItem(45, clearLogsButton);

        ItemBuilder playerHead = new ItemBuilder(Material.SKULL_ITEM);

        playerHead.amount(1);
        playerHead.durability(3);
        playerHead.name(Color.Red + target.getName());
        playerHead.owner(target.getName());
        playerHead.lore("", "&7Banned: &f" + (banReason.equals("none") ? "Not by Kauri." : banReason), "&7Last On: &f" + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - target.getLastPlayed(), true, true) + " ago");

        val playerInfoButton = new Button(false, playerHead.build());

        menu.setItem(49, playerInfoButton);
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
                createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gold + "Kauri Anticheat", "", "&7You are using Kauri Anticheat &e" + Kauri.getInstance().getDescription().getVersion() + "&7.", "", "&eBanned&8: &f" + Kauri.getInstance().getStatsManager().getBanned(), "&eFlagged&8: &f" + Kauri.getInstance().getStatsManager().getFlagged(), "", "&7If you have any issues or questions, please", "&fLeft Click &7to get the link to our Support Discord."),
                        ((player, infopair) -> {
                            if (infopair.getClickType().toString().contains("LEFT")) {
                                player.sendMessage(Color.translate("&eOur Support Discord&8: &fhttp://discord.me/Brighten"));
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

    public static void openRecentViolators(Player toOpen, int page) {
        ChestMenu menu = new ChestMenu(Color.Dark_Gray + "Recent Violators", 6);

        val recentViolations = Kauri.getInstance().getLoggerManager().getRecentViolators();


        val buttons = recentViolations.stream().map(uuid -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);

            ItemBuilder playerHead = new ItemBuilder(Material.SKULL_ITEM);

            playerHead.amount(1);
            playerHead.durability(3);
            playerHead.owner(target.getName());
            playerHead.name(Color.Yellow + target.getName());
            playerHead.lore("", "&f&oLeft Click &7&oto view logs.", "&f&oRight Click &7&oto teleport.");

            return new Button(false, playerHead.build(), (player, action) -> {
                if(action.getClickType().toString().contains("LEFT")) {
                    openLogGUI(player, target);
                } else {
                    val playerTarget = Bukkit.getPlayer(target.getUniqueId());

                    if(playerTarget != null) {
                        player.teleport(playerTarget);
                    } else {
                        player.sendMessage(Color.Red + "Could not teleport you to that player since he/she is not online.");
                    }
                }
            });
        }).collect(Collectors.toList());
        int pageMax = Math.min(buttons.size(), page * 36), pageMin = Math.min(buttons.size(), (page - 1) * 36);
        if (page > 1) {
            menu.setItem(48, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Backward Page: " + Color.White + (page - 1)), (player, infoPair) -> {
                openRecentViolators(player, page  - 1);
            }));
        }

        if (buttons.size() > pageMax) {
            menu.setItem(50, createButton(false, MiscUtils.createItem(Material.SIGN, 1, Color.Gray + "Forward Page: " + Color.White + (page + 1)), (player, infoPair) -> {
                openRecentViolators(player, page + 1);
            }));
        }

        for(int i = pageMin ; i < pageMax ; i++) {
            menu.addItem(buttons.get(i));
        }

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
                            hasModifiedChecks = true;
                            openCheckEditGUI(player2, page);
                        }
                        break;
                        case SHIFT_LEFT: {
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".executable", !check.isExecutable());
                            check.setExecutable(!check.isExecutable());

                            hasModifiedChecks = true;
                            openCheckEditGUI(player2, page);
                            break;
                        }
                        case RIGHT: {
                            Kauri.getInstance().getConfig().set("checks." + check.getName() + ".cancellable", !check.isCancellable());
                            check.setCancellable(!check.isCancellable());

                            hasModifiedChecks = true;
                            openCheckEditGUI(player2, page);
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
            switch (infoPair.getClickType()) {
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
