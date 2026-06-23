package net.flectone.pulse.module.message.serverlink;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerServerLinks;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerLinks;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.serverlink.listener.MinecraftPulseServerlinkListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftServerlinkModule implements ModuleLocalization<Localization.Message.Serverlink> {

    private final FileFacade fileFacade;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final MinecraftPacketSender packetSender;
    private final MessagePipeline messagePipeline;
    private final SocialService socialService;
    private final MinecraftPacketProvider packetProvider;

    @Override
    public void onEnable() {
        listenerRegistry.register(MinecraftPulseServerlinkListener.class);

        if (config().ticker().enable()) {
            taskScheduler.runPlayerAsyncTimer(this::sendLinks, config().ticker().period());
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SERVERLINK;
    }

    @Override
    public Localization.Message.Serverlink localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().serverlink();
    }

    @Override
    public Message.Serverlink config() {
        return fileFacade.message().serverlink();
    }

    @Override
    public Permission.Message.Serverlink permission() {
        return fileFacade.permission().message().serverlink();
    }

    @Override
    public BiPredicate<FEntity, Boolean> disablePredicate() {
        return ModuleLocalization.super.disablePredicate().or((fEntity, _) -> {
            User user = packetProvider.getUser(fEntity.uuid());
            return user == null || user.getPacketVersion().isOlderThan(ClientVersion.V_1_21);
        });
    }

    public void sendLinks(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Map<String, String> links = config().values();
        Map<String, String> linksMessages = localization(fPlayer).values();

        List<WrapperCommonServerServerLinks.ServerLink> serverLinks = links.entrySet().stream()
                .map(entry -> {
                    String link = entry.getValue();

                    Component linkComponent = messagePipeline.build(MessageContext.builder()
                            .sender(fPlayer)
                            .message(linksMessages.getOrDefault(entry.getKey(), link))
                            .build()
                    );

                    return new WrapperCommonServerServerLinks.ServerLink(linkComponent, link);
                }).toList();

        packetSender.send(fPlayer, new WrapperPlayServerServerLinks(serverLinks));
    }

}
