package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EntityLocation {
    public double newX, newY, newZ, x, y, z;
    public final int sentTick;
    public final List<KLocation> interpolatedLocations = new ArrayList<>();

    public void interpolateLocations() {
        int increment = 3;
        interpolatedLocations.clear();
        while(increment > 0) {
            double d0 = x + (newX - x) / increment;
            double d1 = y + (newY - y) / increment;
            double d2 = z + (newZ - z) / increment;

            increment--;

            this.x = d0;
            this.y = d1;
            this.z = d2;
            interpolatedLocations.add(new KLocation(x, y, z));
        }
    }
}