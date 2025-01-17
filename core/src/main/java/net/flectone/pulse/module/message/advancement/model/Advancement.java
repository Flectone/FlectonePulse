package net.flectone.pulse.module.message.advancement.model;

import net.flectone.pulse.util.AdvancementType;

public record Advancement(String title, String description, AdvancementType type) {
}
