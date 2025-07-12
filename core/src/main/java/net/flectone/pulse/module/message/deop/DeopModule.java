package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class DeopModule extends AbstractModuleMessage<Localization.Message.Deop> {

    private final Message.Deop message;
    private final Permission.Message.Deop permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public DeopModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getDeop());

        this.message = fileResolver.getMessage().getDeop();
        this.permission = fileResolver.getPermission().getMessage().getDeop();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeopPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String target) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Deop::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
