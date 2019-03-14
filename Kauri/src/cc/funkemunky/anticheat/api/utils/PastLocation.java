package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;
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
        return previousLocations.stream().min(Comparator.comparingLong(loc -> Kauri.getInstance().getCurrentTicks() - MiscUtils.millisToTicks(time) - loc.getTicks())).orElse(previousLocations.get(previousLocations.size() - 1));
    }

    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        List<CustomLocation> locs = new ArrayList<>();

        int currentTicks = Kauri.getInstance().getCurrentTicks();

        previousLocations.stream()
                .sorted(Comparator.comparingLong(loc -> currentTicks - MiscUtils.millisToTicks(time) - loc.getTicks()))
                .filter(loc -> currentTicks - MiscUtils.millisToTicks(time) - loc.getTicks() < delta)
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
