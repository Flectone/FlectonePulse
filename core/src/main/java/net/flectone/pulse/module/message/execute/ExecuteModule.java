package net.flectone.pulse.module.message.execute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.execute.listener.ExecutePulseListener;
import net.flectone.pulse.module.message.execute.model.ExecuteMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

@Singleton
public class ExecuteModule extends AbstractModuleLocalization<Localization.Message.Execute> {

    private final Message.Execute message;
    private final Permission.Message.Execute permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ExecuteModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getExecute(), MessageType.EXECUTE);

        this.message = fileResolver.getMessage().getExecute();
        this.permission = fileResolver.getPermission().getMessage().getExecute();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ExecutePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, @Nullable String count) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ExecuteMetadata.<Localization.Message.Execute>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(string -> Strings.CS.replace(
                        translationKey == MinecraftTranslationKey.COMMANDS_EXECUTE_CONDITIONAL_PASS ? string.getFormatPass() : string.getFormatPassCount(),
                        "<count>",
                        StringUtils.defaultString(count)
                ))
                .destination(message.getDestination())
                .sound(getModuleSound())
                .count(count)
                .translationKey(translationKey)
                .build()
        );
    }
}