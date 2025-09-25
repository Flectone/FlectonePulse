package net.flectone.pulse.module.message.execute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
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

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ExecuteModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(MessageType.EXECUTE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(ExecutePulseListener.class);
    }

    @Override
    public Message.Execute config() {
        return fileResolver.getMessage().getExecute();
    }

    @Override
    public Permission.Message.Execute permission() {
        return fileResolver.getPermission().getMessage().getExecute();
    }

    @Override
    public Localization.Message.Execute localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getExecute();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, @Nullable String count) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ExecuteMetadata.<Localization.Message.Execute>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> Strings.CS.replace(
                        translationKey == MinecraftTranslationKey.COMMANDS_EXECUTE_CONDITIONAL_PASS ? localization.getPass() : localization.getPassCount(),
                        "<count>",
                        StringUtils.defaultString(count)
                ))
                .destination(config().getDestination())
                .sound(getModuleSound())
                .count(count)
                .translationKey(translationKey)
                .build()
        );
    }
}