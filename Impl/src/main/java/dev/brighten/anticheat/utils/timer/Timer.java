package dev.brighten.anticheat.utils.timer;

public interface Timer {

    boolean isPassed(long stamp);

    boolean isPassed();

    boolean isNotPassed(long stamp);

    boolean isNotPassed();

    boolean isReset();

    int getResetStreak();

    long getPassed();

    long getCurrent();

    void reset();

}
