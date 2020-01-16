package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.Tuple;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataProcessor {

    private final Deque<Tuple<Runnable, Long>> tasks = new LinkedList<>();
    private final ExecutorService processorTask = Executors.newSingleThreadExecutor();

    public DataProcessor() {
        runProcessor();
    }

    private void runProcessor() {
        processorTask.execute(() -> {
            while(true) {
                Tuple<Runnable, Long> task;
                while ((task = tasks.poll()) != null) {
                    task.one.run();
                }
            }
        });
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
