CREATE TABLE IF NOT EXISTS "player" (
    "id" SERIAL PRIMARY KEY,
    "online" BOOLEAN NOT NULL DEFAULT FALSE,
    "uuid" VARCHAR(36) NOT NULL UNIQUE,
    "name" VARCHAR(255) NOT NULL UNIQUE,
    "ip" VARCHAR(39)
);

CREATE TABLE IF NOT EXISTS "setting" (
    "id" SERIAL PRIMARY KEY,
    "player" INTEGER NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "value" TEXT,
    FOREIGN KEY("player") REFERENCES "player"("id"),
    UNIQUE("player", "type")
);

CREATE TABLE IF NOT EXISTS "mail" (
    "id" SERIAL PRIMARY KEY,
    "date" BIGINT NOT NULL,
    "sender" INTEGER NOT NULL,
    "receiver" INTEGER NOT NULL,
    "message" TEXT NOT NULL,
    FOREIGN KEY("sender") REFERENCES "player"("id"),
    FOREIGN KEY("receiver") REFERENCES "player"("id")
);

CREATE TABLE IF NOT EXISTS "ignore" (
    "id" SERIAL PRIMARY KEY,
    "date" BIGINT NOT NULL,
    "initiator" INTEGER NOT NULL,
    "target" INTEGER NOT NULL,
    FOREIGN KEY("initiator") REFERENCES "player"("id"),
    FOREIGN KEY("target") REFERENCES "player"("id")
);

CREATE TABLE IF NOT EXISTS "moderation" (
    "id" SERIAL PRIMARY KEY,
    "player" INTEGER NOT NULL,
    "date" BIGINT NOT NULL,
    "time" INTEGER NOT NULL,
    "reason" TEXT,
    "moderator" INTEGER NOT NULL,
    "type" INTEGER NOT NULL,
    "valid" BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY("player") REFERENCES "player"("id"),
    FOREIGN KEY("moderator") REFERENCES "player"("id")
);

CREATE TABLE IF NOT EXISTS "color" (
    "id" SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "player_color" (
    "id" SERIAL PRIMARY KEY,
    "number" INTEGER NOT NULL,
    "player" INTEGER NOT NULL,
    "color" INTEGER NOT NULL,
    FOREIGN KEY("player") REFERENCES "player"("id"),
    FOREIGN KEY("color") REFERENCES "color"("id")
);