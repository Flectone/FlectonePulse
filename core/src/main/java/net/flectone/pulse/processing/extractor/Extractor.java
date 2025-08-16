package net.flectone.pulse.processing.extractor;

import net.kyori.adventure.text.TextComponent;

public abstract class Extractor {

    public String extractTarget(TextComponent targetComponent) {
        String target = targetComponent.content();
        if (target.isEmpty()) {
            target = targetComponent.insertion();
        }

        return target == null ? "" : target;
    }

}
