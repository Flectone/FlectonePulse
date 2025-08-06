INSERT INTO fcolor (id, name) SELECT id, name FROM color;

INSERT INTO player_fcolor (id, number, player, fcolor, type) SELECT id, number, player, color, 'SEE' FROM player_color;

INSERT INTO fcolor (name) SELECT DISTINCT s.value FROM setting s WHERE s.type = 'STYLE' AND s.value NOT IN (SELECT name FROM fcolor);

INSERT INTO player_fcolor (player, fcolor, type, number) SELECT s.player, (SELECT id FROM fcolor WHERE name = s.value),'OUT',3 FROM setting s WHERE s.type = 'STYLE';

INSERT INTO player_fcolor (player, fcolor, type, number) SELECT s.player,  (SELECT id FROM fcolor WHERE name = s.value),'OUT', 4 FROM setting s WHERE s.type = 'STYLE';

DROP TABLE IF EXISTS player_color;
DROP TABLE IF EXISTS color;
DELETE FROM `setting` WHERE `type` = 'STYLE';