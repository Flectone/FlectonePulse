package net.flectone.pulse.module.message.format.object;

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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;

import java.util.UUID;

@Singleton
public class ObjectModule extends AbstractModule {

    // ANSI serializer converts object components to a string like "[TheFaser head]" or "[item/diamond_sword]"
    // this is too long, so we replace it with "☐"
    private final Component DEFAULT_OBJECT_COMPONENT = Component.text("☐");

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final SkinService skinService;
    private final PacketProvider packetProvider;

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
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (!permissionChecker.check(sender, permission().getPlayerHead())) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PLAYER_HEAD, ((argumentQueue, context) -> {
            FPlayer fReceiver = messageContext.getReceiver();
            if (fReceiver.isUnknown() || !packetProvider.isNewerThanOrEquals(fReceiver, ClientVersion.V_1_21_9)) return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT);

            PlayerHeadObjectContents.Builder playerHeadBuilderComponent = ObjectContents.playerHead();

            String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            if (playerHead == null) {
                PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfileProperty(sender);
                if (profileProperty == null) return Tag.selfClosingInserting(Component.empty());

                return Tag.selfClosingInserting(Component.object().contents(
                                playerHeadBuilderComponent
                                        .name(sender.getName())
                                        .id(sender.getUuid())
                                        .profileProperty(profileProperty)
                                        .build()
                        )
                );
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
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (!permissionChecker.check(sender, permission().getSprite())) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SPRITE, ((argumentQueue, context) -> {
            FPlayer fReceiver = messageContext.getReceiver();
            if (fReceiver.isUnknown() || !packetProvider.isNewerThanOrEquals(fReceiver, ClientVersion.V_1_21_9)) return Tag.selfClosingInserting(DEFAULT_OBJECT_COMPONENT);
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            Key sprite = Key.key(argumentQueue.pop().value());
            Tag.Argument secondArgument = argumentQueue.peek();

            SpriteObjectContents spriteObjectContents = secondArgument == null
                    ? ObjectContents.sprite(sprite)
                    : ObjectContents.sprite(sprite, Key.key(secondArgument.value())); // first atlas, second sprite

            return Tag.selfClosingInserting(Component.object().contents(spriteObjectContents));
        }));
    }
}
