INSERT INTO `fp_moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`, `server`)
SELECT `player`, `date`, `time`, `reason`, `moderator`, CASE `type`
           WHEN 0 THEN 'MUTE'
           WHEN 1 THEN 'BAN'
           WHEN 2 THEN 'WARN'
           WHEN 3 THEN 'KICK'
       END,  `valid`, NULL FROM `fp_moderation_old`;