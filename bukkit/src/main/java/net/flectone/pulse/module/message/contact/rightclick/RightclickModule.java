package net.flectone.pulse.module.message.contact.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.rightclick.listener.RightclickPacketListener;

import java.util.Optional;
import java.util.UUID;


@Singleton
public class RightclickModule extends AbstractModuleMessage<Localization.Message.Contact.Rightclick> {

    private final Message.Contact.Rightclick message;
    private final Permission.Message.Contact.Rightclick permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public RightclickModule(FileManager fileManager,
                            FPlayerManager fPlayerManager,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getContact().getRightclick());
        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getContact().getRightclick();
        permission = fileManager.getPermission().getMessage().getContact().getRightclick();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        listenerRegistry.register(RightclickPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID uuid, int targetId) {
        FPlayer fPlayer = fPlayerManager.get(uuid);

        if (checkModulePredicates(fPlayer)) return;

        Optional<FPlayer> optionalFTarget = fPlayerManager.getFPlayers()
                .stream()
                .filter(filter -> filter.getEntityId() == targetId)
                .findAny();

        if (optionalFTarget.isEmpty()) return;

        builder(optionalFTarget.get())
                .receiver(fPlayer)
                .format(Localization.Message.Contact.Rightclick::getFormat)
                .destination(message.getDestination())
                .sound(getSound())
                .sendBuilt();
    }
}
