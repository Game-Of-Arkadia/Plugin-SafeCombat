package fr.gameofarkadia.safecombat.sync;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
public enum SyncCommand {

  /**
   * New wanted request. <br/>
   * Params: {@code player_uuid, server_id, disconnect_ts}
   */
  WANTED_NEW(UUID.class, String.class, Long.class),

  /**
   * Clear the wanted status. <br/>
   * Params: {@code player_uuid}
   */
  WANTED_CLEAR(UUID.class),

  /**
   * Player is no longer protected.<br/>
   * Params: {@code player_uuid}
   */
  REMOVED_PROTECTION(UUID.class),

  ;

  private final List<Class<?>> parameters;

  SyncCommand(@NotNull Class<?> @NotNull ... parameters) {
    this.parameters = List.of(parameters);
  }

  /**
   * Check passed arguments are valid.
   * @param data data to check.
   */
  public void checkValid(@NotNull Object @NotNull ... data) {
    if(data.length != parameters.size()) {
      throw new IllegalArgumentException("Invalid data length for event " + this.name() + ". Expected " + parameters.size() + ", got " + data.length);
    }
    for(int i = 0; i < data.length; i++) {
      if(!parameters.get(i).isInstance(data[i])) {
        throw new IllegalArgumentException("Invalid data type for event " + this.name() + " at index " + i + ". Expected " + parameters.get(i).getSimpleName() + ", got " + data[i].getClass().getSimpleName());
      }
    }
  }
}
