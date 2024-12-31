package net.flectone.pulse.module.message.bubble.manager;

import net.flectone.pulse.model.FPlayer;

public interface BubbleManager {

    void remove(FPlayer fPlayer);
    void reload();
    void process(FPlayer fPlayer);
}
