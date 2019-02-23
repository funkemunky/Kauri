package cc.funkemunky.anticheat.api.utils;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;

@Getter
public class PastLocation {
    private List<CustomLocation> previousLocations = Lists.newCopyOnWriteArrayList();

    public CustomLocation getPreviousLocation(long time) {
        return previousLocations.stream().min(Comparator.comparingLong(loc -> Math.abs(loc.getTimeStamp() - (System.currentTimeMillis() - time)))).orElse(previousLocations.get(previousLocations.size() - 1));
    }

    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        List<CustomLocation> locs = Lists.newArrayList();

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
