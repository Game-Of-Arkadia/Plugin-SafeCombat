# SafeCombat

Plugin Minecraft Paper pour gérer le PvP de façon sécurisée :
- statut de combat (combat-tag),
- protection temporaire des joueurs,
- gestion des déconnexions en combat (wanted/punition),
- synchronisation inter-serveurs.

Le dépôt contient deux modules Maven :
- `api` (`safecombat-api`) : API Java pour les autres plugins.
- `paper-plugin` (`safecombat`) : implémentation Paper.

## Prérequis

- Java 21
- Maven 3.9+
- Serveur Paper `1.21.x`
- Dépendances serveur (via `plugin.yml`) :
  - `ArkadiaLib`
  - `HuskSync`
  - `PterodactylAPI`

## Build

Depuis la racine du repository :

```bash
mvn clean package
```

Artifacts générés :
- API : `api/target/safecombat-api-2.0.jar`
- Plugin Paper : `paper-plugin/target/safecombat-2.0.0-SNAPSHOT.jar`

## Installation (serveur Paper)

1. Copier `paper-plugin/target/safecombat-2.0.0-SNAPSHOT.jar` dans `plugins/`.
2. Vérifier que `ArkadiaLib`, `HuskSync` et `PterodactylAPI` sont déjà installés.
3. Démarrer le serveur une première fois pour générer/charger `plugins/SafeCombat/config.yml`.
4. Ajuster la configuration puis redémarrer (ou recharger via commande admin).

## Configuration

Fichier : `paper-plugin/src/main/resources/config.yml`

### Vue d'ensemble

```yml
prefix: "&b&l[&9SafeCombat&b&l]"

database:
  name: "plugin_safecombat"

pvp:
  durations:
    fight: 30s
    newbie-protection: 48h
    respawn-protection: 20s
    server-join-protection: 5s
    allowed-disconnection: 1m

punishment:
  lose-money: true
  ban:
    enabled: true
    reason: "Déconnecté en combat."
    duration: 1d
    main-server: arkadia

banned-commands:
  - "home"
  - "spawn"
  - "tp"
```

### Détails des clés

- `prefix` : préfixe des messages plugin (codes couleurs `&` supportés).
- `database.name` : nom de la base utilisée pour les données SafeCombat.
- `pvp.durations.fight` : durée d'un état de combat.
- `pvp.durations.newbie-protection` : protection des nouveaux joueurs.
- `pvp.durations.respawn-protection` : protection après respawn.
- `pvp.durations.server-join-protection` : protection temporaire à la connexion.
- `pvp.durations.allowed-disconnection` : délai max hors-ligne avant punition d'une déco en combat.
- `punishment.lose-money` : retire de l'argent lors de la punition si activé.
- `punishment.ban.enabled` : active/désactive le ban automatique.
- `punishment.ban.reason` : raison du ban appliqué.
- `punishment.ban.duration` : durée du ban (ex : `1d`, `3h`, `30m`).
- `punishment.ban.main-server` : serveur principal cible pour la sanction.
- `banned-commands` : commandes bloquées tant que le joueur est en combat.

## Commandes et permissions

Source : `paper-plugin/src/main/resources/plugin.yml`

### Commandes

- `/protection <status|disable>`
  - `status` : affiche le temps de protection restant.
  - `disable` : désactive la protection (avec confirmation GUI).
- `/safecombat-admin ...`
  - gestion admin (rechargement config et gestion de protection).

> Note : la syntaxe exacte de `/safecombat-admin` est déterminée par `paper-plugin/src/main/java/fr/gameofarkadia/safecombat/command/AdminCommand.java`.

### Permissions

- `safecombat.command.admin` : autorise la commande admin.
- `safecombat.*` : wildcard (inclut `safecombat.command.admin`).

## API (module `safecombat-api`)

### Ajouter la dépendance Maven

```xml
<dependency>
  <groupId>fr.game-of-arkadia</groupId>
  <artifactId>safecombat-api</artifactId>
  <version>2.0</version>
  <scope>provided</scope>
</dependency>
```

Le package est publié via GitHub Packages (`fr.game-of-arkadia`).

### Point d'entrée

Classe principale : `fr.gameofarkadia.safecombat.SafeCombatAPI`

Méthodes utiles :
- `SafeCombatAPI.isFighting(player/uuid)`
- `SafeCombatAPI.isProtected(player/uuid)`
- `SafeCombatAPI.isWanted(player/uuid)`
- `SafeCombatAPI.getCombatManager()`
- `SafeCombatAPI.getProtectionManager()`
- `SafeCombatAPI.getWantedPlayersManager()`

### Exemple 1 : Bloquer une action en combat

```java
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import org.bukkit.entity.Player;

public boolean canUseTeleport(Player player) {
    return !SafeCombatAPI.isFighting(player);
}
```

### Exemple 2 : Ajouter une protection temporaire

```java
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.protection.ProtectionReason;
import org.bukkit.entity.Player;

import java.time.Duration;

public void protectAfterEvent(Player player) {
    SafeCombatAPI.getProtectionManager().addPlayerProtection(
        player,
        ProtectionReason.ADMINISTRATOR,
        Duration.ofMinutes(15)
    );
}
```

### Exemple 3 : Lire le temps restant de protection

```java
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import org.bukkit.entity.Player;

import java.time.Duration;

public String protectionStatus(Player player) {
    if (!SafeCombatAPI.isProtected(player)) {
        return "Le joueur n'est pas protégé.";
    }

    Duration remaining = SafeCombatAPI.getProtectionManager().getRemainingDuration(player);
    return "Protection restante: " + remaining.getSeconds() + "s";
}
```

### Exemple 4 : Écouter les événements de combat

```java
import fr.gameofarkadia.safecombat.events.PlayerStartsFightingEvent;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class CombatListener implements Listener {

    @EventHandler
    public void onFightStart(PlayerStartsFightingEvent event) {
        event.getPlayer().sendMessage("Vous êtes maintenant en combat.");
    }

    @EventHandler
    public void onFightStop(PlayerStopsFightingEvent event) {
        event.getPlayer().getPlayer().sendMessage("Vous n'êtes plus en combat.");
    }
}
```

### Exemple 5 : Vérifier un joueur wanted

```java
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import org.bukkit.OfflinePlayer;

public boolean isPlayerWanted(OfflinePlayer player) {
    return SafeCombatAPI.isWanted(player);
}
```

## Structure du repository

- `api/` : interfaces et événements exposés aux plugins tiers.
- `paper-plugin/` : implémentation complète du plugin SafeCombat.

## Licence

Ce projet est distribué sous licence MIT. Voir `LICENSE`.
