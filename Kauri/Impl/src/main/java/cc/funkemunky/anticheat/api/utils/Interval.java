package cc.funkemunky.anticheat.api.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

public class Interval<T> extends LinkedList<T> {

    private long x, max;

    public Interval(long x, long max) {
        this.x = x;
        this.max = max;
    }

    public Interval(Interval in) {
        this.x = in.x;
        this.max = in.max;
    }

    public double average() {
        return streamNumber().mapToDouble(val -> (double)val).average().orElse(0.0);
    }

    public double frequency(double freq) {
        return Collections.frequency(this, freq);
    }

    public long distinct() {
        return streamNumber().distinct().count();
    }

    public double std() {
        double average = average();
        return Math.sqrt(streamNumber().mapToDouble(val -> Math.pow((double)val - average, 2)).average().orElse(0));
    }

    public double max() {
        return streamNumber().mapToDouble(val -> (double)val).max().orElse(0.0);
    }

    public double min() {
        return streamNumber().mapToDouble(val -> (double)val).min().orElse(0.0);
    }

    public boolean add(T x) {
        if (size() > max) {
            remove(size() - 1);
        }
        return super.add(x);
    }

    public void clearIfMax() {
        if (size() == max) {
            this.clear();
        }
    }

    public Stream<Number> streamNumber() {
        return (Stream<Number>)super.stream();
    }
}