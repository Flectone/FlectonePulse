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
import net.flectone.pulse.module.message.format.object.listener.ObjectPulseListener;
import net.flectone.pulse.module.message.format.object.texture.TextureService;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Singleton
public class MinecraftObjectModule extends ObjectModule {

    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MinecraftSkinService skinService;
    private final TextureService textureService;
    private final PacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModuleController moduleController;
    private final boolean isNewerThanOrEqualsV_1_21_9;

    @Inject
    public MinecraftObjectModule(FileFacade fileFacade,
                                 ListenerRegistry listenerRegistry,
                                 PermissionChecker permissionChecker,
                                 MinecraftSkinService skinService,
                                 TextureService textureService,
                                 PacketProvider packetProvider,
                                 IntegrationModule integrationModule,
                                 PlatformPlayerAdapter platformPlayerAdapter,
                                 ModuleController moduleController,
                                 @Named("isNewerThanOrEqualsV_1_21_9") boolean isNewerThanOrEqualsV1219) {
        super(fileFacade);

        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
        this.skinService = skinService;
        this.textureService = textureService;
        this.packetProvider = packetProvider;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.moduleController = moduleController;
        this.isNewerThanOrEqualsV_1_21_9 = isNewerThanOrEqualsV1219;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(ObjectPulseListener.class);

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
                TagResolver.resolver(MessagePipeline.ReplacementTag.PLAYER_HEAD.getTagName(), ((argumentQueue, context) ->
                        createPlayerHeadTag(messageContext, getDefaultObjectComponent(messageContext), argumentQueue))
                ),
                TagResolver.resolver(MessagePipeline.ReplacementTag.PLAYER_HEAD_OR.getTagName(), ((argumentQueue, context) -> {
                    Component defaultComponent = argumentQueue.hasNext() ? Component.text(argumentQueue.pop().value()) : getDefaultObjectComponent(messageContext);
                    return createPlayerHeadTag(messageContext, defaultComponent, argumentQueue);
                }))
        );
    }

    private Tag createPlayerHeadTag(MessageContext messageContext, Component defaultComponent, ArgumentQueue argumentQueue) {
        if (config().playerHeadTag().hideInvisiblePlayerHead()
                && !messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)
                && platformPlayerAdapter.hasPotionEffect(messageContext.sender(), PotionUtil.INVISIBILITY_POTION_NAME)) return MessagePipeline.ReplacementTag.emptyTag();

        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultComponent, config().playerHeadTag().needExtraSpace());
        if (receiverVersionTag != null) return receiverVersionTag;

        PlayerHeadObjectContents.Builder playerHeadBuilder = ObjectContents.playerHead();

        FEntity sender = messageContext.sender();
        String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
        if (playerHead == null || playerHead.length() > 16) {
            PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(sender);

            Component playerHeadComponent = StringUtils.isNotEmpty(profileProperty.value())
                    ? Component.object().contents(playerHeadBuilder.profileProperty(profileProperty).build()).build()
                    : Component.object().contents(playerHeadBuilder.name(sender.name()).build()).build();

            return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, playerHeadComponent, config().playerHeadTag().needExtraSpace()));
        }

        try {
            playerHeadBuilder.id(UUID.fromString(playerHead));
        } catch (IllegalArgumentException e) {
            playerHeadBuilder.name(playerHead);
        }

        boolean showPlayerHat = !argumentQueue.hasNext() || Boolean.parseBoolean(argumentQueue.pop().value());

        Component playerHeadComponent = Component.object().contents(
                playerHeadBuilder
                        .hat(showPlayerHat)
                        .build()
        ).build();

        return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, playerHeadComponent, config().playerHeadTag().needExtraSpace()));
    }

    public MessageContext addSpriteTag(MessageContext messageContext) {
        if (!config().spriteTag().enable()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            if (moduleController.isDisabledFor(this, sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().spriteTag())) return messageContext;
        }

        return messageContext.addTagResolvers(
                TagResolver.resolver(MessagePipeline.ReplacementTag.SPRITE.getTagName(), ((argumentQueue, context) ->
                        createSpriteTag(messageContext, getDefaultObjectComponent(messageContext), argumentQueue))
                ),
                TagResolver.resolver(MessagePipeline.ReplacementTag.SPRITE_OR.getTagName(), (argumentQueue, context) -> {
                    Component defaultComponent = argumentQueue.hasNext() ? Component.text(argumentQueue.pop().value()) : getDefaultObjectComponent(messageContext);
                    return createSpriteTag(messageContext, defaultComponent, argumentQueue);
                })
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
                TagResolver.resolver(MessagePipeline.ReplacementTag.TEXTURE.getTagName(), ((argumentQueue, context) ->
                        createTextureTag(messageContext, getDefaultObjectComponent(messageContext), argumentQueue))
                ),
                TagResolver.resolver(MessagePipeline.ReplacementTag.TEXTURE_OR.getTagName(), (argumentQueue, context) -> {
                    Component defaultComponent = argumentQueue.hasNext() ? Component.text(argumentQueue.pop().value()) : getDefaultObjectComponent(messageContext);
                    return createTextureTag(messageContext, defaultComponent, argumentQueue);
                })
        );
    }

    public Tag createTextureTag(MessageContext messageContext, Component defaultComponent, ArgumentQueue argumentQueue) {
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultComponent, config().textureTag().needExtraSpace());
        if (receiverVersionTag != null) return receiverVersionTag;
        if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

        String textureName = argumentQueue.pop().value();
        Component textureComponent = textureService.getTexture(textureName);
        if (textureComponent == null) return MessagePipeline.ReplacementTag.emptyTag();

        return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, textureComponent, config().textureTag().needExtraSpace()));
    }

    private Tag createSpriteTag(MessageContext messageContext, Component defaultComponent, ArgumentQueue argumentQueue) {
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultComponent, config().spriteTag().needExtraSpace());
        if (receiverVersionTag != null) return receiverVersionTag;
        if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

        Key sprite = Key.key(argumentQueue.pop().value());
        Tag.Argument secondArgument = argumentQueue.peek();

        SpriteObjectContents spriteObjectContents = secondArgument == null
                ? ObjectContents.sprite(sprite)
                : ObjectContents.sprite(sprite, Key.key(secondArgument.value())); // first atlas, second sprite

        Component spriteComponent = Component.object().contents(spriteObjectContents).build();

        return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, spriteComponent, config().spriteTag().needExtraSpace()));
    }

    private Tag checkAndGetReceiverTag(MessageContext messageContext, Component defaultComponent, boolean needExtraSpace) {
        FPlayer fReceiver = messageContext.receiver();

        if (messageContext.isFlag(MessageFlag.OBJECT_DEFAULT_VALUE)) {
            return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, defaultComponent, needExtraSpace));
        }

        if (!messageContext.isFlag(MessageFlag.OBJECT_RECEIVER_VALIDATION) && isNewerThanOrEqualsV_1_21_9) {
            return null;
        }

        if (fReceiver.isUnknown()) {
            if (isNewerThanOrEqualsV_1_21_9) {
                return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, defaultComponent, needExtraSpace));
            }

            return MessagePipeline.ReplacementTag.emptyTag();
        }

        // get user
        User user = packetProvider.getUser(fReceiver);

        // I think null user == Status (MOTD) viewer
        if (user == null) return null;

        // check player version
        if (user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_9)) {
            // bedrock player does not support object component
            if (integrationModule.isBedrockPlayer(fReceiver)) {
                return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, defaultComponent, needExtraSpace));
            }

            // continue building
            return null;
        }

        // for old client
        return MessagePipeline.ReplacementTag.emptyTag();
    }

    private Component getDefaultObjectComponent(MessageContext messageContext) {
        return Component.text(localization(messageContext.receiver()).defaultSymbol());
    }

    private Component addDefaultParametersIfNeeded(MessageContext messageContext, Component component, boolean needExtraSpace) {
        if (!Component.IS_NOT_EMPTY.test(component)) return Component.empty();

        if (!messageContext.isFlag(MessageFlag.PLAYER_MESSAGE) && needExtraSpace) {
            component = component.append(Component.space());
        }

        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            component = component.color(NamedTextColor.WHITE);
        }

        return component;
    }
}
