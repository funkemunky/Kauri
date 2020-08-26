package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PastLocation {
    public List<KLocation> previousLocations = new CopyOnWriteArrayList<>();

    public KLocation getPreviousLocation(long time) {
        return (this.previousLocations.stream()
                .min(Comparator.comparing(loc -> Math.abs(time - loc.timeStamp)))
                .orElse(this.previousLocations.get(0)));
    }

    public List<KLocation> getEstimatedLocation(int ping, int delta) {
        int index = Math.max(0, previousLocations.size() - ping - 1);

        if(previousLocations.size() < 15) return new ArrayList<>();

        return new ArrayList<>(previousLocations).subList(Math.max(0, index - delta),
                Math.min(previousLocations.size() - 1, index + delta));
    }

    public List<KLocation> getPreviousRange(int ping) {
        List<KLocation> kloc = new ArrayList<>();

        for(int i = ping ; i >= 0 ; i--) {
            kloc.add(previousLocations.get(i));
        }

        return kloc;
    }

    public void addLocation(Location location) {
        if (previousLocations.size() >= 40) {
            previousLocations.remove(0);
        }

        previousLocations.add(new KLocation(location));
    }

    public KLocation getLast() {
        if(previousLocations.size() == 0) return null;
        return previousLocations.get(previousLocations.size() - 1);
    }

    public KLocation getFirst() {
        if(previousLocations.size() == 0) return null;
        return previousLocations.get(0);
    }

    public void addLocation(KLocation location) {
        if (previousLocations.size() >= 40) {
            previousLocations.remove(0);
        }

        previousLocations.add(location.clone());
    }
}