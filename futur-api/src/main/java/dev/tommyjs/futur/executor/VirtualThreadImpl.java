package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

class VirtualThreadImpl implements PromiseExecutor<Thread> {

    @Override
    public Thread run(@NotNull Runnable task) {
        return Thread.ofVirtual().start(task);
    }

    @Override
    public Thread run(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(unit.toMillis(delay));
            } catch (InterruptedException e) {
                return;
            }
            task.run();
        });
    }

    @Override
    public boolean cancel(Thread task) {
        if (task.isAlive()) {
            task.interrupt();
            return true;
        } else {
            return false;
        }
    }

}