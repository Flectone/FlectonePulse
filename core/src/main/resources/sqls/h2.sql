CREATE TABLE IF NOT EXISTS `player` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `online` INT NOT NULL DEFAULT 0,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `ip` VARCHAR(39)
);

CREATE TABLE IF NOT EXISTS setting (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `player` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `value` TEXT,
    FOREIGN KEY (`player`) REFERENCES `player`(`id`),
    UNIQUE (`player`, `type`)
);

CREATE TABLE IF NOT EXISTS `mail` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `date` DOUBLE NOT NULL,
    `sender` INT NOT NULL,
    `receiver` INT NOT NULL,
    `message` TEXT NOT NULL,
    FOREIGN KEY (`sender`) REFERENCES `player`(`id`),
    FOREIGN KEY (`receiver`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `ignore` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `date` DOUBLE NOT NULL,
    `initiator` INT NOT NULL,
    `target` INT NOT NULL,
    `valid` INT NOT NULL DEFAULT 1,
    FOREIGN KEY (`initiator`) REFERENCES `player`(`id`),
    FOREIGN KEY (`target`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `moderation` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `player` INT NOT NULL,
    `date` DOUBLE NOT NULL,
    `time` INT NOT NULL,
    `reason` TEXT,
    `moderator` INT NOT NULL,
    `type` INT NOT NULL,
    `valid` INT NOT NULL DEFAULT 1,
    FOREIGN KEY (`player`) REFERENCES `player`(`id`),
    FOREIGN KEY (`moderator`) REFERENCES `player`(`id`)
);

CREATE TABLE IF NOT EXISTS `fcolor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `name` TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `player_fcolor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `number` INT NOT NULL,
    `player` INT NOT NULL,
    `fcolor` INT NOT NULL,
    `type` TEXT NOT NULL,
    FOREIGN KEY (`player`) REFERENCES `player`(`id`),
    FOREIGN KEY (`fcolor`) REFERENCES `fcolor`(`id`)
);