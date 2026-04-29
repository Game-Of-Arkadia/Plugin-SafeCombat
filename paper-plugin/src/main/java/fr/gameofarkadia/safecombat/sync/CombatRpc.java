package fr.gameofarkadia.safecombat.sync;

import fr.arkadia.highlands.Main;
import fr.arkadia.pterodactyl.api.ipc.IpcNotification;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * IPC notification for data synchronization across servers.
 * @param command SYNC command to use.
 * @param values arguments for the command.
 */
public record CombatRpc(
    @NotNull SyncCommand command,
    @NotNull Object... values
) implements IpcNotification {

  /**
   * Get a typed value from the values array.
   * @param index index of the value.
   * @param clazz expected class of the value.
   * @return the value casted to the expected type.
   * @param <T> type of the value.
   */
  public <T> T get(int index, @NotNull Class<T> clazz) {
    try {
      return clazz.cast(values[index]);
    } catch (Throwable err) {
      Main.logger().error("Could not get value at index {} with class {}.", index, clazz, err);
      throw new IllegalArgumentException("Invalid value at index " + index + ": expected " + clazz.getName() + ", got " + (index < values.length ? values[index].getClass().getName() : "none"), err);
    }
  }

  @Override
  public @NonNull String toString() {
    return "SyncNotif<"+command+"; "+ Arrays.toString(values) +">";
  }
}
