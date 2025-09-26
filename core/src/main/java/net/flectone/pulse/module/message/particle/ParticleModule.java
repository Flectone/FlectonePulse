package net.flectone.pulse.module.message.particle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.particle.listener.ParticlePulseListener;
import net.flectone.pulse.module.message.particle.model.ParticleMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
public class ParticleModule extends AbstractModuleLocalization<Localization.Message.Particle> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ParticleModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(MessageType.PARTICLE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(ParticlePulseListener.class);
    }

    @Override
    public Message.Particle config() {
        return fileResolver.getMessage().getParticle();
    }

    @Override
    public Permission.Message.Particle permission() {
        return fileResolver.getPermission().getMessage().getParticle();
    }

    @Override
    public Localization.Message.Particle localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getParticle();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String particle) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ParticleMetadata.<Localization.Message.Particle>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> Strings.CS.replace(localization.getFormat(), "<particle>", particle))
                .particle(particle)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}