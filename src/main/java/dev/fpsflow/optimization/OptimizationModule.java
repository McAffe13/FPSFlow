package dev.fpsflow.optimization;

public interface OptimizationModule {

    String getId();

    void initialize();

    void shutdown();

    boolean isEnabled();

    /** Called once per game tick on the main thread. */
    default void onTick() {}
}
