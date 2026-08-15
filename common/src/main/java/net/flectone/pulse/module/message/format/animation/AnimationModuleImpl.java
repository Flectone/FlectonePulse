package net.flectone.pulse.module.message.format.animation;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.animation.listener.PulseAnimationListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnimationModuleImpl implements AnimationModule {

    private final @Named("animation") Cache<AnimationKey, AtomicInteger> animationCache;
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseAnimationListener.class);
    }

    @Override
    public Localization.Message.Format.Animation localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().animation();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_ANIMATION;
    }

    @Override
    public Message.Format.Animation config() {
        return fileFacade.message().format().animation();
    }

    @Override
    public Permission.Message.Format.Animation permission() {
        return fileFacade.permission().message().format().animation();
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(AnimationModule.super.permissions());
        permissions.addAll(permission().values().values());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public MessageContext addTag(MessageContext messageContext) {
        if (moduleController.isDisabledFor(this, messageContext.sender())) return messageContext;

        if (messageContext.message().contains("<animation:")) {
            messageContext = messageContext.addFlag(MessageFlag.USE_CACHE, false);
        }

        MessageContext finalMessageContext = messageContext;
        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.ANIMATION.getTagName(), (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String animation = argumentQueue.pop().value();
            if (!permissionChecker.check(finalMessageContext.receiver(), permission().values().get(animation))) return MessagePipeline.ReplacementTag.emptyTag();

            List<String> texts = localization(finalMessageContext.receiver()).values().get(animation);
            if (texts == null || texts.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            Message.Format.Animation.AnimationConfig animationConfig = config().values().get(animation);
            if (animationConfig == null || animationConfig.interval() < 0) return MessagePipeline.ReplacementTag.emptyTag();

            UUID sender = finalMessageContext.sender().uuid();
            UUID receiver = finalMessageContext.receiver().uuid();
            int playerIndex = increment(sender, receiver, animation, animationConfig.interval(), texts.size());

            try {
                String text = texts.get(playerIndex);
                if (Boolean.TRUE.equals(animationConfig.raw())) return Tag.preProcessParsed(text);

                return Tag.inserting(messagePipeline.build(MessageContext.builder()
                        .sender(finalMessageContext.sender())
                        .receiver(finalMessageContext.receiver())
                        .message(text)
                        .flags(finalMessageContext.flags())
                        .flag(MessageFlag.USE_CACHE, true)
                        .build()
                ));
            } catch (IndexOutOfBoundsException _) { // reload safety
                return MessagePipeline.ReplacementTag.emptyTag();
            }
        }));
    }

    @Override
    public int increment(UUID sender, UUID receiver, String animation, int maxInterval, int maxIndex) {
        AnimationKey animationKey = new AnimationKey(sender, receiver, animation);

        AtomicInteger encoded = animationCache.get(animationKey, _ -> new AtomicInteger());

        int newEncoded = encoded.updateAndGet(encodedIndex -> {
            int currentIndex = encodedIndex / (maxInterval + 1);
            int currentInterval = encodedIndex % (maxInterval + 1);

            if (maxInterval <= 0 || currentInterval >= maxInterval) {
                currentInterval = 0;
                currentIndex = (currentIndex + 1) % maxIndex;
            } else {
                currentInterval++;
            }

            return currentIndex * (maxInterval + 1) + currentInterval;
        });

        return newEncoded / (maxInterval + 1);
    }

}
