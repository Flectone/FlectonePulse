package net.flectone.pulse.module.message.setblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.setblock.listener.SetblockPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.jetbrains.annotations.NotNull;

@Singleton
public class SetblockModule extends AbstractModuleLocalization<Localization.Message.Setblock> {

    private final Message.Setblock message;
    private final Permission.Message.Setblock permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SetblockModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSetblock());

        this.message = fileResolver.getMessage().getSetblock();
        this.permission = fileResolver.getPermission().getMessage().getSetblock();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SetblockPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, @NotNull String x, @NotNull String y, @NotNull String z) {
        if (isModuleDisabledFor(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat()
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
