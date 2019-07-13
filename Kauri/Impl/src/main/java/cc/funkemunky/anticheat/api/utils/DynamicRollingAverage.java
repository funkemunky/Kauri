package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DynamicRollingAverage {
    private List<Double> values = new ArrayList<>();

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

    public List<Double> getCopy() {
        return new ArrayList<>(values);
    }

    public double getAverage() {
        return getCopy().stream().mapToDouble(i -> i).average().orElse(0.0);
    }
}
