package net.flectone.pulse.module.message.format.object;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.object.listener.ObjectPulseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
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
public class ObjectModule extends AbstractModule {

    // ANSI serializer converts object components to a string like "[TheFaser head]" or "[item/diamond_sword]"
    // this is too long, so we replace it with "☐"
    private final Component DEFAULT_OBJECT_COMPONENT = Component.text("☐").color(NamedTextColor.WHITE);

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final SkinService skinService;
    private final PacketProvider packetProvider;
    private final boolean isNewerThanOrEqualsV_1_21_9;

    @Inject
    public ObjectModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry,
                        PermissionChecker permissionChecker,
                        SkinService skinService,
                        PacketProvider packetProvider) {
        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
        this.skinService = skinService;
        this.packetProvider = packetProvider;
        this.isNewerThanOrEqualsV_1_21_9 = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_9);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().getPlayerHead());
        registerPermission(permission().getSprite());

        listenerRegistry.register(ObjectPulseListener.class);
    }

    @Override
    public Message.Format.Object config() {
        return fileResolver.getMessage().getFormat().getObject();
    }

    @Override
    public Permission.Message.Format.Object permission() {
        return fileResolver.getPermission().getMessage().getFormat().getObject();
    }

    public void addPlayerHeadTag(MessageContext messageContext) {
        if (!config().isPlayerHead()) return;

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return;
            if (!permissionChecker.check(sender, permission().getPlayerHead())) return;
        }

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER_HEAD, ((argumentQueue, context) -> {
            Tag receiverVersionTag = checkAndGetReceiverTag(messageContext);
            if (receiverVersionTag != null) return receiverVersionTag;

            PlayerHeadObjectContents.Builder playerHeadBuilderComponent = ObjectContents.playerHead();

            String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            if (playerHead == null) {
                PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(sender);
                if (profileProperty == null) return Tag.selfClosingInserting(Component.empty());

                Component playerHeadComponent = Component.object().contents(
                        playerHeadBuilderComponent
                                .name(sender.getName())
                                .id(sender.getUuid())
                                .profileProperty(profileProperty)
                                .build()
                ).build().color(NamedTextColor.WHITE);

                if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
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
            playerHeadBuilderComponent.hat(showPlayerHat);

            return Tag.selfClosingInserting(Component.object().contents(playerHeadBuilderComponent.build()));
        }));
    }

    public void addSpriteTag(MessageContext messageContext) {
        if (!config().isSprite()) return;

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return;
            if (!permissionChecker.check(sender, permission().getSprite())) return;
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

            if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
                spriteComponent = spriteComponent.append(Component.space());
            }

            return Tag.selfClosingInserting(spriteComponent);
        }));
    }

    private Tag checkAndGetReceiverTag(MessageContext messageContext) {
        FPlayer fReceiver = messageContext.getReceiver();

        // check console version
        if (isNewerThanOrEqualsV_1_21_9 && fReceiver.isUnknown()) {
            if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
                return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT);
            }

            return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT.append(Component.space()));
        } else if (fReceiver.isUnknown()) {
            return Tag.selfClosingInserting(Component.empty());
        }

        // check player version
        if (packetProvider.isNewerThanOrEquals(fReceiver, ClientVersion.V_1_21_9)) {
            // continue building
            return null;
        }

        // return empty for old client
        return Tag.selfClosingInserting(Component.empty());
    }
}
