ALTER TABLE `mail` ADD COLUMN `valid` INTEGER NOT NULL DEFAULT 1;

ALTER TABLE `ignore` ADD COLUMN `valid` INTEGER NOT NULL DEFAULT 1;

UPDATE `setting` SET `type` = 'CHAT_NAME' WHERE `type` = 'CHAT';

UPDATE `setting` SET `type` = 'SPY_STATUS' WHERE `type` = 'SPY';

DELETE FROM `setting` WHERE `value` = '';

INSERT INTO `fp_player` SELECT * FROM `player`;
DROP TABLE `player`;

INSERT INTO `fp_setting` SELECT * FROM `setting`;
DROP TABLE `setting`;

INSERT INTO `fp_mail` SELECT * FROM `mail`;
DROP TABLE `mail`;

INSERT INTO `fp_ignore` SELECT * FROM `ignore`;
DROP TABLE `ignore`;

INSERT INTO `fp_moderation` SELECT * FROM `moderation`;
DROP TABLE `moderation`;

INSERT INTO `fp_fcolor` SELECT * FROM `fcolor`;
DROP TABLE `fcolor`;

INSERT INTO `fp_player_fcolor` SELECT * FROM `player_fcolor`;
DROP TABLE `player_fcolor`;

INSERT INTO `fp_version` SELECT * FROM `version`;
DROP TABLE `version`;