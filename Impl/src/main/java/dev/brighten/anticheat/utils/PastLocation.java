package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PastLocation {
    public List<KLocation> previousLocations = new CopyOnWriteArrayList<>();

    public KLocation getPreviousLocation(long time) {
        long timeStamp = System.currentTimeMillis() - time;
        return (this.previousLocations.stream()
                .min(Comparator.comparing((loc) -> MathUtils.getDelta(timeStamp, loc.timeStamp)))
                .orElse(this.previousLocations.get(0)));
    }

    public List<KLocation> getEstimatedLocation(long time, long delta) {
        long prevTimeStamp = System.currentTimeMillis() - time;
        return this.previousLocations
                .stream()
                .filter(loc -> MathUtils.getDelta(prevTimeStamp, loc.timeStamp) < delta)
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