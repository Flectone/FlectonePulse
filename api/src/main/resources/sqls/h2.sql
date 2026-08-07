CREATE TABLE IF NOT EXISTS `fp_player` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `online` TINYINT NOT NULL DEFAULT 0,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL,
    `ip` VARCHAR(39),
    UNIQUE(`uuid`, `name`)
);

CREATE TABLE IF NOT EXISTS `fp_time` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `player` INT NOT NULL UNIQUE,
    `first` BIGINT NOT NULL,
    `last` BIGINT NOT NULL,
    `total` BIGINT NOT NULL,
    `sessions` INT NOT NULL DEFAULT 0,
    FOREIGN KEY (`player`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_setting` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `player` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `value` TEXT,
    FOREIGN KEY (`player`) REFERENCES `fp_player`(`id`),
    UNIQUE (`player`, `type`)
);

CREATE TABLE IF NOT EXISTS `fp_mail` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `date` BIGINT NOT NULL,
    `sender` INT NOT NULL,
    `receiver` INT NOT NULL,
    `message` TEXT NOT NULL,
    `valid` TINYINT NOT NULL DEFAULT 1,
    FOREIGN KEY (`sender`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY (`receiver`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_ignore` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `date` BIGINT NOT NULL,
    `initiator` INT NOT NULL,
    `target` INT NOT NULL,
    `valid` TINYINT NOT NULL DEFAULT 1,
    FOREIGN KEY (`initiator`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY (`target`) REFERENCES `fp_player`(`id`),
    UNIQUE (`initiator`, `target`)
);

CREATE TABLE IF NOT EXISTS `fp_moderation` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `player` INT NOT NULL,
    `date` BIGINT NOT NULL,
    `time` BIGINT NOT NULL,
    `reason` TEXT,
    `moderator` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `valid` TINYINT NOT NULL DEFAULT 1,
    `server` VARCHAR(255),
    FOREIGN KEY (`player`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY (`moderator`) REFERENCES `fp_player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fp_fcolor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `name` VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `fp_player_fcolor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `number` INT NOT NULL,
    `player` INT NOT NULL,
    `fcolor` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    FOREIGN KEY (`player`) REFERENCES `fp_player`(`id`),
    FOREIGN KEY (`fcolor`) REFERENCES `fp_fcolor`(`id`),
    UNIQUE(`number`, `player`, `type`)
);

CREATE TABLE IF NOT EXISTS `fp_version` (
    `id` INTEGER PRIMARY KEY CHECK (id = 1),
    `name` VARCHAR(32) NOT NULL
);

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