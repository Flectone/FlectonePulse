package net.flectone.pulse.util;

import java.util.Arrays;

public enum MinecraftMessage {

    NO_SLEEP("block.minecraft.bed.no_sleep"),
    NOT_SAFE("block.minecraft.bed.not_safe"),
    OBSTRUCTED("block.minecraft.bed.obstructed"),
    OCCUPIED("block.minecraft.bed.occupied"),
    TOO_FAR_AWAY("block.minecraft.bed.too_far_away"),
    NOT_POSSIBLE("sleep.not_possible"),
    PLAYERS_SLEEPING("sleep.players_sleeping"),
    SKIPPING_NIGHT("sleep.skipping_night"),
    UNKNOWN("unknown");

    private final String key;

    MinecraftMessage(String key) {
        this.key = key;
    }

    public static MinecraftMessage fromString(String string) {
        return Arrays.stream(MinecraftMessage.values())
                .filter(type -> type.key.equalsIgnoreCase(string))
                .findAny()
                .orElse(UNKNOWN);
    }

}
