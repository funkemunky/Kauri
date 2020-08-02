package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import dev.brighten.anticheat.Kauri;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PastLocation {
    public List<KLocation> previousLocations = new CopyOnWriteArrayList<>();

    public KLocation getPreviousLocation(long time) {
        return (this.previousLocations.stream()
                .min(Comparator.comparing(loc -> Math.abs(time - loc.timeStamp)))
                .orElse(this.previousLocations.get(0)));
    }

    public List<KLocation> getEstimatedLocation(int ping, long delta) {
        List<KLocation> locs = new ArrayList<>();

        int current = Kauri.INSTANCE.keepaliveProcessor.tick;

        for (KLocation loc : previousLocations) {
            if(Math.abs(current - (int)loc.timeStamp - ping) <= delta) {
                locs.add(loc);
            }
        }

        return locs;
    }

    public List<KLocation> getPreviousRange(int ping) {
        List<KLocation> kloc = new ArrayList<>();

        for(int i = ping ; i >= 0 ; i--) {
            kloc.add(previousLocations.get(i));
        }

        return kloc;
    }

    public void addLocation(Location location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }


        KLocation loc = new KLocation(location);

        loc.timeStamp = Kauri.INSTANCE.keepaliveProcessor.tick;

        previousLocations.add(loc);
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
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        KLocation loc = location.clone();

        loc.timeStamp = Kauri.INSTANCE.keepaliveProcessor.tick;

        previousLocations.add(loc);
    }
}