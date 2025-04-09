package net.flectone.pulse.module.message.enchant;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.enchant.listener.EnchantPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftTranslationKeys;

import java.util.UUID;

@Singleton
public class EnchantModule extends AbstractModuleMessage<Localization.Message.Enchant> {

    private final Message.Enchant message;
    private final Permission.Message.Enchant permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EnchantModule(FileManager fileManager,
                         FPlayerService fPlayerService,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getEnchant());

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getEnchant();
        permission = fileManager.getPermission().getMessage().getEnchant();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(EnchantPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key, String enchant, String level, String value) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        if (key == MinecraftTranslationKeys.COMMANDS_ENCHANT_SUCCESS_SINGLE) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (key == MinecraftTranslationKeys.COMMANDS_ENCHANT_SUCCESS_SINGLE
                        ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<enchant>", enchant)
                        .replace("<level>", level)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
