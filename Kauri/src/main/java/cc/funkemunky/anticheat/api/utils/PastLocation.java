package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.utils.MathUtils;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class PastLocation {
    private List<CustomLocation> previousLocations = new CopyOnWriteArrayList<>();

    public CustomLocation getPreviousLocation(long time) {
        return (this.previousLocations.stream().min(Comparator.comparingLong((loc) -> MathUtils.getDelta((System.currentTimeMillis() - loc.getTimeStamp()), time))).orElse(this.previousLocations.get(0)));
    }

    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        new ArrayList();
        long currentTimestamp = System.currentTimeMillis();
        return this.previousLocations.stream().filter(loc -> MathUtils.getDelta((currentTimestamp - loc.getTimeStamp()), time) <= delta).collect(Collectors.toList());
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
