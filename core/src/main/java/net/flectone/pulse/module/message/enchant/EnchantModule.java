package net.flectone.pulse.module.message.enchant;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.enchant.listener.EnchantPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class EnchantModule extends AbstractModuleLocalization<Localization.Message.Enchant> {

    private final Message.Enchant message;
    private final Permission.Message.Enchant permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EnchantModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getEnchant());

        this.message = fileResolver.getMessage().getEnchant();
        this.permission = fileResolver.getPermission().getMessage().getEnchant();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(EnchantPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, String enchant, String level, String value) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS;

        if (isSingle && !value.isEmpty()) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSingle ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<enchant>", enchant)
                        .replace("<level>", level)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
