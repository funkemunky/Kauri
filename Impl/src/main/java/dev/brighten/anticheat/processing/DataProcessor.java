package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.Tuple;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataProcessor {

    private final Deque<Tuple<Runnable, Long>> tasks = new LinkedBlockingDeque<>();
    private final ExecutorService processorTask = Executors.newSingleThreadExecutor();
    private AtomicBoolean running = new AtomicBoolean(true);

    public DataProcessor() {
        running.set(true);
        runProcessor();
    }

    private void runProcessor() {
        processorTask.submit(() -> {
            if(running.get()) {
                Tuple<Runnable, Long> task;
                while ((task = tasks.poll()) != null) {
                    task.one.run();
                }
                runProcessor();
            }
        });
    }

    public void shutdown() {
        tasks.clear();
        running.set(false);
        processorTask.shutdown();
    }

    public void runTask(Runnable runnable) {
        long timeStamp = System.currentTimeMillis();
        tasks.addLast(new Tuple<>(runnable, timeStamp));
        //processorTask.execute(runnable);
    }
}
