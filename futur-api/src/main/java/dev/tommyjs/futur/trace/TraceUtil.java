package dev.tommyjs.futur.trace;

import org.jetbrains.annotations.NotNull;

public class TraceUtil {

    public static ExecutorTrace getTrace(@NotNull Object function) {
        return new ExecutorTrace(function.getClass(), Thread.currentThread().getStackTrace());
    }

}
