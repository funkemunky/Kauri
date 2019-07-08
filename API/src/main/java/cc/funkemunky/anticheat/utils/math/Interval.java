package cc.funkemunky.anticheat.utils.math;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class Interval {

    private int x, max;
    private Deque<Integer> valList = new LinkedList<>();

    public Interval(int x, int max) {
        this.x = x;
        this.max = max;

        valList.add(x);
        valList.stream().filter(val -> val == 0);
    }

    public Interval(Deque x, int max) {
        this.valList = x;
        this.max = max;
    }

    public double average() {
        return valList.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
    }

    public double frequency(double freq) {
        return Collections.frequency(valList, freq);
    }

    public long distinct() {
        return valList.stream().distinct().count();
    }

    public double max() {
        return valList.stream().mapToDouble(Integer::doubleValue).max().orElse(0.0);
    }

    public double min() {
        return valList.stream().mapToDouble(Integer::doubleValue).min().orElse(0.0);
    }

    public void add(int x) {
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
}