package net.flectone.pulse.module.message.vanilla.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.Extractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaPacketListener implements PacketListener {

    private static final TranslatableComponent NOT_VALID_MESSAGE = Component.translatable("block.minecraft.spawn.not_valid");
    private static final TranslatableComponent LEGACY_NOT_VALID_MESSAGE = Component.translatable("block.minecraft.bed.not_valid");

    private final FPlayerService fPlayerService;
    private final VanillaModule vanillaModule;
    private final Extractor extractor;
    private final @Named("isNewerThanOrEqualsV_1_16") boolean isNewerThanOrEqualsV_1_16;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.CHANGE_GAME_STATE) return;

        WrapperPlayServerChangeGameState wrapper = new WrapperPlayServerChangeGameState(event);
        if (wrapper.getReason() != WrapperPlayServerChangeGameState.Reason.NO_RESPAWN_BLOCK_AVAILABLE) return;

        Optional<ParsedComponent> parsedComponent = extractor.extract(isNewerThanOrEqualsV_1_16 ? NOT_VALID_MESSAGE : LEGACY_NOT_VALID_MESSAGE);
        if (parsedComponent.isEmpty()) return;

        event.setCancelled(true);

        vanillaModule.send(fPlayerService.getFPlayer(event.getUser().getUUID()), parsedComponent.get());
    }

}
