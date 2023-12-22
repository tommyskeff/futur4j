package dev.tommyjs.test;

import dev.tommyjs.futur.promise.PooledPromiseFactory;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.scheduler.Scheduler;
import dev.tommyjs.futur.scheduler.SingleExecutorScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        Scheduler scheduler = new SingleExecutorScheduler(Executors.newScheduledThreadPool(4));
        Logger logger = LoggerFactory.getLogger(Test.class);
        PromiseFactory factory = new PooledPromiseFactory(scheduler, logger);

        Thread.sleep(2000);

        Promise.start(factory)
            .thenRunAsync(() -> {
                System.out.println("HI");
            })
            .thenApplyDelayedAsync(_v -> {
                return "ABC";
            }, 1L, TimeUnit.SECONDS)
            .thenConsumeSync(t -> {
                System.out.println(t);
            });
    }

}
