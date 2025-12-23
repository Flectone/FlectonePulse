package net.flectone.pulse.module.message.format.names;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.names.listener.NamesPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamesModule extends AbstractModuleLocalization<Localization.Message.Format.Names> {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(NamesPulseListener.class);
    }

    @Override
    public Message.Format.Names config() {
        return fileFacade.message().format().names();
    }

    @Override
    public Permission.Message.Format.Names permission() {
        return fileFacade.permission().message().format().names();
    }

    @Override
    public MessageType messageType() {
        return MessageType.NAME;
    }

    @Override
    public Localization.Message.Format.Names localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().names();
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;
        if (isModuleDisabledFor(sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();

        if (!(sender instanceof FPlayer fPlayer)) {
            return messageContext.addTagResolver(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) -> {
                Localization.Message.Format.Names localizationName = localization(receiver);

                Component showEntityName = sender.getShowEntityName();
                if (showEntityName == null) {
                    MessageContext displayContext = messagePipeline.createContext(sender, receiver,
                            StringUtils.replaceEach(
                                    sender.getType().equals(FEntity.UNKNOWN_TYPE) ? localizationName.unknown() : localizationName.entity(),
                                    new String[]{"<name>", "<type>", "<uuid>"},
                                    new String[]{"<lang:'" + sender.getType() + "'>", sender.getType(), sender.getUuid().toString()}
                            )
                    );
                    Component displayName = messagePipeline.build(displayContext);

                    return Tag.selfClosingInserting(displayName);
                }

                MessageContext displayContext = messagePipeline.createContext(sender, receiver,
                                sender.getType().equals(FEntity.UNKNOWN_TYPE)
                                        ? localizationName.unknown()
                                        : StringUtils.replaceEach(
                                        localizationName.entity(),
                                        new String[]{"<type>", "<uuid>"},
                                        new String[]{sender.getType(), sender.getUuid().toString()}
                                )
                        )
                        .addTagResolver(TagResolver.resolver("name", (args, ctx) -> Tag.selfClosingInserting(showEntityName)));

                Component displayName = messagePipeline.build(displayContext);
                return Tag.selfClosingInserting(displayName);
            });
        }

        return messageContext
                .addTagResolvers(
                        TagResolver.resolver(MessagePipeline.ReplacementTag.CONSTANT.getTagName(), (argumentQueue, context) -> {
                            String constantName = fPlayer.getConstantName();
                            if (constantName == null) {
                                constantName = localization(fPlayer).constant();
                            }

                            if (constantName.isEmpty()) {
                                return Tag.selfClosingInserting(Component.empty());
                            }

                            MessageContext constantContext = messagePipeline.createContext(fPlayer, constantName);
                            String serialized = messagePipeline.buildDefault(constantContext);
                            return Tag.preProcessParsed(serialized);
                        }),
                        TagResolver.resolver(MessagePipeline.ReplacementTag.DISPLAY_NAME.getTagName(), (argumentQueue, context) -> {
                            Localization.Message.Format.Names localization = localization(receiver);
                            String displayName = fPlayer.isUnknown()
                                    ? Strings.CS.replace(localization.unknown(), "<name>", fPlayer.getName())
                                    : localization.display();

                            MessageContext displayContext = messagePipeline.createContext(sender, receiver, displayName);
                            Component displayNameComponent = messagePipeline.build(displayContext);

                            return Tag.selfClosingInserting(displayNameComponent);
                        }),
                        TagResolver.resolver(MessagePipeline.ReplacementTag.VAULT_PREFIX.getTagName(), (argumentQueue, context) -> {
                            String prefix = integrationModule.getPrefix(fPlayer);
                            if (StringUtils.isEmpty(prefix)) return Tag.selfClosingInserting(Component.empty());

                            MessageContext prefixContext = messagePipeline.createContext(fPlayer, receiver, prefix);
                            String text = messagePipeline.buildDefault(prefixContext);
                            return Tag.preProcessParsed(text);
                        }),
                        TagResolver.resolver(MessagePipeline.ReplacementTag.VAULT_SUFFIX.getTagName(), (argumentQueue, context) -> {
                            String suffix = integrationModule.getSuffix(fPlayer);
                            if (StringUtils.isEmpty(suffix)) return Tag.selfClosingInserting(Component.empty());

                            MessageContext suffixContext = messagePipeline.createContext(fPlayer, receiver, suffix);
                            String text = messagePipeline.buildDefault(suffixContext);
                            return Tag.preProcessParsed(text);
                        }),
                        TagResolver.resolver(MessagePipeline.ReplacementTag.PLAYER.getTagName(), (argumentQueue, context) ->
                                Tag.preProcessParsed(fPlayer.getName())
                        )
                );
    }

    public MessageContext addInvisibleTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;
        if (isModuleDisabledFor(sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        return messageContext.addTagResolver(Set.of(MessagePipeline.ReplacementTag.DISPLAY_NAME, MessagePipeline.ReplacementTag.PLAYER),
                (argumentQueue, context) -> {
                    String formatInvisible = localization(receiver).invisible();
                    MessageContext invisibleContext = messagePipeline.createContext(sender, receiver, formatInvisible);
                    Component name = messagePipeline.build(invisibleContext);

                    return Tag.selfClosingInserting(name);
                }
        );
    }
}