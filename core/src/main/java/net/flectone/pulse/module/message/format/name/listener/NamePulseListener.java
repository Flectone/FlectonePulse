package net.flectone.pulse.module.message.format.name.listener;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Set;

@Singleton
public class NamePulseListener implements PulseListener {

    private final Message.Format.Name message;
    private final Permission.Message.Format formatPermission;
    private final NameModule nameModule;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public NamePulseListener(FileResolver fileResolver,
                             NameModule nameModule,
                             PermissionChecker permissionChecker,
                             IntegrationModule integrationModule,
                             MessagePipeline messagePipeline,
                             PlatformPlayerAdapter platformPlayerAdapter) {
        this.message = fileResolver.getMessage().getFormat().getName_();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.nameModule = nameModule;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (nameModule.checkModulePredicates(sender)) return;

        FPlayer receiver = messageContext.getReceiver();

        if (isInvisible(sender)) {
            messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.DISPLAY_NAME, MessagePipeline.ReplacementTag.PLAYER), (argumentQueue, context) -> {
                String formatInvisible = nameModule.resolveLocalization(receiver).getInvisible();
                Component name = messagePipeline.builder(sender, receiver, formatInvisible)
                        .build();

                return Tag.selfClosingInserting(name);
            });

            return;
        }

        if (!(sender instanceof FPlayer fPlayer)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) ->
                    Tag.preProcessParsed(nameModule.resolveLocalization(receiver).getEntity()
                            .replace("<name>", sender.getName())
                            .replace("<type>", sender.getType())
                            .replace("<uuid>", sender.getUuid().toString())
                    ));
            return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.CONSTANT, (argumentQueue, context) -> {
            String constantName = fPlayer.getConstantName();
            if (constantName != null && constantName.isEmpty()) {
                return Tag.preProcessParsed(constantName);
            }

            if (constantName == null) {
                constantName = nameModule.resolveLocalization(fPlayer).getConstant();
            }

            if (constantName.isEmpty()) {
                return Tag.selfClosingInserting(Component.empty());
            }

            return Tag.preProcessParsed(messagePipeline.builder(fPlayer, constantName).defaultSerializerBuild());
        });

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DISPLAY_NAME, (argumentQueue, context) -> {
            if (fPlayer.isUnknown()) {
                return Tag.preProcessParsed(nameModule.resolveLocalization(receiver).getUnknown()
                        .replace("<name>", fPlayer.getName())
                );
            }

            String displayName = nameModule.resolveLocalization(receiver).getDisplay();
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

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER, (argumentQueue, context) ->
                Tag.preProcessParsed(fPlayer.getName())
        );
    }

    private boolean isInvisible(FEntity entity) {
        return message.isShouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionTypes.INVISIBILITY);
    }
}
