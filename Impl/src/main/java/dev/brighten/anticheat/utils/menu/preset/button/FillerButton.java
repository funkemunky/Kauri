package dev.brighten.anticheat.utils.menu.preset.button;

import dev.brighten.anticheat.utils.ItemBuilder;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.menu.button.Button;
import org.bukkit.Material;

public class FillerButton extends Button {

    public FillerButton() {
        super(false, new ItemBuilder(MiscUtils.m(160)).name(" ").durability(15).build());
    }
}
