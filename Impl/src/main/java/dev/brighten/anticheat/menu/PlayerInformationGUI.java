package dev.brighten.anticheat.menu;

import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerInformationGUI extends ChestMenu {

    public ObjectData data;
    private static String line = MiscUtils.line(Color.Dark_Gray);
    private static String halfLine = line.substring(0, Math.round(line.length() / 1.5f));
    private Button playerButton, forgeButton, violationsButton;
    private BukkitTask updatingTask;
    private ModData modData;

    public PlayerInformationGUI(ObjectData data) {
        super(data.getPlayer().getName() + "'s Information", 3);
        this.data = data;
        this.modData = ForgeHandler.getMods(data.getPlayer());

        addItems();
        update();
    }

    public void addItems() {
        playerButton = new Button(false, playerSkull(), null);
        forgeButton = new Button(false, forgeItem(), (
                modData == null
                || modData.getMods().size() == 0 ? null : (player, info) -> modsGUI().showMenu(player)));
        violationsButton = new Button(false, violationsButton(), (player, info) -> {
            if(info.getClickType().name().contains("LEFT")) {
                info.getMenu().close(player);
                Bukkit.dispatchCommand(player, "kauri logs " + data.getPlayer().getName());
            }
        });

        setItem(13, playerButton);
        setItem(11, forgeButton);
        setItem(15, violationsButton);
    }

    private ItemStack playerSkull() {
        ItemBuilder vioItem = new ItemBuilder(XMaterial.SKULL_ITEM.parseMaterial());

        vioItem.amount(1);
        vioItem.durability(3);
        vioItem.name(Color.Gold + data.getPlayer().getName());
        vioItem.owner(data.getPlayer().getName());
        vioItem.lore("",
                halfLine,
                "&eVersion&7: &f" + data.playerVersion.name(),
                "&ePing/tPing&7: &f" + data.lagInfo.ping + "ms, " + data.lagInfo.transPing + "ms",
                "&eLast Packet Drop&7: &f" + DurationFormatUtils
                        .formatDurationHMS(data.lagInfo.lastPacketDrop.getPassed() * 50),
                halfLine);
        return vioItem.build();
    }

    private ItemStack forgeItem() {
        modData = ForgeHandler.getMods(data.getPlayer());
        ItemBuilder forgeItem = new ItemBuilder(XMaterial.ANVIL.parseMaterial());

        forgeItem.amount(1);
        forgeItem.name(Color.Gold + "Client  Information");

        List<String> loreList = new ArrayList<>(Arrays.asList("",
                halfLine,
                "&eUsing Forge&7: &f" + (modData != null),
                "&eUsing Lunar&7: &f" + data.usingLunar,
                ""));

        if(modData != null) {
            List<String> modList = new ArrayList<>(modData.getMods());

            loreList.add(modList.size() > 0 ? "&7&oRight click to view mods" : "&c&oNo mods.");
            modList.clear();
        } else {
            loreList.add("&c&oNo mods.");
        }
        loreList.add(halfLine);

        forgeItem.lore(loreList.stream().map(Color::translate).collect(Collectors.toList()));

        return forgeItem.build();
    }

    private ItemStack violationsButton() {
        ItemBuilder violationsItem = new ItemBuilder(XMaterial.ENCHANTED_BOOK.parseMaterial());

        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(data.getPlayer().getUniqueId());

        violationsItem.amount(1);
        violationsItem.name(Color.Gold + "Violations");
        violationsItem.lore("",
                halfLine,
                "&fThis player currently has &e" + logs.size() + " logs&f.",
                "&7&oLeft click to view logs",
                halfLine);

        logs.clear();
        return violationsItem.build();
    }

    private ChestMenu modsGUI() {
        ChestMenu subMenu = new ChestMenu(data.getPlayer().getName() + "'s Mods",
                Math.min(6, (int)Math.ceil(modData.getMods().size() / 9D)));

        subMenu.setParent(this);

        modData.getMods().forEach(string -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BOOK.parseMaterial());

            builder.amount(1);
            builder.name(Color.Gold + string);
            subMenu.addItem(new Button(false, builder.build(), null));
        });

        return subMenu;
    }

    private void update() {
        updatingTask = RunUtils.taskTimerAsync(() -> {
            if(data == null) {
                updatingTask.cancel();
                return;
            }
            playerButton.getStack().setItemMeta(playerSkull().getItemMeta());
            buildInventory(false);
        }, Kauri.INSTANCE, 80L, 50L);
    }

    @Override
    public void handleClose(Player player) {
        super.handleClose(player);
        updatingTask.cancel();
    }
}
