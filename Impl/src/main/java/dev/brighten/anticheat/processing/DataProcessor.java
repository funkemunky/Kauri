package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.Tuple;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataProcessor {

    private final Deque<Tuple<Runnable, Long>> tasks = new LinkedList<>();
    private final ScheduledExecutorService processorTask = Executors.newSingleThreadScheduledExecutor();

    public DataProcessor() {
        runProcessor();
    }

    private void runProcessor() {
        processorTask.scheduleAtFixedRate(() -> {
            Tuple<Runnable, Long> task;
            while ((task = tasks.poll()) != null) {
                task.one.run();
            }
        }, 500L, 100L, TimeUnit.NANOSECONDS);
    }

    public void shutdown() {
        tasks.clear();
        processorTask.shutdownNow();
    }

    public void runTask(Runnable runnable) {
        long timeStamp = System.currentTimeMillis();
        tasks.addLast(new Tuple<>(runnable, timeStamp));
    }
}
