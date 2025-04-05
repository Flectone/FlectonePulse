package net.flectone.pulse.model;

public record ExternalModeration(String playerName, String moderatorName, String reason,
                                 long moderationId, long date, long time, boolean permanent) {
}
