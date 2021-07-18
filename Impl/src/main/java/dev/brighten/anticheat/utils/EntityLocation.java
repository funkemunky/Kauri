package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathHelper;
import dev.brighten.anticheat.Kauri;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class EntityLocation {
    public final UUID uuid;
    public double newX, newY, newZ, x, y, z;
    public float newYaw, newPitch, yaw, pitch;
    public List<KLocation> interpolatedLocations = new ArrayList<>();

    public void interpolateLocations() {
        int increment = 3;
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
}