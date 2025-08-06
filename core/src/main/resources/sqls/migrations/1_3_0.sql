INSERT INTO fcolor (id, name) SELECT id, name FROM color;

INSERT INTO player_fcolor (id, number, player, fcolor, type) SELECT id, number, player, color, 'SEE' FROM player_color;

DROP TABLE IF EXISTS player_color;
DROP TABLE IF EXISTS color;