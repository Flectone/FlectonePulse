package net.flectone.pulse.module.message.format.name;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Set;

@Singleton
public class NameModule extends AbstractModuleMessage<Localization.Message.Format.Name> implements MessageProcessor {

    private final Message.Format.Name message;
    private final Permission.Message.Format.Name permission;
    private final Permission.Message.Format formatPermission;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public NameModule(FileResolver fileResolver,
                      IntegrationModule integrationModule,
                      PermissionChecker permissionChecker,
                      MessagePipeline messagePipeline,
                      MessageProcessRegistry messageProcessRegistry,
                      PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getFormat().getName_());

        this.message = fileResolver.getMessage().getFormat().getName_();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getName_();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(150, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (checkModulePredicates(sender)) return;

        FEntity receiver = messageContext.getReceiver();

        if (isInvisible(sender)) {
            messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.DISPLAY_NAME, MessagePipeline.ReplacementTag.PLAYER), (argumentQueue, context) -> {
                String formatInvisible = resolveLocalization(receiver).getInvisible();
                Component name = messagePipeline.builder(sender, receiver, formatInvisible)
                        .build();

                return Tag.selfClosingInserting(name);
            });

            return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.CONSTANT, (argumentQueue, context) -> {
            String constantName = sender.getConstantName();
            if (constantName != null && constantName.isEmpty()) {
                return Tag.preProcessParsed(constantName);
            }

            if (constantName == null) {
                constantName = resolveLocalization(sender).getConstant();
            }

            if (constantName.isEmpty()) {
                return Tag.selfClosingInserting(Component.empty());
            }

            return Tag.preProcessParsed(messagePipeline.builder(sender, constantName).defaultSerializerBuild());
        });

        if (!(sender instanceof FPlayer fPlayer)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) ->
                    Tag.preProcessParsed(resolveLocalization(receiver).getEntity()
                            .replace("<name>", sender.getName())
                            .replace("<type>", sender.getType())
                            .replace("<uuid>", sender.getUuid().toString())
                    ));
            return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) -> {
            if (fPlayer.isUnknown()) {
                return Tag.preProcessParsed(resolveLocalization(receiver).getUnknown()
                        .replace("<name>", fPlayer.getName())
                );
            }

            String displayName = resolveLocalization(receiver).getDisplay();
            Component name = messagePipeline.builder(sender, receiver, displayName)
                    .build();

            return Tag.selfClosingInserting(name);
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.VAULT_PREFIX, (argumentQueue, context) -> {
            String prefix = integrationModule.getPrefix(fPlayer);
            if (prefix == null || prefix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, receiver, prefix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.VAULT_SUFFIX, (argumentQueue, context) -> {
            String suffix = integrationModule.getSuffix(fPlayer);
            if (suffix == null || suffix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, receiver, suffix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });

        if (messageContext.isPlayer()) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER, (argumentQueue, context) ->
                    Tag.preProcessParsed(fPlayer.getName())
            );
        }
    }

    private boolean isInvisible(FEntity entity) {
        return message.isShouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionTypes.INVISIBILITY);
    }
}
