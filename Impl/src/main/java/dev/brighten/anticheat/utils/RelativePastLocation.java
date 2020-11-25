package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflections.impl.CraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RelativePastLocation {

    @Getter
    private List<RelativeLocation> pastLocations = new EvictingList<>(20);

    public Optional<RelativeLocation> getLocation(int tick) {
        return pastLocations.stream().filter(pl ->  Math.abs(tick - pl.tick) <= 3 && tick > pl.tick)
                .min(Comparator.comparing(pl -> Math.abs(tick - pl.tick)));
    }

    public void addLocation(WrappedOutRelativePosition packet) {
        double x, y, z;
        Entity entity = packet.getPlayer().getWorld().getEntities().stream()
                .filter(ent -> ent.getEntityId() == packet.getId()).findFirst().orElse(null);

        if(entity == null) return;

        int[] loc = ReflectionUtil
                .getTrackerLoc(CraftReflection.getVanillaWorld(entity.getWorld()), entity.getEntityId());

        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9)) {
            int rx = packet.getX(), ry = packet.getY(), rz = packet.getZ();

            x = rx / 4096.;
            y = ry / 4096.;
            z = rz / 4096.;
        } else {
            byte rx = packet.getX(), ry = packet.getY(), rz = packet.getZ();
            x = rx / 32.;
            y = ry / 32.;
            z = rz / 32.;
        }

        RelativeLocation location = new RelativeLocation(x, y, z,
                new Location(entity.getWorld(), loc[0] / 32., loc[1] / 32., loc[2] / 32.),
                3, MiscUtils.currentTick());

        pastLocations.add(location);
    }

    @AllArgsConstructor
    public static class RelativeLocation {
        public double x, y, z;
        public Location location;
        public int increments, tick;

        public Vector getPosition(int level) {
            int current = increments;

            level = Math.min(3, level);

            Vector currentLoc = location.toVector();
            double mx = currentLoc.getX() + x, my = currentLoc.getY() + y, mz = currentLoc.getZ() + z;
            while(current > level) {
                double d0, d1, d2;

                d0 = currentLoc.getX() + (mx - currentLoc.getX()) / increments;
                d1 = currentLoc.getY() + (my - currentLoc.getY()) / increments;
                d2 = currentLoc.getZ() + (mz - currentLoc.getZ()) / increments;

                currentLoc.setX(d0);
                currentLoc.setY(d1);
                currentLoc.setZ(d2);

                current--;
            }

            return currentLoc;
        }
    }
}
