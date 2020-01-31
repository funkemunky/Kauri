package dev.brighten.anticheat.utils.menu.type.impl;

import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.menu.MenuListener;
import dev.brighten.anticheat.utils.menu.button.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;

import java.util.function.BiConsumer;

public class ValueMenu<T> extends ChestMenu {

    public BiConsumer<Player, T> consumer;

    public ValueMenu(BiConsumer<Player, T> consumer) {
        super("&7Value Menu", 1);

        Button valButton = new Button(false,
                new ItemBuilder(MiscUtils.m(340)).name("&eEnter Value").amount(1).build(),
                (player, info) -> {
                    AnvilInventory anvil = (AnvilInventory)
                            Bukkit.createInventory(null, InventoryType.ANVIL, "Enter Value");

                    anvil.setItem(0, new ItemBuilder(MiscUtils.m(160)).amount(1).name("  ").build());

                    player.openInventory(anvil);
                    MenuListener.anvils.put(anvil, this);
                });

        this.consumer = consumer;

        setItem(4, valButton);
    }
}
