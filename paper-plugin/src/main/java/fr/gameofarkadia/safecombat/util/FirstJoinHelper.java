package fr.gameofarkadia.safecombat.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FirstJoinHelper {

  public static @NotNull CompletableFuture<Boolean> isItFirstConnection(@NotNull Player player) {
    //TODO do me
    return CompletableFuture.completedFuture(false);
  }

}
