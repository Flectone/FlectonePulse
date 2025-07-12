package net.flectone.pulse.module.message.seed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.seed.listener.SeedPacketListener;
import net.flectone.pulse.service.FPlayerService;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class SeedModule extends AbstractModuleMessage<Localization.Message.Seed> {

    private final Message.Seed message;
    private final Permission.Message.Seed permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SeedModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSeed());

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileResolver.getMessage().getSeed();
        permission = fileResolver.getPermission().getMessage().getSeed();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SeedPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String seed) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat().replace("<seed>", seed))
                .sound(getSound())
                .sendBuilt();
    }

}
