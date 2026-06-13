CREATE TABLE IF NOT EXISTS `fp_player` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `online` INTEGER NOT NULL DEFAULT 0 CHECK(`online` IN (0, 1)),
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL,
    `ip` VARCHAR(39),
    UNIQUE(`uuid`, `name`)
);

CREATE TABLE IF NOT EXISTS `fp_time` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `player` INTEGER NOT NULL UNIQUE,
    `first` INTEGER NOT NULL,
    `last` INTEGER NOT NULL,
    `total` INTEGER NOT NULL,
    `sessions` INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (`player`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_setting` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `player` INTEGER NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `value` TEXT,
    FOREIGN KEY(`player`) REFERENCES `fp_player`(`id`),
    UNIQUE(`player`, `type`)
);

CREATE TABLE IF NOT EXISTS `fp_mail` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `date` INTEGER NOT NULL,
    `sender` INTEGER NOT NULL,
    `receiver` INTEGER NOT NULL,
    `message` TEXT NOT NULL,
    `valid` INTEGER NOT NULL DEFAULT 1 CHECK(`valid` IN (0, 1)),
    FOREIGN KEY(`sender`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY(`receiver`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_ignore` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `date` INTEGER NOT NULL,
    `initiator` INTEGER NOT NULL,
    `target` INTEGER NOT NULL,
    `valid` INTEGER NOT NULL DEFAULT 1 CHECK(`valid` IN (0, 1)),
    FOREIGN KEY(`initiator`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY(`target`) REFERENCES `fp_player`(`id`),
    UNIQUE(`initiator`, `target`)
);

CREATE TABLE IF NOT EXISTS `fp_moderation` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `player` INTEGER NOT NULL,
    `date` INTEGER NOT NULL,
    `time` INTEGER NOT NULL,
    `reason` TEXT,
    `moderator` INTEGER NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `valid` INTEGER NOT NULL DEFAULT 1 CHECK(`valid` IN (0, 1)),
    `server` VARCHAR(255),
    FOREIGN KEY(`player`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY(`moderator`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_fcolor` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `fp_player_fcolor` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `number` INTEGER NOT NULL,
    `player` INTEGER NOT NULL,
    `fcolor` INTEGER NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    FOREIGN KEY(`player`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY(`fcolor`) REFERENCES `fp_fcolor`(`id`),
    UNIQUE(`number`, `player`, `type`)
);

CREATE TABLE IF NOT EXISTS `fp_version` (
    `id` INTEGER PRIMARY KEY CHECK (`id` = 1),
    `name` VARCHAR(32) NOT NULL
);

PRAGMA foreign_keys = ON;

CREATE INDEX IF NOT EXISTS `idx_fp_player_ip` ON `fp_player`(`ip`);
CREATE INDEX IF NOT EXISTS `idx_fp_player_online` ON `fp_player`(`online`);

CREATE INDEX IF NOT EXISTS `idx_fp_time_total` ON `fp_time`(`total` DESC);

CREATE INDEX IF NOT EXISTS `idx_fp_mail_receiver_valid` ON `fp_mail`(`receiver`, `valid`);
CREATE INDEX IF NOT EXISTS `idx_fp_mail_sender_valid` ON `fp_mail`(`sender`, `valid`);

CREATE INDEX IF NOT EXISTS `idx_fp_ignore_initiator_valid` ON `fp_ignore`(`initiator`, `valid`);

CREATE INDEX IF NOT EXISTS `idx_fp_moderation_player_type_valid_time` ON `fp_moderation`(`player`, `type`, `valid`, `time`);
CREATE INDEX IF NOT EXISTS `idx_fp_moderation_type_valid_time` ON `fp_moderation`(`type`, `valid`, `time`);
CREATE INDEX IF NOT EXISTS `idx_fp_moderation_moderator` ON `fp_moderation`(`moderator`);

CREATE INDEX IF NOT EXISTS `idx_fp_player_fcolor_player_type` ON `fp_player_fcolor`(`player`, `type`);