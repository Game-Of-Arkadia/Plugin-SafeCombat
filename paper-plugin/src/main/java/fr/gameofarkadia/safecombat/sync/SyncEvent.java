package fr.gameofarkadia.safecombat.sync;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
public enum SyncEvent {

  /**
   * Player disconnected. <br/>
   * Params: {@code player_uuid, server_id, disconnect_ts}
   */
  PLAYER_DISCONNECTED(UUID.class, String.class, Long.class),

  PLAYER_RECONNECT_BEFORE_PUNISH(UUID.class, String.class),

  /**
   * Player punished.<br/>
   * Params : {@code player_uuid}
   */
  PLAYER_RECONNECT_PUNISHED(UUID.class),

  /**
   * Protection-state changed on a player.<br/>
   * Params : {@code player_uuid}
   */
  PLAYER_PROTECTION_REFRESH(UUID.class),
  ;

  private final List<Class<?>> parameters;

  SyncEvent(@NotNull Class<?> @NotNull ... parameters) {
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

  /**
   * Write data to an output buffer.
   * @param out output buffer.
   * @param data data to write.
   */
  void write(@NotNull ByteArrayDataOutput out, @NotNull Object @NotNull [] data) {
    out.writeUTF(this.name());
    for(Object o : data) {
      switch (o) {
        case UUID uuid -> out.writeUTF(uuid.toString());
        case String s -> out.writeUTF(s);
        case Long l -> out.writeLong(l);
        default -> throw new IllegalArgumentException("Unsupported data type for event " + this + ": " + o.getClass().getSimpleName());
      }
    }
  }
}
