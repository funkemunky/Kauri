package dev.brighten.anticheat.menu;

import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.reflection.CraftReflection;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.Reflections;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedMethod;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.commands.LogCommand;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.menu.button.Button;
import dev.brighten.anticheat.utils.menu.type.impl.ChestMenu;
import lombok.val;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlayerInformation extends ChestMenu {

    public ObjectData data;
    private static String line = MiscUtils.line(Color.Dark_Gray);
    private static String halfLine = line.substring(0, Math.round(line.length() / 1.5f));
    private Button playerButton, forgeButton, violationsButton;
    private BukkitTask updatingTask;
    private ModData modData;
    private List<Log> logs;
    private static WrappedClass nbtCompound = Reflections.getNMSClass("NBTTagCompound");
    private static WrappedMethod setString = nbtCompound.getMethod("setString", String.class, String.class);

    public PlayerInformation(ObjectData data, String title, int size) {
        super(title, size);
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

        })

    }

    private ItemStack playerSkull() {
        ItemBuilder vioItem = new ItemBuilder(Material.SKULL_ITEM);

        vioItem.amount(1);
        vioItem.durability(3);
        vioItem.name(Color.Gold + data.getPlayer().getName());
        vioItem.owner(data.getPlayer().getName());
        vioItem.lore("",
                halfLine,
                "&eVersion&7: &f" + TinyProtocolHandler.getProtocolVersion(data.getPlayer()).name(),
                "&ePing/tPing&7: &f" + data.lagInfo.ping + "ms, " + data.lagInfo.transPing,
                "&eLast Packet Drop&7: &f" + DurationFormatUtils
                        .formatDurationHMS(data.lagInfo.lastPacketDrop.getPassed() * 50),
                halfLine);
        return vioItem.build();
    }

    private ChestMenu logMenu() {
        ChestMenu menu = new ChestMenu("Click the book", 3);

        String log = LogCommand.getLogsFromUUID(data.getPlayer().getUniqueId());

        ItemStack book = MiscUtils.createItem(Material.BOOK, 1, "&6Open Log",
                "",
                "&7&oLeft click to open URL",
                log);

        Object compound = nbtCompound.getConstructor().newInstance();
        setString.invoke(compound, "action", "open_url");
        setString.invoke(compound, "value", log);
        Object vanillaBook = CraftReflection.getVanillaItemStack(book);

        val setTag = MinecraftReflection.itemStack.getMethod("setTag", Object.class);

        setTag.invoke(vanillaBook, compound);
    }

    private ItemStack forgeItem() {
        ItemBuilder forgeItem = new ItemBuilder(Material.ANVIL);

        forgeItem.amount(1);
        forgeItem.name(Color.Gold + "Forge Information");

        List<String> loreList = new ArrayList<>(Arrays.asList("",
                halfLine,
                "&eUsing Forge&7: &f" + (modData != null),
                ""));

        if(modData != null) {
            List<String> modList = new ArrayList<>(modData.getMods());

            loreList.add(modList.size() > 0 ? "&7&oRight click to view mods" : "&c&oNo mods.");
            modList.clear();
        } else {
            loreList.add("&c&oNo mods.");
        }
        loreList.add(halfLine);
        forgeItem.lore(loreList);

        return forgeItem.build();
    }

    private ItemStack violationsButton() {
        ItemBuilder violationsItem = new ItemBuilder(Material.ENCHANTED_BOOK);

        logs = Kauri.INSTANCE.loggerManager.getLogs(data.getPlayer().getUniqueId());

        violationsItem.amount(1);
        violationsItem.name(Color.Gold + "Violations");
        violationsItem.lore("",
                halfLine,
                "&fThis player currently has &e" + logs.size() + " logs&f.",
                "&7&oRight click to view logs",
                halfLine);
        return violationsItem.build();
    }

    private ChestMenu modsGUI() {
        ChestMenu subMenu = new ChestMenu(data.getPlayer().getName() + "'s Mods",
                Math.min(6, (int)Math.ceil(modData.getMods().size() / 9D)));

        subMenu.setParent(this);

        modData.getMods().forEach(string -> {
            ItemBuilder builder = new ItemBuilder(Material.BOOK);

            builder.amount(1);
            builder.name(Color.Gold + string);
            subMenu.addItem(new Button(false, builder.build(), null));
        });

        return subMenu;
    }

    private void

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
}
