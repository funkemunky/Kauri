package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PastLocation {
    private final Deque<KLocation> previousLocations = new LinkedList<>();

    public KLocation getPreviousLocation(long time) {
        synchronized (previousLocations) {
            return (this.previousLocations.stream()
                    .min(Comparator.comparing(loc -> Math.abs(time - loc.timeStamp)))
                    .orElse(this.previousLocations.getFirst()));
        }
    }

    public List<KLocation> getEstimatedLocation(long time, long ping, long delta) {
        synchronized (previousLocations) {
            return this.previousLocations
                    .stream()
                    .filter(loc -> time - loc.timeStamp > 0 && Math.abs(time - loc.timeStamp - ping) < delta)
                    .collect(Collectors.toList());
        }
    }

    public List<KLocation> getEstimatedLocation(long time, long ping) {
        synchronized (previousLocations) {
            return this.previousLocations.stream()
                    .filter(loc -> time - loc.timeStamp > 0
                            && time - loc.timeStamp <= ping + (ping < 50 ? 100 : 50))
                    .collect(Collectors.toList());
        }
    }

    public List<KLocation> getPreviousRange(long delta) {
       synchronized (previousLocations) {
           long stamp = System.currentTimeMillis();

           return this.previousLocations.stream()
                   .filter(loc -> stamp - loc.timeStamp < delta)
                   .collect(Collectors.toList());
       }
    }

    public void addLocation(Location location) {
        synchronized (previousLocations) {
            if(previousLocations.size() >= 20)
                previousLocations.removeFirst();

            previousLocations.add(new KLocation(location));
        }
    }

    public Deque<KLocation> getPreviousLocations() {
        return previousLocations;
    }

    public KLocation getLast() {
        if(getPreviousLocations().size() == 0) return null;
        return getPreviousLocations().getLast();
    }

    public KLocation getFirst() {
        if(getPreviousLocations().size() == 0) return null;
        return getPreviousLocations().getFirst();
    }

    public void addLocation(KLocation location) {
        getPreviousLocations().add(location.clone());
    }
}