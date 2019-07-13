package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

public class DynamicRollingAverage {
    private Deque<Double> values = new ArrayDeque<>();

    private int size;

    @Getter
    private boolean reachedSize = false;

    public DynamicRollingAverage(int size) {
        this.size = size;
    }

    public void add(double value) {
        if (values.size() >= size) {
            reachedSize = true;

            this.values.remove(values.size() - 1);
        }

        this.values.add(value);
    }

    public void clearValues() {
        values.clear();

        reachedSize = false;
    }

    public Deque<Double> getCopy() {
        return new ArrayDeque<>(values);
    }

    public double getAverage() {
        return getCopy().stream().mapToDouble(i -> i).average().orElse(0.0);
    }
}
