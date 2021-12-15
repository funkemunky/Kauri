package dev.brighten.anticheat.check.impl.premium.util;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EntityLocation {
    public final Entity entity;
    public double newX, newY, newZ, x, y, z;
    public float newYaw, newPitch, yaw, pitch;
    public int increment = 0;
    public boolean sentTeleport = false;
    public KLocation oldLocation, location;
    public List<KLocation> interpolatedLocations = new EvictingList<>(8);

    public void interpolateLocations() {
        increment = 3;
        interpolatedLocations.clear();
        while(increment > 0) {
            double d0 = x + (newX - x) / increment;
            double d1 = y + (newY - y) / increment;
            double d2 = z + (newZ - z) / increment;
            double d3 = MathHelper.wrapAngleTo180_float(newYaw - yaw);

            yaw = (float) ((double) yaw + d3 / (double) increment);
            pitch = (float) ((double) pitch + (newPitch - (double) pitch) / (double) increment);

            increment--;

            this.x = d0;
            this.y = d1;
            this.z = d2;
            interpolatedLocations.add(new KLocation(x, y, z, yaw, pitch, Kauri.INSTANCE.keepaliveProcessor.tick));
        }
    }

    public List<KLocation> getInterpolatedLocations() {
        int increment = 3;
        oldLocation = new KLocation(x, y, z, yaw, pitch);
        double x = this.x, y = this.y, z = this.z, newX = this.newX, newY = this.newY, newZ = this.newZ;
        float yaw = this.yaw, pitch = this.pitch, newYaw = this.newYaw, newPitch = this.newPitch;
        List<KLocation> locations = new ArrayList<>();
        while(increment > 0) {
            double d0 = x + (newX - x) / increment;
            double d1 = y + (newY - y) / increment;
            double d2 = z + (newZ - z) / increment;
            double d3 = MathHelper.wrapAngleTo180_float(newYaw - yaw);

            yaw = (float) ((double) yaw + d3 / (double) increment);
            pitch = (float) ((double) pitch + (newPitch - (double) pitch) / (double) increment);

            increment--;

            x = d0;
            y = d1;
            z = d2;
            locations.add(new KLocation(x, y, z, yaw, pitch, Kauri.INSTANCE.keepaliveProcessor.tick));
        }

        location = new KLocation(x, y, z, yaw, pitch);

        return locations;
    }

    public void interpolateLocation() {
        oldLocation = new KLocation(x, y, z, yaw, pitch);
        if(increment > 0) {
            double d0 = x + (newX - x) / increment;
            double d1 = y + (newY - y) / increment;
            double d2 = z + (newZ - z) / increment;
            double d3 = MathHelper.wrapAngleTo180_float(newYaw - yaw);

            yaw = (float) ((double) yaw + d3 / (double) increment);
            pitch = (float) ((double) pitch + (newPitch - (double) pitch) / (double) increment);

            increment--;

            this.x = d0;
            this.y = d1;
            this.z = d2;
        }
    }

    public EntityLocation clone() {
        final EntityLocation loc = new EntityLocation(entity);

        loc.x = x;
        loc.y = y;
        loc.z = z;
        loc.yaw = yaw;
        loc.pitch = pitch;
        loc.increment = increment;
        loc.newX = newX;
        loc.newY = newY;
        loc.newZ = newZ;
        loc.newYaw = newYaw;
        loc.newPitch = newPitch;
        loc.sentTeleport = sentTeleport;
        loc.interpolatedLocations.addAll(interpolatedLocations);

        return loc;
    }
}