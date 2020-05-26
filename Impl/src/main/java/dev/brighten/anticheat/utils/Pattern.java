package dev.brighten.anticheat.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Pattern {
    private int amount = 0;
    private int low = 0;
    private int high = 0;
    private List<Integer> allHighs = new ArrayList<Integer>();
    private List<Integer> allLows = new ArrayList<Integer>();
    private List<Integer> patternHigh = new ArrayList<Integer>();
    private List<Integer> patternLow = new ArrayList<Integer>();

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getLow() {
        return this.low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return this.high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public void addHighs(int number) {
        this.allHighs.add(number);
    }

    public void addLows(int number) {
        this.allLows.add(number);
    }

    public static int getOscillation(List<Integer> numbers) {
        int highest = -1;
        int lowest = -1;
        Iterator<Integer> iterator = numbers.iterator();
        while (iterator.hasNext()) {
            int number = iterator.next();
            if (highest == -1) {
                highest = number;
            }
            if (lowest == -1) {
                lowest = number;
            }
            if (number > highest) {
                highest = number;
            }
            if (number >= lowest) continue;
            lowest = number;
        }
        return highest - lowest;
    }

    public List<Integer> getAllHighs() {
        return this.allHighs;
    }

    public List<Integer> getAllLows() {
        return this.allLows;
    }

    public void addPatternHigh(int number) {
        this.patternHigh.add(number);
    }

    public void addPatternLow(int number) {
        this.patternLow.add(number);
    }

    public List<Integer> getPatternHigh() {
        return this.patternHigh;
    }

    public List<Integer> getPatternLow() {
        return this.patternLow;
    }
}