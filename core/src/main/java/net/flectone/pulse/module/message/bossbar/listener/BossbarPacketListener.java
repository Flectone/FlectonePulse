package net.flectone.pulse.module.message.bossbar.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.bossbar.BossbarModule;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossbarPacketListener implements PacketListener {

    private final FileFacade fileFacade;
    private final BossbarModule bossbarModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.BOSS_BAR) return;

        WrapperPlayServerBossBar wrapper = new WrapperPlayServerBossBar(event);

        WrapperPlayServerBossBar.Action action = wrapper.getAction();
        if (action != WrapperPlayServerBossBar.Action.ADD && action != WrapperPlayServerBossBar.Action.UPDATE_TITLE) return;

        Component title = wrapper.getTitle();
        if (!(title instanceof TranslatableComponent translatableTitle)) return;

        String titleKey = translatableTitle.key();
        if (!fileFacade.localization().message().bossbar().types().containsKey(titleKey)) return;

        UUID playerUUID = event.getUser().getUUID();
        UUID bossbarUUID = wrapper.getUUID();
        boolean announce = action == WrapperPlayServerBossBar.Action.ADD;

        bossbarModule.send(playerUUID, bossbarUUID, titleKey, announce, title);
    }

}
