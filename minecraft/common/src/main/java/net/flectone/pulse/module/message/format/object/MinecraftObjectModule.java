package net.flectone.pulse.module.message.format.object;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.object.listener.ObjectPulseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
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
    private final PacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final boolean isNewerThanOrEqualsV_1_21_9;

    @Inject
    public MinecraftObjectModule(FileFacade fileFacade,
                                 ListenerRegistry listenerRegistry,
                                 PermissionChecker permissionChecker,
                                 MinecraftSkinService skinService,
                                 PacketProvider packetProvider,
                                 IntegrationModule integrationModule,
                                 @Named("isNewerThanOrEqualsV_1_21_9") boolean isNewerThanOrEqualsV1219) {
        super(fileFacade);

        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
        this.skinService = skinService;
        this.packetProvider = packetProvider;
        this.integrationModule = integrationModule;
        isNewerThanOrEqualsV_1_21_9 = isNewerThanOrEqualsV1219;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(ObjectPulseListener.class);
    }

    public MessageContext addPlayerHeadTag(MessageContext messageContext) {
        if (!config().playerHead()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().playerHead())) return messageContext;
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
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultComponent);
        if (receiverVersionTag != null) return receiverVersionTag;

        PlayerHeadObjectContents.Builder playerHeadBuilderComponent = ObjectContents.playerHead();

        FEntity sender = messageContext.sender();
        String playerHead = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
        if (playerHead == null || playerHead.length() > 16) {
            PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(sender);

            Component playerHeadComponent = Component.object().contents(
                    playerHeadBuilderComponent
                            .name(sender.getName())
                            .id(sender.getUuid())
                            .profileProperty(profileProperty)
                            .build()
            ).build();

            return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, playerHeadComponent));
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
                ).build();

        return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, playerHeadComponent));
    }

    public MessageContext addSpriteTag(MessageContext messageContext) {
        if (!config().sprite()) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            if (isModuleDisabledFor(sender)) return messageContext;
            if (!permissionChecker.check(sender, permission().sprite())) return messageContext;
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

    private Tag createSpriteTag(MessageContext messageContext, Component defaultComponent, ArgumentQueue argumentQueue) {
        Tag receiverVersionTag = checkAndGetReceiverTag(messageContext, defaultComponent);
        if (receiverVersionTag != null) return receiverVersionTag;
        if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

        Key sprite = Key.key(argumentQueue.pop().value());
        Tag.Argument secondArgument = argumentQueue.peek();

        SpriteObjectContents spriteObjectContents = secondArgument == null
                ? ObjectContents.sprite(sprite)
                : ObjectContents.sprite(sprite, Key.key(secondArgument.value())); // first atlas, second sprite

        Component spriteComponent = Component.object().contents(spriteObjectContents).build();

        return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, spriteComponent));
    }

    private Tag checkAndGetReceiverTag(MessageContext messageContext, Component defaultComponent) {
        FPlayer fReceiver = messageContext.receiver();

        // check console version
        if (isNewerThanOrEqualsV_1_21_9 && fReceiver.isUnknown()) {
            return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, defaultComponent));
        } else if (fReceiver.isUnknown()) {
            return Tag.selfClosingInserting(Component.empty());
        }

        // check player version
        if (packetProvider.isNewerThanOrEquals(fReceiver, ClientVersion.V_1_21_9)) {
            // bedrock player does not support object component
            if (integrationModule.isBedrockPlayer(fReceiver)) {
                return Tag.selfClosingInserting(addDefaultParametersIfNeeded(messageContext, defaultComponent));
            }

            // continue building
            return null;
        }

        // for old client
        return Tag.selfClosingInserting(Component.empty());
    }

    private Component getDefaultObjectComponent(MessageContext messageContext) {
        String defaultSymbol = localization(messageContext.receiver()).defaultSymbol();
        if (StringUtils.isEmpty(defaultSymbol)) return Component.empty();

        return Component.text(localization(messageContext.receiver()).defaultSymbol());
    }

    private Component addDefaultParametersIfNeeded(MessageContext messageContext, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return Component.empty();

        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE) && config().needExtraSpace()) {
            component = component.append(Component.space());
        }

        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            component = component.color(NamedTextColor.WHITE);
        }

        return component;
    }
}
