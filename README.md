<img src="banner.png" alt="Safe Combat logo">

Avoiding **anti-gambling** in **PvP**  

> Languages available :
> - **English** (default) 🇬🇧 🇺🇸
> - **French** 🇫🇷
> - **Custom** language into **custom file**

## Features

> ⚔️ **Anti-logout**  
> A player who **disconnects in combat** without **reconnecting after a certain time** will **be killed, and his stuff dropped to the ground**.
> - Time remaining visible in an animated boss bar.

> 🔙 **Anti-safe zone back**  
> Compatibility with **WorldGuard** thanks to a custom flag to prevent players in combat from entering into areas.  
> - A client-only force field is generated to prevent the player from advancing. 
> - Nor can he use ender pearls to teleport.  
> 
> See [**demonstration video**](https://www.youtube.com/watch?v=nF5s4Tff7Yk).

> 🛡️ **Protection**  
> A protected player **cannot attack or be attacked**.
> - Automatic protection for **new players on your server.**
> - Personalised protection for a player _(administrator command)_.
> - Time remaining visible in an animated boss bar.
> - A player **can refuse protection** with a command (irreversible).

## Commands

- **/protection**
    - **disable**  
    Disable your protection
    - **remove**  
    Unprotect a player
    - **add**  
    Protect a player

## Permissions

- **safecombat.protection**  
    Add and remove player's protection

# API

_You can use JitPack.io to implement SafeCombat as a dependency in your project_  
[![](https://jitpack.io/v/Game-Of-Arkadia/Plugin-SafeCombat.svg)](https://jitpack.io/#Game-Of-Arkadia/Plugin-SafeCombat)

### Events
Events aren't cancellable yet.
> **PlayerStartsFightingEvent**
> - Called when the player enters in combat mode.
> - Called twice : for the attacker and the attacked.
> - Custom getters :
>   - **getPlayer()** Get the player concerned.
>   - **getType()** Get if the player is the attacker or the attacked.

> **PlayerStopsFightingEvent**
> - Called when the player leaves the combat mode.
> - The player can be the attacker or the attacked.
> - Custom getter :
>   - **getPlayer()** Get the player concerned.

# Soft dependencies

- **WorldGuard**
- A **Factions** plugin _(SaberFactions recommended)_