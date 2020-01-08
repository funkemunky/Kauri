package dev.brighten.anticheat.utils;


import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.utils.handlers.PlayerSizeHandler;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

	public static int angularDistance(double alpha, double beta) {
		while (alpha < 0) alpha += 360;
		while (beta < 0) beta += 360;
		double phi = Math.abs(beta - alpha) % 360;
		return (int) (phi > 180 ? 360 - phi : phi);
	}

	public static Vector vector(double yaw, double pitch) {
		Vector vector = new Vector();
		vector.setY(-Math.sin(Math.toRadians(pitch)));
		double xz = Math.cos(Math.toRadians(pitch));
		vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
		vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
		return vector;
	}

	public static SimpleCollisionBox getMovementHitbox(Player player, double x, double y, double z) {
		return PlayerSizeHandler.instance.bounds(player, x, y, z);
	}

	public static SimpleCollisionBox getMovementHitbox(Player player) {
		return PlayerSizeHandler.instance.bounds(player);
	}

	public static SimpleCollisionBox getCombatHitbox(Player player, ProtocolVersion version) {
		return version.isBelow(ProtocolVersion.V1_9) ? PlayerSizeHandler.instance.bounds(player).expand(.1, 0, .1) : PlayerSizeHandler.instance.bounds(player);
	}

	private static Block getBlockAt(World world, int x, int y, int z) {
		return world.isChunkLoaded(x >> 4, z >> 4) ? world.getChunkAt(x >> 4, z >> 4).getBlock(x & 15, y, z & 15) : null;
	}


	public static List<Block> blockCollisions(List<Block> blocks, SimpleCollisionBox box) {
		return blocks.stream().filter(b -> BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(box)).collect(Collectors.toCollection(LinkedList::new));
	}

	public static List<Block> blockCollisions(List<Block> blocks, SimpleCollisionBox box, int material) {
		return blocks.stream().filter(b -> Materials.checkFlag(b.getType(), material)).filter(b -> BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(box)).collect(Collectors.toCollection(LinkedList::new));
	}

	public static <C extends CollisionBox> List<C> collisions(List<C> boxes, CollisionBox box) {
		return boxes.stream().filter(b -> b.isCollided(box)).map(b -> b).collect(Collectors.toCollection(LinkedList::new));
	}

	public static List<Block> getBlocksNearby(CollisionHandler handler, SimpleCollisionBox collisionBox) {
		try {
			return handler.getBlocks().stream().filter(b -> b.getType() != Material.AIR
					&& BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())
					.isCollided(collisionBox))
					.collect(Collectors.toList());
		} catch (NullPointerException e) {
			return new ArrayList<>();
		}
	}

	public static List<Block> getBlocksNearby2(World world, SimpleCollisionBox collisionBox, int mask) {
		int x1 = (int) Math.floor(collisionBox.xMin);
		int y1 = (int) Math.floor(collisionBox.yMin);
		int z1 = (int) Math.floor(collisionBox.zMin);
		int x2 = (int) Math.ceil(collisionBox.xMax);
		int y2 = (int) Math.ceil(collisionBox.yMax);
		int z2 = (int) Math.ceil(collisionBox.zMax);
		List<Block> blocks = new LinkedList<>();
		Block block;
		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++)
				for (int z = z1; z <= z2; z++)
					if ((block = getBlockAt(world, x, y, z)) != null
							&& block.getType()!=Material.AIR)
						if (Materials.checkFlag(block.getType(),mask))
							blocks.add(block);
		return blocks;
	}

	public static List<Block> getBlocksNearby(CollisionHandler handler, SimpleCollisionBox collisionBox, int mask) {
		return handler.getBlocks().stream().filter(b -> b.getType() != Material.AIR
				&& Materials.checkFlag(b.getType(), mask)
				&& BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())
				.isCollided(collisionBox))
				.collect(Collectors.toList());
	}

	public static List<Block> getBlocks(CollisionHandler handler, SimpleCollisionBox collisionBox) {
		return Helper.blockCollisions(getBlocksNearby(handler, collisionBox), collisionBox);
	}

	public static List<Block> getBlocks(CollisionHandler handler, SimpleCollisionBox collisionBox, int material) {
		return Helper.blockCollisions(getBlocksNearby(handler, collisionBox), collisionBox, material);
	}

	public static List<CollisionBox> toCollisions(List<Block> blocks) {
		return blocks.stream().map(b -> BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())).collect(Collectors.toCollection(LinkedList::new));
	}

	public static List<SimpleCollisionBox> toCollisionsDowncasted(List<Block> blocks) {
		List<SimpleCollisionBox> collisions = new LinkedList<>();
		blocks.forEach(b -> BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).downCast(collisions));
		return collisions;
	}

	public static CollisionBox toCollisions(Block b) {
		return BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion());
	}
}
