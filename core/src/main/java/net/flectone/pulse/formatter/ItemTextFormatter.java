package net.flectone.pulse.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public interface ItemTextFormatter {

    Component translatableComponent(Object item);

    HoverEvent<?> hoverEvent(Object item);
}
