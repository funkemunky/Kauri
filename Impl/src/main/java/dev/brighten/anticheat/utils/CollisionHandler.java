package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
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
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class CollisionHandler {
	private List<Block> blocks = new CopyOnWriteArrayList<>();
	private List<Entity> entities;
	private ObjectData data;
	private KLocation location;

	private double width, height;
	private double shift;
	@Setter
	private boolean single = false;
	@Setter
	private boolean debugging;

	public CollisionHandler(List<Block> blocks, List<Entity> entities, KLocation to, ObjectData data) {
		this.blocks.addAll(blocks);
		this.entities = entities;
		this.location = to;
		this.data = data;
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

	public boolean isCollidedWith(SimpleCollisionBox playerBox, int bitmask) {
		for (Block b : blocks) {
			Location block = b.getLocation();
			Material material = data.playerInfo.shitMap.getOrDefault(block, b.getType());

			if (Materials.checkFlag(material, bitmask)
					&& (!single || (block.getBlockX() == MathUtils.floor(location.x)
					&& block.getBlockZ() == MathUtils.floor(location.z)))) {
				if (BlockData.getData(material).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox)) {
					return true;
				}
			}
		}

		if(bitmask == 0) {
			for(Entity entity : entities) {
				if(EntityData.getEntityBox(entity.getLocation(), entity).isCollided(playerBox))
					return true;
			}
		}

		return false;
	}

	public boolean isIntersectsWith(SimpleCollisionBox playerBox, int bitmask) {
		for (Block b : blocks) {
			Location block = b.getLocation();
			Material material = data.playerInfo.shitMap.getOrDefault(block, b.getType());

			if (Materials.checkFlag(material, bitmask)
					&& (!single || (block.getBlockX() == MathUtils.floor(location.x)
					&& block.getBlockZ() == MathUtils.floor(location.z)))) {
				if (BlockData.getData(material).getBox(b, ProtocolVersion.getGameVersion()).isIntersected(playerBox)) {
					return true;
				}
			}
		}

		if(bitmask == 0) {
			for(Entity entity : entities) {
				if(EntityData.getEntityBox(entity.getLocation(), entity).isIntersected(playerBox))
					return true;
			}
		}

		return false;
	}

	public boolean isCollidedWithEntity(SimpleCollisionBox box) {
		for(Entity entity : entities) {
			if(EntityData.getEntityBox(entity.getLocation(), entity).isCollided(box))
				return true;
		}
		return false;
	}

	public boolean isCollidedWithEntity() {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return isCollidedWithEntity(playerBox);
	}

	public boolean isCollidedWith(int bitmask) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return isCollidedWith(playerBox, bitmask);
	}

	public boolean isIntersectedWith(int bitmask) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return isIntersectsWith(playerBox, bitmask);
	}

	public List<CollisionBox> getCollisionBoxes(SimpleCollisionBox playerBox) {
		List<CollisionBox> collided = new ArrayList<>();

		for (Block b : blocks) {
			Location block = b.getLocation();
			Material material = data.playerInfo.shitMap.getOrDefault(block, b.getType());

			CollisionBox box;
			if((box = BlockData.getData(material).getBox(b, ProtocolVersion.getGameVersion())).isCollided(playerBox)) {
				collided.add(box);
			}
		}

		for(Entity entity : entities) {
			CollisionBox box = EntityData.getEntityBox(entity.getLocation(), entity);
			if(box.isCollided(playerBox))
				collided.add(box);
		}

		return collided;
	}
	public List<CollisionBox> getCollisionBoxes() {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return getCollisionBoxes(playerBox);
	}

	public boolean isCollidedWith(CollisionBox box) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return box.isCollided(playerBox);
	}

	public boolean isCollidedWith(SimpleCollisionBox playerBox, Material... materials) {
		for (Block b : blocks) {
			Location block = b.getLocation();
			Material material = data.playerInfo.shitMap.getOrDefault(block, b.getType());

			if (materials.length == 0 || MiscUtils.contains(materials, material)) {
				if (BlockData.getData(material).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox))
					return true;
			}
		}

		if(materials.length == 0) {
			for(Entity entity : entities) {
				if(EntityData.getEntityBox(entity.getLocation(), entity).isCollided(playerBox))
					return true;
			}
		}

		return false;
	}
	public boolean isCollidedWith(Material... materials) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.x, location.y, location.z)
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return isCollidedWith(playerBox, materials);
	}
}
