CREATE TABLE IF NOT EXISTS "fp_player" (
    "id" SERIAL PRIMARY KEY,
    "online" BOOLEAN NOT NULL DEFAULT FALSE,
    "uuid" VARCHAR(36) NOT NULL UNIQUE,
    "name" VARCHAR(255) NOT NULL UNIQUE,
    "ip" VARCHAR(39),
    UNIQUE("uuid", "name")
);

CREATE TABLE IF NOT EXISTS "fp_setting" (
    "id" SERIAL PRIMARY KEY,
    "player" INTEGER NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "value" TEXT,
    FOREIGN KEY("player") REFERENCES "fp_player"("id"),
    UNIQUE("player", "type")
);

CREATE TABLE IF NOT EXISTS "fp_mail" (
    "id" SERIAL PRIMARY KEY,
    "date" BIGINT NOT NULL,
    "sender" INTEGER NOT NULL,
    "receiver" INTEGER NOT NULL,
    "message" TEXT NOT NULL,
    "valid" BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY("sender") REFERENCES "fp_player"("id"),
    FOREIGN KEY("receiver") REFERENCES "fp_player"("id")
);

CREATE TABLE IF NOT EXISTS "fp_ignore" (
    "id" SERIAL PRIMARY KEY,
    "date" BIGINT NOT NULL,
    "initiator" INTEGER NOT NULL,
    "target" INTEGER NOT NULL,
    "valid" BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY("initiator") REFERENCES "fp_player"("id"),
    FOREIGN KEY("target") REFERENCES "fp_player"("id"),
    UNIQUE("initiator", "target")
);

CREATE TABLE IF NOT EXISTS "fp_moderation" (
    "id" SERIAL PRIMARY KEY,
    "player" INTEGER NOT NULL,
    "date" BIGINT NOT NULL,
    "time" BIGINT NOT NULL,
    "reason" TEXT,
    "moderator" INTEGER NOT NULL,
    "type" INTEGER NOT NULL,
    "valid" BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY("player") REFERENCES "fp_player"("id"),
    FOREIGN KEY("moderator") REFERENCES "fp_player"("id")
);

CREATE TABLE IF NOT EXISTS "fp_fcolor" (
    "id" SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "fp_player_fcolor" (
    "id" SERIAL PRIMARY KEY,
    "number" INTEGER NOT NULL,
    "player" INTEGER NOT NULL,
    "fcolor" INTEGER NOT NULL,
    "type" TEXT NOT NULL,
    FOREIGN KEY("player") REFERENCES "fp_player"("id"),
    FOREIGN KEY("fcolor") REFERENCES "fp_fcolor"("id")
);

CREATE TABLE IF NOT EXISTS "fp_version" (
    "id" INTEGER PRIMARY KEY CHECK ("id" = 1),
    "name" TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS "idx_fp_player_ip" ON "fp_player"("ip");
CREATE INDEX IF NOT EXISTS "idx_fp_player_online" ON "fp_player"("online");

CREATE INDEX IF NOT EXISTS "idx_fp_mail_receiver_valid_date" ON "fp_mail"("receiver", "valid", "date");
CREATE INDEX IF NOT EXISTS "idx_fp_mail_sender_valid_date" ON "fp_mail"("sender", "valid", "date");

CREATE INDEX IF NOT EXISTS "idx_fp_ignore_initiator_target_valid" ON "fp_ignore"("initiator", "target", "valid");
CREATE INDEX IF NOT EXISTS "idx_fp_ignore_target_valid" ON "fp_ignore"("target", "valid");

CREATE INDEX IF NOT EXISTS "idx_fp_moderation_player_valid_time" ON "fp_moderation"("player", "valid", "time");
CREATE INDEX IF NOT EXISTS "idx_fp_moderation_player_type_valid" ON "fp_moderation"("player", "type", "valid");
CREATE INDEX IF NOT EXISTS "idx_fp_moderation_moderator_valid" ON "fp_moderation"("moderator", "valid");
CREATE INDEX IF NOT EXISTS "idx_fp_moderation_valid_time" ON "fp_moderation"("valid", "time");

CREATE INDEX IF NOT EXISTS "idx_fp_player_fcolor_player_type" ON "fp_player_fcolor"("player", "type");
CREATE INDEX IF NOT EXISTS "idx_fp_player_fcolor_player_type_number" ON "fp_player_fcolor"("player", "type", "number");