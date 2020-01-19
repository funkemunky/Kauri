package dev.brighten.anticheat.processing;
import java.util.concurrent.*;

public class DataProcessor {

    private final ExecutorService processorTask = Executors.newSingleThreadExecutor();

    public void runTask(Runnable runnable) {
        //long timeStamp = System.currentTimeMillis();
        //tasks.addLast(new Tuple<>(runnable, timeStamp));
        processorTask.submit(runnable);
    }
}
