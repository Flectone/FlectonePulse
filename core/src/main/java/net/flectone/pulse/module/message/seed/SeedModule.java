package net.flectone.pulse.module.message.seed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.seed.listener.SeedPulseListener;
import net.flectone.pulse.module.message.seed.model.SeedMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;

@Singleton
public class SeedModule extends AbstractModuleLocalization<Localization.Message.Seed> {

    private final Message.Seed message;
    private final Permission.Message.Seed permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SeedModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSeed(), MessageType.SEED);

        this.message = fileResolver.getMessage().getSeed();
        this.permission = fileResolver.getPermission().getMessage().getSeed();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SeedPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String seed) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SeedMetadata.<Localization.Message.Seed>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(s -> Strings.CS.replace(s.getFormat(), "<seed>", seed))
                .seed(seed)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
