package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SkiddedUtils {

    /*public static double[] cursor(Player player, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());
        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double horizontalDistance = MathUtils.getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;
        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return new double[]{Math.abs(offsetX), Math.abs(offsetY)};
    }*/

    public static double[] cursor(Player player, double dividend, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX() / dividend);
        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double horizontalDistance = MathUtils.getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;
        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return new double[]{Math.abs(offsetX), Math.abs(offsetY)};
    }

    public static double getDistance3D(Location one, Location two) {
        double toReturn = 0.0D;
        double xSqr = (two.getX() - one.getX()) * (two.getX() - one.getX());
        double ySqr = (two.getY() - one.getY()) * (two.getY() - one.getY());
        double zSqr = (two.getZ() - one.getZ()) * (two.getZ() - one.getZ());
        double sqrt = Math.sqrt(xSqr + ySqr + zSqr);
        toReturn = Math.abs(sqrt);
        return toReturn;
    }

    public static Vector getRotation(Location one, Location two) {
        double dx = two.getX() - one.getX();
        double dy = two.getY() - one.getY();
        double dz = two.getZ() - one.getZ();
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, distanceXZ) * 180.0D / 3.141592653589793D);
        return new Vector(yaw, pitch, 0.0F);
    }

    public static double clamp180(double theta) {
        theta %= 360.0D;
        if (theta >= 180.0D) {
            theta -= 360.0D;
        }
        if (theta < -180.0D) {
            theta += 360.0D;
        }
        return theta;
    }

    public static double getAimbotoffset(Location playerLocLoc, double playerEyeHeight, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = playerLocLoc.add(0.0D, playerEyeHeight, 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());

        double horizontalDistance = MathUtils.getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;

        return offsetX;
    }

    public static double getAimbotoffset2(Location playerLocLoc, double playerEyeHeight, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = playerLocLoc.add(0.0D, playerEyeHeight, 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return offsetY;
    }

    public static double[] getOffsetsOffCursor(Player player, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());
        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double horizontalDistance = MathUtils.getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;
        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return new double[]{Math.abs(offsetX), Math.abs(offsetY)};
    }

    public static double getOffsetOffCursor(Player player, LivingEntity entity) {
        double offset = 0.0D;
        double[] offsets = getOffsetsOffCursor(player, entity);

        offset += offsets[0];
        offset += offsets[1];

        return offset;
    }
}
