package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.BoundingBox;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RayTrace {

    //origin = start position
    //direction = direction in which the raytrace will go
    Vector origin, direction;

    public RayTrace(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    //general intersection detection
    public static boolean intersects(Vector position, Vector min, Vector max) {
        if (position.getX() < min.getX() || position.getX() > max.getX()) {
            return false;
        } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
            return false;
        } else return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ());
    }

    //get a point on the raytrace at X blocks away
    public Vector getPostion(double blocksAway) {
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }

    //checks if a position is on contained within the position
    public boolean isOnLine(Vector position) {
        double t = (position.getX() - origin.getX()) / direction.getX();
        return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
    }

    //get all postions on a raytrace
    public List<Vector> traverse(double blocksAway, double accuracy) {
        return traverse(0, blocksAway, accuracy, accuracy,0,0);
    }

    public List<Vector> traverse(double skip, double blocksAway, double accuracy, double extremeAccuracy, double extremeMinimum, double extremeMaximum) {
        List<Vector> positions = new ArrayList<>();
        for (double d = skip; d <= blocksAway; d += (d > extremeMinimum && d < extremeMaximum ? extremeAccuracy : accuracy)) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    public List<Vector> traverse(double blocksAway, double accuracy, double extremeAccuracy, double extremeMinimum) {
        return traverse(0, blocksAway, accuracy, extremeAccuracy, extremeMinimum, 15);
    }

    public List<Vector> traverse(double skip, double blocksAway, double accuracy) {
        return traverse(skip, blocksAway, accuracy, accuracy, 0,0);
    }

    public List<Block> getBlocks(World world, double blocksAway, double accuracy) {
        List<Block> blocks = new ArrayList<>();

        traverse(blocksAway, accuracy).stream().filter(vector -> vector.toLocation(world).getBlock().getType().isSolid()).forEach(vector -> blocks.add(vector.toLocation(world).getBlock()));
        return blocks;
    }

    //intersection detection for current raytrace with return
    public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        return positions.stream().filter(position -> intersects(position, min, max)).findFirst().orElse(null);
    }

    //intersection detection for current raytrace
    public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        return positions.stream().anyMatch(position -> intersects(position, min, max));
    }

    //bounding blockbox instead of vector
    public Vector positionOfIntersection(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        return positions.stream().filter(position -> intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum())).findFirst().orElse(null);
    }

    public Vector positionOfIntersection(BoundingBox boundingBox, double skip, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(skip, blocksAway, accuracy);
        return positions.stream().filter(position -> intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum())).findFirst().orElse(null);
    }

    //bounding blockbox instead of vector
    public boolean intersects(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        return positions.stream().anyMatch(position -> intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum()));
    }

    public boolean intersects(BoundingBox boundingBox, double skip, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        return positions.stream().anyMatch(position -> intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum()));
    }

    //debug / effects
    public void highlight(World world, double blocksAway, double accuracy) {
        traverse(blocksAway, accuracy).forEach(position -> world.playEffect(position.toLocation(world), (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? Effect.SMOKE : Effect.valueOf("COLOURED_DUST")), 0));
    }
}