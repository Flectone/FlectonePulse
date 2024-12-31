CREATE TABLE IF NOT EXISTS `player` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
    `online` INTEGER NOT NULL DEFAULT '0',
	`uuid` TEXT NOT NULL UNIQUE,
	`name` TEXT NOT NULL UNIQUE,
	`ip` TEXT,
    `chat` TEXT,
    `locale` TEXT,
    `world_prefix` TEXT,
    `stream_prefix` TEXT,
    `afk_suffix` TEXT,
	`setting` TEXT
);

CREATE TABLE IF NOT EXISTS `mail` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
	`date` REAL NOT NULL,
	`sender` INTEGER NOT NULL,
	`receiver` INTEGER NOT NULL,
	`message` TEXT NOT NULL,
FOREIGN KEY(`sender`) REFERENCES `player`(`id`),
FOREIGN KEY(`receiver`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `ignore` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
	`date` REAL NOT NULL,
	`initiator` INTEGER NOT NULL,
	`target` INTEGER NOT NULL,
FOREIGN KEY(`initiator`) REFERENCES `player`(`id`),
FOREIGN KEY(`target`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `moderation` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
	`player` INTEGER NOT NULL,
	`date` REAL NOT NULL,
	`time` INTEGER NOT NULL,
	`reason` TEXT,
	`moderator` INTEGER NOT NULL,
    `type` INTEGER NOT NULL,
    `valid` INTEGER NOT NULL DEFAULT '1',
FOREIGN KEY(`player`) REFERENCES `player`(`id`),
FOREIGN KEY(`moderator`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `color` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
	`name` TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `player_color` (
	`id` INTEGER PRIMARY KEY NOT NULL UNIQUE,
    `number` INTEGER NOT NULL,
	`player` INTEGER NOT NULL,
	`color` INTEGER NOT NULL,
FOREIGN KEY(`player`) REFERENCES `player`(`id`),
FOREIGN KEY(`color`) REFERENCES `color`(`id`)
);
