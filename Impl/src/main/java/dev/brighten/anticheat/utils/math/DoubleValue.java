package dev.brighten.anticheat.utils.math;

public final class DoubleValue {

    private double alpha;

    public DoubleValue() {
    }

    public DoubleValue(final double alpha) {
        this.alpha = alpha;
    }

    public double get() {
        return alpha;
    }

    public double set(final double beta) {
        return (alpha = beta);
    }

    public double add(final double beta) {
        return (this.alpha += beta);
    }

    public double subtract(final double beta) {
        return (this.alpha -= beta);
    }

    public double multiply(final double beta) {
        return (this.alpha *= beta);
    }

    public double divide(final double beta) {
        return (this.alpha /= beta);
    }
}