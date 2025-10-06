ALTER TABLE "mail" ADD COLUMN "valid" BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE "ignore" ADD COLUMN "valid" BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE `setting` SET `type` = 'CHAT_NAME' WHERE `type` = 'CHAT';

UPDATE `setting` SET `type` = 'SPY_STATUS' WHERE `type` = 'SPY';

DELETE FROM `setting` WHERE `value` = '';

INSERT INTO `fp_fcolor` SELECT * FROM `fcolor`;

INSERT INTO `fp_player` SELECT * FROM `player`;

INSERT INTO `fp_player_fcolor` SELECT * FROM `player_fcolor`;

INSERT INTO `fp_setting` SELECT * FROM `setting`;

INSERT INTO `fp_mail` SELECT * FROM `mail`;

INSERT INTO `fp_ignore` SELECT * FROM `ignore`;

INSERT INTO `fp_moderation` SELECT * FROM `moderation`;