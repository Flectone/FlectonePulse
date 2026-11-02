package net.flectone.pulse.module.command.chatsetting.builder;

import net.kyori.adventure.text.Component;

public record MenuButton(
        String id,
        Component title,
        Component lore,
        MenuAction action
) {

    @FunctionalInterface
    public interface MenuAction {
        void accept(HytaleChatsettingPage page);
    }

}
