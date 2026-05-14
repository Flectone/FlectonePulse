package net.flectone.pulse.module.message.format.moderation.delete.model;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public record HistoryMessage(UUID uuid, Component component) {
}
