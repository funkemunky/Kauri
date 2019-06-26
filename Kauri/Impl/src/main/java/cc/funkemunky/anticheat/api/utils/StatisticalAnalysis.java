package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class StatisticalAnalysis implements Serializable {

    private final double[] objects;

    private int currentObject;
    private int windowAmount;

    private double variant;

    public StatisticalAnalysis(int size) {
        this.objects = new double[size];
        this.variant = size * 2.5;

        for (int i = 0, len = this.objects.length; i < len; i++) {
            this.objects[i] = size * 2.5 / size;
        }
    }

    public void addValue(double sum) {
        sum /= this.objects.length;

        this.variant -= this.objects[currentObject];
        this.variant += sum;

        this.objects[currentObject] = sum;

        this.currentObject = (currentObject + 1) % this.objects.length;
    }

    public double getStdDev(double required) {
        double stdDev = Math.sqrt(variant);

        if (stdDev < required) {
            if (++windowAmount > this.objects.length) {
                return stdDev;
            }
        } else {
            if (windowAmount > 0) {
                windowAmount = 0;
            }

            return required;
        }

        return Double.NaN;
    }

    public double getStdDev() {
        return Math.sqrt(variant);
    }
}

