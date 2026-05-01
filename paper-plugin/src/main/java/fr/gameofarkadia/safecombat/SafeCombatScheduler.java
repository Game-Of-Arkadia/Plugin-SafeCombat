package fr.gameofarkadia.safecombat;

import lombok.AccessLevel;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Shortcut to execute code in another thread.
 */
public final class SafeCombatScheduler {

    @Setter(AccessLevel.MODULE)
    private static JavaPlugin plugin;

    /**
     * Execute a block of code in the main thread.
     * @param runnable code to execute.
     */
    public static void run(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void event(@NotNull Event event) {
        run(() -> Bukkit.getPluginManager().callEvent(event));
    }

    /**
     * Execute a block of code in the main thread, later.
     * @param runnable code to execute.
     * @param ticks duration, in ticks, to wait before execution.
     * @return the scheduled task.
     */
    public static @NotNull BukkitTask runLater(@NotNull Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    /**
     * Execute a block of code in an asynchronous thread, later.
     * @param runnable code to execute.
     * @param ticks duration, in ticks, to wait before execution.
     */
    public static @NotNull BukkitTask runLaterAsync(@NotNull Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks);
    }

    /**
     * Execute a block of code in the main thread, repeatedly.
     * @param runnable code to execute.
     * @param period duration, in ticks, to wait between iterations.
     */
    public static @NotNull BukkitTask runTimer(@NotNull Runnable runnable, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, period, period);
    }

    /**
     * Execute a block of code asynchronously, repeatedly.
     * @param runnable code to execute.
     * @param period duration, in ticks, to wait between iterations.
     */
    public static @NotNull BukkitTask runTimerAsync(@NotNull Runnable runnable, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, period, period);
    }

    /**
     * Execute a block of code in an asynchronous thread.
     * @param runnable code to execute.
     */
    public static void runAsync(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Execute a block of code in an asynchronous thread, returning a future.
     * @param runnable code to execute.
     * @return a future completed when the code is done.
     */
    public static @NotNull CompletableFuture<Void> execAsync(@NotNull Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception error) {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    /**
     * Execute a block of code in an asynchronous thread, returning a future.
     * @param supplier code to execute.
     * @return a future completed when the code is done.
     * @param <T> type of the result
     */
    public static <T> @NotNull CompletableFuture<T> execAsync(@NotNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception error) {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

}
