package net.flectone.pulse.module.message.format.object;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.object.listener.ObjectPulseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectModule extends AbstractModule {

    // ANSI serializer converts object components to a string like "[TheFaser head]" or "[item/diamond_sword]"
    // this is too long, so we replace it with "☐"
    private static final Component DEFAULT_OBJECT_COMPONENT = Component.text("☐").color(NamedTextColor.WHITE);

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final SkinService skinService;
    private final PacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final @Named("isNewerThanOrEqualsV_1_21_9") boolean isNewerThanOrEqualsV_1_21_9;

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().playerHead());
        registerPermission(permission().sprite());

        listenerRegistry.register(ObjectPulseListener.class);
    }

    @Override
    public Message.Format.Object config() {
        return fileFacade.message().format().object();
    }

    @Override
    public Permission.Message.Format.Object permission() {
        return fileFacade.permission().message().format().object();
    }

    public void addPlayerHeadTag(MessageContext messageContext) {
        if (!messageContext.getMessage().contains(MessagePipeline.ReplacementTag.PLAYER_HEAD.getTagName())) return;
        if (!config().playerHead()) return;

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return;
            if (!permissionChecker.check(sender, permission().playerHead())) return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER_HEAD, ((argumentQueue, context) -> {
            Tag receiverVersionTag = checkAndGetReceiverTag(messageContext);
            if (receiverVersionTag != null) return receiverVersionTag;

            PlayerHeadObjectContents.Builder playerHeadBuilderComponent = ObjectContents.playerHead();

            String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            if (playerHead == null || playerHead.length() > 16) {
                PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(sender);

                Component playerHeadComponent = Component.object().contents(
                        playerHeadBuilderComponent
                                .name(sender.getName())
                                .id(sender.getUuid())
                                .profileProperty(profileProperty)
                                .build()
                ).build().color(NamedTextColor.WHITE);

                if (!messageContext.isFlag(MessageFlag.USER_MESSAGE) && config().needExtraSpace()) {
                    playerHeadComponent = playerHeadComponent.append(Component.space());
                }

                return Tag.selfClosingInserting(playerHeadComponent);
            }

            try {
                playerHeadBuilderComponent.id(UUID.fromString(playerHead));
            } catch (IllegalArgumentException e) {
                playerHeadBuilderComponent.name(playerHead);
            }

            boolean showPlayerHat = !argumentQueue.hasNext() || Boolean.parseBoolean(argumentQueue.pop().value());

            Component playerHeadComponent = Component.object().contents(
                        playerHeadBuilderComponent
                                .hat(showPlayerHat)
                                .build()
                    ).build()
                    .color(NamedTextColor.WHITE);

            return Tag.selfClosingInserting(playerHeadComponent);
        }));
    }

    public void addSpriteTag(MessageContext messageContext) {
        if (!messageContext.getMessage().contains(MessagePipeline.ReplacementTag.SPRITE.getTagName())) return;
        if (!config().sprite()) return;

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return;
            if (!permissionChecker.check(sender, permission().sprite())) return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SPRITE, ((argumentQueue, context) -> {
            Tag receiverVersionTag = checkAndGetReceiverTag(messageContext);
            if (receiverVersionTag != null) return receiverVersionTag;
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            Key sprite = Key.key(argumentQueue.pop().value());
            Tag.Argument secondArgument = argumentQueue.peek();

            SpriteObjectContents spriteObjectContents = secondArgument == null
                    ? ObjectContents.sprite(sprite)
                    : ObjectContents.sprite(sprite, Key.key(secondArgument.value())); // first atlas, second sprite

            Component spriteComponent = Component.object().contents(spriteObjectContents)
                    .build()
                    .color(NamedTextColor.WHITE);

            if (!messageContext.isFlag(MessageFlag.USER_MESSAGE) && config().needExtraSpace()) {
                spriteComponent = spriteComponent.append(Component.space());
            }

            return Tag.selfClosingInserting(spriteComponent);
        }));
    }

    private Tag checkAndGetReceiverTag(MessageContext messageContext) {
        FPlayer fReceiver = messageContext.getReceiver();

        // check console version
        if (isNewerThanOrEqualsV_1_21_9 && fReceiver.isUnknown()) {
            if (!messageContext.isFlag(MessageFlag.USER_MESSAGE) && config().needExtraSpace()) {
                return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT.append(Component.space()));
            }

            return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT);
        } else if (fReceiver.isUnknown()) {
            return Tag.selfClosingInserting(Component.empty());
        }

        // check player version
        if (packetProvider.isNewerThanOrEquals(fReceiver, ClientVersion.V_1_21_9)) {
            // bedrock player does not support object component
            if (integrationModule.isBedrockPlayer(fReceiver)) {
                return messageContext.isFlag(MessageFlag.USER_MESSAGE)
                        ? Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT)
                        : Tag.selfClosingInserting(Component.empty());
            }

            // continue building
            return null;
        }

        // return empty for old client
        return Tag.selfClosingInserting(Component.empty());
    }
}
