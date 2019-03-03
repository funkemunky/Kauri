package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PastLocation {
    private List<CustomLocation> previousLocations = new CopyOnWriteArrayList<>();

    public CustomLocation getPreviousLocation(long time) {
        return previousLocations.stream().min(Comparator.comparingLong(loc -> Math.abs(loc.getTimeStamp() - (System.currentTimeMillis() - time)))).orElse(previousLocations.get(previousLocations.size() - 1));
    }

    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        List<CustomLocation> locs = new ArrayList<>();

        previousLocations.stream()
                .sorted(Comparator.comparingLong(loc -> Math.abs(loc.getTimeStamp() - (System.currentTimeMillis() - time))))
                .filter(loc -> Math.abs(loc.getTimeStamp() - (System.currentTimeMillis() - time)) < delta)
                .forEach(locs::add);
        return locs;
    }

    public void addLocation(Location location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(new CustomLocation(location));
    }

    public void addLocation(CustomLocation location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(location);
    }
}
