package net.flectone.pulse.module.message.seed;

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
import net.flectone.pulse.module.message.seed.listener.SeedPulseListener;
import net.flectone.pulse.module.message.seed.model.SeedMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SeedModule extends AbstractModuleLocalization<Localization.Message.Seed> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SeedPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.SEED;
    }

    @Override
    public Message.Seed config() {
        return fileResolver.getMessage().getSeed();
    }

    @Override
    public Permission.Message.Seed permission() {
        return fileResolver.getPermission().getMessage().getSeed();
    }

    @Override
    public Localization.Message.Seed localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSeed();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String seed) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SeedMetadata.<Localization.Message.Seed>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> Strings.CS.replace(localization.getFormat(), "<seed>", seed))
                .seed(seed)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
