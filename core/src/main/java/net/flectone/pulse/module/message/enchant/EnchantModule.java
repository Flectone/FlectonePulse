package net.flectone.pulse.module.message.enchant;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.enchant.listener.EnchantPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.apache.commons.lang3.StringUtils;

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
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, Enchant enchant) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS;

        if (isSingle && !enchant.value().isEmpty()) {
            fTarget = fPlayerService.getFPlayer(enchant.value());
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        isSingle ? s.getSingle() : s.getMultiple(),
                        new String[]{"<count>", "<enchant>", "<level>"},
                        new String[]{enchant.value(), enchant.name(), enchant.level()}
                ))
                .sound(getSound())
                .sendBuilt();
    }

}
