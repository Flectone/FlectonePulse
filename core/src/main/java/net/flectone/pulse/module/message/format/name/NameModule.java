package net.flectone.pulse.module.message.format.name;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.name.listener.NamePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Set;

@Singleton
public class NameModule extends AbstractModuleLocalization<Localization.Message.Format.Name> {

    private final Message.Format.Name message;
    private final Permission.Message.Format.Name permission;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public NameModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry,
                      IntegrationModule integrationModule,
                      MessagePipeline messagePipeline) {
        super(localization -> localization.getMessage().getFormat().getName_(), MessageType.NAME);

        this.message = fileResolver.getMessage().getFormat().getName_();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getName_();
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(NamePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void addTags(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;
        if (isModuleDisabledFor(sender)) return;

        FPlayer receiver = messageContext.getReceiver();

        if (!(sender instanceof FPlayer fPlayer)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) -> {
                String formatEntity = StringUtils.replaceEach(
                        resolveLocalization(receiver).getEntity(),
                        new String[]{"<name>", "<type>", "<uuid>"},
                        new String[]{sender.getName(), sender.getType(), sender.getUuid().toString()}
                );

                Component name = messagePipeline.builder(sender, receiver, formatEntity)
                        .build();

                return Tag.selfClosingInserting(name);
            });
            return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.CONSTANT, (argumentQueue, context) -> {
            String constantName = fPlayer.getConstantName();
            if (constantName == null) {
                constantName = resolveLocalization(fPlayer).getConstant();
            }

            if (constantName.isEmpty()) {
                return Tag.selfClosingInserting(Component.empty());
            }

            return Tag.preProcessParsed(messagePipeline.builder(fPlayer, constantName).defaultSerializerBuild());
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) -> {
            Localization.Message.Format.Name localization = resolveLocalization(receiver);

            String displayName = fPlayer.isUnknown()
                    ? Strings.CS.replace(localization.getUnknown(), "<name>", fPlayer.getName())
                    : localization.getDisplay();

            Component displayNameComponent = messagePipeline.builder(sender, receiver, displayName).build();

            return Tag.selfClosingInserting(displayNameComponent);
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.VAULT_PREFIX, (argumentQueue, context) -> {
            String prefix = integrationModule.getPrefix(fPlayer);
            if (StringUtils.isEmpty(prefix)) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, receiver, prefix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.VAULT_SUFFIX, (argumentQueue, context) -> {
            String suffix = integrationModule.getSuffix(fPlayer);
            if (StringUtils.isEmpty(suffix)) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, receiver, suffix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER, (argumentQueue, context) ->
                Tag.preProcessParsed(fPlayer.getName())
        );
    }

    public void addInvisibleTag(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;
        if (isModuleDisabledFor(sender)) return;

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.DISPLAY_NAME, MessagePipeline.ReplacementTag.PLAYER), (argumentQueue, context) -> {
            String formatInvisible = resolveLocalization(receiver).getInvisible();
            Component name = messagePipeline.builder(sender, receiver, formatInvisible)
                    .build();

            return Tag.selfClosingInserting(name);
        });
    }

}
