-- Players to be killed
CREATE TABLE `%DATABASE_NAME%`.`players_to_kill` (
    `player_uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
    `server_id` VARCHAR(255) NOT NULL,
    `disconnected_time` DATETIME NOT NULL,
    `to_kill_time` DATETIME NOT NULL
);

-- Players to be protected
CREATE TABLE `%DATABASE_NAME%`.`protected_players` (
    `player_uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL
);

-- Players already seen before
-- Used for newbie protection.
CREATE TABLE `%DATABASE_NAME%`.`seen_players` (
    `player_uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
    `player_name` VARCHAR(63) NOT NULL,
    `first_seen_time` DATETIME NOT NULL DEFAULT NOW()
);
