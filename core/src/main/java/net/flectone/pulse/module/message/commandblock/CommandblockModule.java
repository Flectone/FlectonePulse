package net.flectone.pulse.module.message.commandblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.commandblock.listener.CommandBlockPulseListener;
import net.flectone.pulse.module.message.commandblock.model.CommandBlockMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandblockModule extends AbstractModuleLocalization<Localization.Message.Commandblock> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(CommandBlockPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMANDBLOCK;
    }

    @Override
    public Message.Commandblock config() {
        return fileResolver.getMessage().getCommandblock();
    }

    @Override
    public Permission.Message.Commandblock permission() {
        return fileResolver.getPermission().getMessage().getCommandblock();
    }

    @Override
    public Localization.Message.Commandblock localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getCommandblock();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, @Nullable String command) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(CommandBlockMetadata.<Localization.Message.Commandblock>builder()
                .sender(fPlayer)
                .format(localization -> translationKey == MinecraftTranslationKey.ADV_MODE_NOT_ENABLED
                        ? localization.getNotEnabled()
                        : localization.getFormat()
                )
                .command(command)
                .translationKey(translationKey)
                .range(config().getRange())
                .destination(config().getDestination())
                .tagResolvers(fReceiver -> new TagResolver[]{
                        TagResolver.resolver("command", (argumentQueue, context) ->
                                Tag.selfClosingInserting(Component.text(StringUtils.defaultString(command)))
                        )
                })
                .sound(getModuleSound())
                .build()
        );
    }
}