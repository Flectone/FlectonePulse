package net.flectone.pulse.module.message.contact.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.rightclick.listener.RightclickPacketListener;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.ComponentUtil;

import java.util.Optional;
import java.util.UUID;


@Singleton
public class RightclickModule extends AbstractModuleMessage<Localization.Message.Contact.Rightclick> {

    private final Message.Contact.Rightclick message;
    private final Permission.Message.Contact.Rightclick permission;

    private final PlatformSender platformSender;
    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;
    private final ComponentUtil componentUtil;

    @Inject
    public RightclickModule(FileManager fileManager,
                            PlatformSender platformSender,
                            FPlayerManager fPlayerManager,
                            ListenerManager listenerManager,
                            ComponentUtil componentUtil) {
        super(localization -> localization.getMessage().getContact().getRightclick());
        this.platformSender = platformSender;
        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;
        this.componentUtil = componentUtil;

        message = fileManager.getMessage().getContact().getRightclick();
        permission = fileManager.getPermission().getMessage().getContact().getRightclick();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        listenerManager.register(RightclickPacketListener.class);
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

        platformSender.sendActionBar(fPlayer, componentUtil.builder(optionalFTarget.get(), fPlayer, resolveLocalization(fPlayer).getFormat()).build());
        playSound(fPlayer);
    }
}
