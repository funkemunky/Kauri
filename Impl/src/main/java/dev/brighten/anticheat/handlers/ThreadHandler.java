package dev.brighten.anticheat.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadHandler {
    public List<ExecutorService> threads = new ArrayList<>();
    public int players;

    public ThreadHandler() {
        threads.add(Executors.newSingleThreadExecutor());
    }

    public ExecutorService newThread() {
        if(++players % 10 == 0) {
            threads.add(Executors.newSingleThreadExecutor());
        }
        return threads.get(threads.size() - 1);
    }
}
