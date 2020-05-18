package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.data.ObjectData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CollisionHandler {
	private List<Block> blocks;
	private List<Entity> entities;
	private ObjectData data;
	private KLocation location;

	private double width, height;
	private double shift;
	@Setter
	private boolean single = false;
	@Setter
	private boolean debugging;

	public CollisionHandler(List<Block> blocks, List<Entity> entities, KLocation to) {
		this.blocks = blocks;
		this.entities = entities;
		this.location = to;
	}

	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public void setOffset(double shift) {
		this.shift = shift;
	}

	public boolean containsFlag(int bitmask) {
		for (Block b : blocks) {
			if (Materials.checkFlag(b.getType(), bitmask)) return true;
		}
		return false;
	}

	public boolean contains(EntityType type) {
		return entities.stream().anyMatch(e -> e.getType() == type);
	}

	public boolean isCollidedWith(int bitmask) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		for (Block b : blocks) {
			Location block = b.getLocation();
			if (Materials.checkFlag(b.getType(), bitmask)
					&& (!single || (block.getBlockX() == MathUtils.floor(location.x) && block.getBlockZ() == MathUtils.floor(location.z)))) {
				if (BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox)) {
					return true;
				}
			}
		}

		return false;
	}

	public List<CollisionBox> getCollisionBoxes() {
		List<CollisionBox> collided = new ArrayList<>();
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		for (Block b : blocks) {
			Location block = b.getLocation();

			CollisionBox box;
			if((box = BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())).isCollided(playerBox)) {
				collided.add(box);
			}
		}

		return collided;
	}

	public boolean isCollidedWith(CollisionBox box) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return box.isCollided(playerBox);
	}

	public boolean isCollidedWith(Material... materials) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		for (Block b : blocks) {
			if (MiscUtils.contains(materials, b.getType())) {
				if (BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox))
					return true;
			}
		}

		return false;
	}
}
