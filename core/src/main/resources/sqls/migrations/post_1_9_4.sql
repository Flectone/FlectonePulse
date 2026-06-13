INSERT INTO `fp_moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`, `server`)
SELECT `player`, `date`, `time`, `reason`, `moderator`,
       CASE `type`
           WHEN 0 THEN 'MUTE'
           WHEN 1 THEN 'BAN'
           WHEN 2 THEN 'WARN'
           WHEN 3 THEN 'KICK'
       END,  `valid`, NULL
FROM `fp_moderation_old`;

INSERT INTO `fp_player_fcolor` (`number`, `player`, `fcolor`, `type`)
SELECT DISTINCT `number`, `player`, `fcolor`, `type`
FROM `fp_player_fcolor_old`;