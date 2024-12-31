package net.flectone.pulse.platform;

import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;

public abstract class PlatformSender {

    public abstract void sendMessage(FPlayer fPlayer, Component component);

    public abstract void sendActionBar(FPlayer fPlayer, Component component);

    public abstract void sendPlayerListFooter(FPlayer fPlayer, Component component);

    public abstract void sendPlayerListHeader(FPlayer fPlayer, Component component);
}
