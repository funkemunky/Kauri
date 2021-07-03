package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PastLocation {
    private final EvictingList<KLocation> previousLocations = new EvictingList<>(20);

    public KLocation getPreviousLocation(long time) {
        return (this.getPreviousLocations().stream()
                .min(Comparator.comparing(loc -> Math.abs(time - loc.timeStamp)))
                .orElse(this.getPreviousLocations().getFirst()));
    }

    public List<KLocation> getEstimatedLocation(long time, long ping, long delta) {
        return this.getPreviousLocations()
                .stream()
                .filter(loc -> time - loc.timeStamp > 0 && Math.abs(time - loc.timeStamp - ping) < delta)
                .collect(Collectors.toList());
    }

    public List<KLocation> getEstimatedLocation(long time, long ping) {
        return this.getPreviousLocations().stream()
                .filter(loc -> time - loc.timeStamp > 0
                        && time - loc.timeStamp <= ping + (ping < 50 ? 100 : 50))
                .collect(Collectors.toList());
    }

    public List<KLocation> getPreviousRange(long delta) {
        long stamp = System.currentTimeMillis();

        return this.getPreviousLocations().stream()
                .filter(loc -> stamp - loc.timeStamp < delta)
                .collect(Collectors.toList());
    }

    public void addLocation(Location location) {
        getPreviousLocations().add(new KLocation(location));
    }

    public synchronized EvictingList<KLocation> getPreviousLocations() {
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