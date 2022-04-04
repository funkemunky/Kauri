package dev.brighten.anticheat.processing.thread;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Getter
@Setter
public class PlayerThread {
    private int count;
    private final ExecutorService thread;

    public void addCount() {
        count++;
    }

    public void subtractCount() {
        count--;
    }

    public void runTask(Runnable runnable) {
        thread.execute(runnable);
    }
}
