package net.flectone.pulse.module.message.format.object;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.object.listener.MinecraftPulseObjectListener;
import net.flectone.pulse.module.message.format.object.texture.MinecraftTextureService;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.parser.string.UUIDParser;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.checker.ValidNameChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class MinecraftObjectModule extends ObjectModule {

    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MinecraftSkinService skinService;
    private final MinecraftTextureService textureService;
    private final FPlayerService fPlayerService;
    private final MinecraftPacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;
    private final UUIDParser uuidParser;
    private final ValidNameChecker validNameChecker;
    private final boolean isNewerThanOrEqualsV_1_21_9;

    @Inject
    public MinecraftObjectModule(FileFacade fileFacade,
                                 ListenerRegistry listenerRegistry,
                                 PermissionChecker permissionChecker,
                                 MinecraftSkinService skinService,
                                 MinecraftTextureService textureService,
                                 FPlayerService fPlayerService,
                                 MinecraftPacketProvider packetProvider,
                                 IntegrationModule integrationModule,
                                 PlatformPlayerAdapter platformPlayerAdapter,
                                 ModuleController moduleController,
                                 MessagePipeline messagePipeline,
                                 UUIDParser uuidParser,
                                 ValidNameChecker validNameChecker,
                                 @Named("isNewerThanOrEqualsV_1_21_9") boolean isNewerThanOrEqualsV1219,
                                 SocialService socialService) {
        super(fileFacade, socialService);

        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
        this.skinService = skinService;
        this.textureService = textureService;
        this.fPlayerService = fPlayerService;
        this.packetProvider = packetProvider;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.moduleController = moduleController;
        this.messagePipeline = messagePipeline;
        this.uuidParser = uuidParser;
        this.validNameChecker = validNameChecker;
        this.isNewerThanOrEqualsV_1_21_9 = isNewerThanOrEqualsV1219;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseObjectListener.class);

        if (config().textureTag().enable()) {
            textureService.reload();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        textureService.terminateMineskin();
    }

    public MessageContext addPlayerHeadTag(MessageContext messageContext) {
        if (!config().playerHeadTag().enable()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            if (moduleController.isDisabledFor(this, sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().playerHeadTag())) return messageContext;
        }

        return messageContext.addTagResolvers(
                messagePipeline.resolver(MessagePipeline.ReplacementTag.PLAYER_HEAD.getTagName(), (argumentQueue, _) ->
                        createPlayerHeadTag(
                                messageContext,
                                localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                ),
                messagePipeline.resolver(MessagePipeline.ReplacementTag.PLAYER_HEAD_OR.getTagName(), (argumentQueue, _) ->
                        createPlayerHeadTag(
                                messageContext,
                                argumentQueue.hasNext() ? argumentQueue.pop().value() : localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                )
        );
    }

    private Tag createPlayerHeadTag(MessageContext messageContext,
                                    String defaultValue,
                                    ArgumentQueue argumentQueue) {
        if (config().playerHeadTag().hideInvisiblePlayerHead()
                && !messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)
                && platformPlayerAdapter.hasPotionEffect(messageContext.sender(), PotionUtil.INVISIBILITY_POTION_NAME)) return MessagePipeline.ReplacementTag.emptyTag();

        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultValue, config().playerHeadTag().needExtraSpace(), true);
        if (receiverVersionTag != null) return receiverVersionTag;

        PlayerHeadObjectContents.Builder playerHeadBuilder = ObjectContents.playerHead();

        String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;

        // I think a player name "true" or "false" is not possible on the server
        if (playerHead == null || "true".equals(playerHead) || "false".equals(playerHead)) {
            playerHeadBuilder.hat(!"false".equals(playerHead));

            FEntity sender = messageContext.sender();
            applyFPlayerProfileProperty(sender, playerHeadBuilder, builder -> builder.name(sender.name()));

            Component playerHeadComponent = Component.object().contents(playerHeadBuilder.build()).build();
            return applyDefaultFormatting(messageContext, playerHeadComponent, config().playerHeadTag().needExtraSpace());
        }

        playerHeadBuilder.hat(!argumentQueue.hasNext() || Boolean.parseBoolean(argumentQueue.pop().value()));

        // first check valid player name
        if (validNameChecker.check(playerHead)) {
            // try load this player
            FPlayer fPlayer = fPlayerService.getFPlayer(playerHead);

            // apply custom property
            applyFPlayerProfileProperty(fPlayer, playerHeadBuilder, builder -> builder.name(playerHead));
        } else {
            // second check player uuid
            UUID playerHeadUUID = uuidParser.parse(playerHead);
            if (playerHeadUUID != null) {
                // try load this player
                FPlayer fPlayer = fPlayerService.getFPlayer(playerHeadUUID);

                // apply custom property
                applyFPlayerProfileProperty(fPlayer, playerHeadBuilder, builder -> builder.id(playerHeadUUID));
            } else {
                // or insert value to textures
                playerHeadBuilder.profileProperty(PlayerHeadObjectContents.property("textures", playerHead));
            }
        }

        Component playerHeadComponent = Component.object().contents(playerHeadBuilder.build()).build();

        return applyDefaultFormatting(messageContext, playerHeadComponent, config().playerHeadTag().needExtraSpace());
    }

    private void applyFPlayerProfileProperty(FEntity fEntity,
                                             PlayerHeadObjectContents.Builder playerHeadBuilder,
                                             Consumer<PlayerHeadObjectContents.Builder> otherConsumer) {
        PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(fEntity);

        if (StringUtils.isNotEmpty(profileProperty.value())) {
            playerHeadBuilder.profileProperty(profileProperty);
        } else {
            otherConsumer.accept(playerHeadBuilder);
        }
    }

    public MessageContext addSpriteTag(MessageContext messageContext) {
        if (!config().spriteTag().enable()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            if (moduleController.isDisabledFor(this, sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().spriteTag())) return messageContext;
        }

        return messageContext.addTagResolvers(
                messagePipeline.resolver(MessagePipeline.ReplacementTag.SPRITE.getTagName(), (argumentQueue, _) ->
                        createSpriteTag(
                                messageContext,
                                localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                ),
                messagePipeline.resolver(MessagePipeline.ReplacementTag.SPRITE_OR.getTagName(), (argumentQueue, _) ->
                        createSpriteTag(
                                messageContext,
                                argumentQueue.hasNext() ? argumentQueue.pop().value() : localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                )
        );
    }

    public MessageContext addTextureTag(MessageContext messageContext) {
        if (!config().textureTag().enable()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            if (moduleController.isDisabledFor(this, sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().textureTag())) return messageContext;
        }

        return messageContext.addTagResolvers(
                messagePipeline.resolver(MessagePipeline.ReplacementTag.TEXTURE.getTagName(), (argumentQueue, _) ->
                        createTextureTag(
                                messageContext,
                                localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                ),
                messagePipeline.resolver(MessagePipeline.ReplacementTag.TEXTURE_OR.getTagName(), (argumentQueue, _) ->
                        createTextureTag(
                                messageContext,
                                argumentQueue.hasNext() ? argumentQueue.pop().value() : localization(messageContext.receiver()).defaultSymbol(),
                                argumentQueue
                        )
                )
        );
    }

    public Tag createTextureTag(MessageContext messageContext,
                                String defaultValue,
                                ArgumentQueue argumentQueue) {
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultValue, config().textureTag().needExtraSpace(), false);
        if (receiverVersionTag != null) return receiverVersionTag;
        if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

        String textureName = argumentQueue.pop().value();
        Component textureComponent = textureService.getTexture(textureName);
        if (textureComponent == null) return MessagePipeline.ReplacementTag.emptyTag();

        return applyDefaultFormatting(messageContext, textureComponent, config().textureTag().needExtraSpace());
    }

    private Tag createSpriteTag(MessageContext messageContext,
                                String defaultValue,
                                ArgumentQueue argumentQueue) {
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultValue, config().spriteTag().needExtraSpace(), false);
        if (receiverVersionTag != null) return receiverVersionTag;
        if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

        Key sprite = Key.key(argumentQueue.pop().value());
        Tag.Argument secondArgument = argumentQueue.peek();

        SpriteObjectContents spriteObjectContents = secondArgument == null
                ? ObjectContents.sprite(sprite)
                : ObjectContents.sprite(sprite, Key.key(secondArgument.value())); // first atlas, second sprite

        Component spriteComponent = Component.object().contents(spriteObjectContents).build();

        return applyDefaultFormatting(messageContext, spriteComponent, config().spriteTag().needExtraSpace());
    }

    @Nullable
    private Tag checkAndGetReceiverTag(MessageContext messageContext,
                                       String defaultValue,
                                       boolean needExtraSpace,
                                       boolean skipFormattingForOldVersion) {
        // ViaVersion will not be able to process messages that contain Object on older versions
        if (!isNewerThanOrEqualsV_1_21_9) {
            return skipFormattingForOldVersion
                    ? MessagePipeline.ReplacementTag.emptyTag()
                    : applyDefaultFormatting(messageContext, defaultValue, needExtraSpace);
        }

        // return default formatting
        if (messageContext.isFlag(MessageFlag.OBJECT_DEFAULT_VALUE)) {
            return applyDefaultFormatting(messageContext, defaultValue, needExtraSpace);
        }

        // continue building
        if (!messageContext.isFlag(MessageFlag.OBJECT_RECEIVER_VALIDATION)) {
            return null;
        }

        FPlayer fReceiver = messageContext.receiver();

        // return default formatting
        if (fReceiver.isUnknown() || fReceiver.isConsole()) {
            return applyDefaultFormatting(messageContext, defaultValue, needExtraSpace);
        }

        // get user
        User user = packetProvider.getUser(fReceiver);

        // I think null user == Status (MOTD) viewer
        if (user == null) {
            return null;
        }

        // check player version
        if (user.getPacketVersion().isNewerThanOrEquals(ClientVersion.V_1_21_9)) {
            // bedrock player does not support object component
            if (integrationModule.isBedrockPlayer(fReceiver)) {
                return applyDefaultFormatting(messageContext, defaultValue, needExtraSpace);
            }

            // continue building
            return null;
        }

        // for old client
        return MessagePipeline.ReplacementTag.emptyTag();
    }

    private Tag applyDefaultFormatting(MessageContext messageContext, String argument, boolean needExtraSpace) {
        if (StringUtils.isEmpty(argument)) return MessagePipeline.ReplacementTag.emptyTag();

        return applyDefaultFormatting(messageContext, buildArgument(messageContext, argument), needExtraSpace);
    }

    private Tag applyDefaultFormatting(MessageContext messageContext, Component component, boolean needExtraSpace) {
        if (!Component.IS_NOT_EMPTY.test(component)) return MessagePipeline.ReplacementTag.emptyTag();

        boolean isPlayerMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);

        if (!isPlayerMessage && needExtraSpace) {
            component = component.append(Component.space());
        }

        if (isPlayerMessage) {
            component = component.color(NamedTextColor.WHITE);
        }

        return isPlayerMessage ? Tag.selfClosingInserting(component) : Tag.inserting(component);
    }

    private Component buildArgument(MessageContext messageContext, String argument) {
        return messagePipeline.build(MessageContext.builder()
                .sender(messageContext.sender())
                .receiver(messageContext.receiver())
                .message(argument)
                .flags(messageContext.flags())
                .build()
        );
    }
}
