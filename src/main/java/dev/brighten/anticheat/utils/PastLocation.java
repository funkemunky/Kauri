package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PastLocation {
    public List<KLocation> previousLocations = new CopyOnWriteArrayList<>();

    public KLocation getPreviousLocation(long time) {
        return (this.previousLocations.stream()
                .min(Comparator.comparingLong((loc) -> MathUtils.getDelta((System.currentTimeMillis() - loc.timeStamp), time)))
                .orElse(this.previousLocations.get(0)));
    }

    public List<KLocation> getEstimatedLocation(long time, long delta) {
        long currentTimestamp = System.currentTimeMillis();
        return this.previousLocations.stream()
                .filter(loc -> MathUtils.getDelta(
                        MathUtils.millisToTicks(currentTimestamp - loc.timeStamp), MathUtils.millisToTicks(time))
                        <= MathUtils.millisToTicks(delta))
                .collect(Collectors.toList());
    }

    public void addLocation(Location location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(new KLocation(location));
    }

    public void addLocation(KLocation location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(location.clone());
    }
}