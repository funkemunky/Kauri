package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.XMaterial;
import cc.funkemunky.api.utils.math.IntVector;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

@RequiredArgsConstructor
public class Block {
    public final World world;
    public final IntVector location;
    public final XMaterial material;
    public final byte data;

    public CollisionBox getBox() {
        return BlockData.getData(material.parseMaterial()).getBox()
    }
}
