package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class Interval {

    private long x, max;
    @Getter
    private Deque<Long> valList = new LinkedList<>();

    public Interval(long x, long max) {
        this.x = x;
        this.max = max;

        valList.add(x);
        valList.stream().filter(val -> val == 0);
    }

    public Interval(Deque x, long max) {
        this.valList = x;
        this.max = max;
    }

    public double average() {
        return getCopy().stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
    }

    public double frequency(double freq) {
        return Collections.frequency(getCopy(), freq);
    }

    public long distinct() {
        return getCopy().stream().distinct().count();
    }

    public double std() {
        double average = average();
        return Math.sqrt(getCopy().stream().mapToDouble(val -> Math.pow(val - average, 2)).average().orElse(0));
    }

    public double max() {
        return getCopy().stream().mapToDouble(Long::doubleValue).max().orElse(0.0);
    }

    public double min() {
        return getCopy().stream().mapToDouble(Long::doubleValue).min().orElse(0.0);
    }

    public void add(long x) {
        valList.add(x);

        if (valList.size() > max) {
            valList.remove(valList.size() - 1);
        }
    }

    public void clear() {
        valList.clear();
    }

    public void clearIfMax() {
        if (valList.size() == max) {
            this.clear();
        }
    }

    public Deque<Long> getCopy() {
        return new LinkedList<>(valList);
    }
}