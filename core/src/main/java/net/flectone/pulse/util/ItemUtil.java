package net.flectone.pulse.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public interface ItemUtil {

    Component translatableComponent(Object item);

    HoverEvent<?> hoverEvent(Object item);
}
