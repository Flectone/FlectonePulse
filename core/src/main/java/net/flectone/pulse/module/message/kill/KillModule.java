package net.flectone.pulse.module.message.kill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.kill.listener.KillPulseListener;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.module.message.kill.model.KillMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KillModule extends AbstractModuleLocalization<Localization.Message.Kill> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(KillPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.KILL;
    }

    @Override
    public Message.Kill config() {
        return fileResolver.getMessage().getKill();
    }

    @Override
    public Permission.Message.Kill permission() {
        return fileResolver.getPermission().getMessage().getKill();
    }

    @Override
    public Localization.Message.Kill localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getKill();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Kill kill) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(KillMetadata.<Localization.Message.Kill>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> translationKey == MinecraftTranslationKey.COMMANDS_KILL_SUCCESS_MULTIPLE
                        ? Strings.CS.replace(localization.getMultiple(), "<entities>", StringUtils.defaultString(kill.getEntities()))
                        : localization.getSingle()
                )
                .kill(kill)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, kill.getTarget())})
                .build()
        );
    }
}
