package dev.tommyjs.futur.trace;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExecutorTrace {

    private final @NotNull Class<?> clazz;
    private final @NotNull StackTraceElement[] trace;

    public ExecutorTrace(@NotNull Class<?> clazz, @NotNull StackTraceElement[] trace) {
        this.clazz = clazz;
        this.trace = trace;
    }

    public @NotNull Class<?> getClazz() {
        return clazz;
    }

    public @NotNull StackTraceElement[] getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return Arrays.stream(trace).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
    }

}
