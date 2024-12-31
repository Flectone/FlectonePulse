package net.flectone.pulse.module.message.death.model;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.event.HoverEvent;


@Getter
@Setter
public class Item {

    private final String name;
    private HoverEvent<?> hoverEvent;

    public Item(String name) {
        this.name = name;
    }
}
