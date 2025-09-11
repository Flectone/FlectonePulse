package net.flectone.pulse.module.message.particle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.particle.listener.ParticlePulseListener;
import net.flectone.pulse.module.message.particle.model.ParticleMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;

@Singleton
public class ParticleModule extends AbstractModuleLocalization<Localization.Message.Particle> {

    private final Message.Particle message;
    private final Permission.Message.Particle permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ParticleModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getParticle(), MessageType.PARTICLE);

        this.message = fileResolver.getMessage().getParticle();
        this.permission = fileResolver.getPermission().getMessage().getParticle();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ParticlePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String particle) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ParticleMetadata.<Localization.Message.Particle>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(string -> Strings.CS.replace(
                        string.getFormat(),
                        "<particle>",
                        particle
                ))
                .particle(particle)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}