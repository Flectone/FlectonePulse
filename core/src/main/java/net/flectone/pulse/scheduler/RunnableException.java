package net.flectone.pulse.scheduler;

@FunctionalInterface
public interface RunnableException {
    void run() throws Exception;
}
