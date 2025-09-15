package net.flectone.pulse.module.message.commandblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.commandblock.listener.CommandBlockPulseListener;
import net.flectone.pulse.module.message.commandblock.model.CommandBlockMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

@Singleton
public class CommandblockModule extends AbstractModuleLocalization<Localization.Message.Commandblock> {

    private final Message.Commandblock message;
    private final Permission.Message.Commandblock permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public CommandblockModule(FileResolver fileResolver,
                              ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getCommandblock(), MessageType.COMMANDBLOCK);

        this.message = fileResolver.getMessage().getCommandblock();
        this.permission = fileResolver.getPermission().getMessage().getCommandblock();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(CommandBlockPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, @Nullable String command) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(CommandBlockMetadata.<Localization.Message.Commandblock>builder()
                .sender(fPlayer)
                .format(localization -> translationKey == MinecraftTranslationKey.ADV_MODE_NOT_ENABLED
                        ? localization.getNotEnabled()
                        : Strings.CS.replace(localization.getFormat(), "<command>", StringUtils.defaultString(command))
                )
                .command(command)
                .translationKey(translationKey)
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}