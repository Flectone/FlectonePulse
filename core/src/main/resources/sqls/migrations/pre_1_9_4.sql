ALTER TABLE `fp_moderation` RENAME TO `fp_moderation_old`;

ALTER TABLE `fp_player` ADD COLUMN `server` VARCHAR(255);