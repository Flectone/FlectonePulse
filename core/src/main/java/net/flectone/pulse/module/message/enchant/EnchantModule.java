package net.flectone.pulse.module.message.enchant;

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
import net.flectone.pulse.module.message.enchant.listener.EnchantPacketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Singleton
public class EnchantModule extends AbstractModuleMessage<Localization.Message.Enchant> {

    private final Message.Enchant message;
    private final Permission.Message.Enchant permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;

    @Inject
    public EnchantModule(FileManager fileManager,
                         FPlayerManager fPlayerManager,
                         ListenerManager listenerManager) {
        super(localization -> localization.getMessage().getEnchant());

        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;

        message = fileManager.getMessage().getEnchant();
        permission = fileManager.getPermission().getMessage().getEnchant();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(EnchantPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String enchant, @NotNull String level, @Nullable String target, @Nullable String count) {
        if (target == null && count == null) return;

        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        if (target != null) {
            fTarget = fPlayerManager.getOnline(target);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .receiver(fPlayer)
                .format(s -> (count == null ? s.getSingle() : s.getMultiple().replace("<count>", count))
                        .replace("<enchant>", enchant)
                        .replace("<level>", level)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
