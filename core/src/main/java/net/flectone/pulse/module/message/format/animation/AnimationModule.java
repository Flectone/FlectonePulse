package net.flectone.pulse.module.message.format.animation;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.format.animation.listener.AnimationPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnimationModule implements ModuleLocalization<Localization.Message.Format.Animation> {

    private final Map<AnimationKey, Integer> animationMap = new ConcurrentHashMap<>();
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(AnimationPulseListener.class);
    }

    @Override
    public void onDisable() {
        animationMap.clear();
    }

    @Override
    public Localization.Message.Format.Animation localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().animation();
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
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().addAll(permission().values().values());
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (moduleController.isDisabledFor(this, messageContext.sender())) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.ANIMATION, (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String animation = argumentQueue.pop().value();
            if (!permissionChecker.check(messageContext.receiver(), permission().values().get(animation))) return MessagePipeline.ReplacementTag.emptyTag();

            List<String> texts = localization(messageContext.receiver()).values().get(animation);
            if (texts == null || texts.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            Message.Format.Animation.AnimationConfig animationConfig = config().values().get(animation);
            if (animationConfig == null || animationConfig.interval() < 0) return MessagePipeline.ReplacementTag.emptyTag();

            UUID player = messageContext.receiver().uuid();
            int playerIndex = increment(player, animation, animationConfig.interval(), texts.size());

            try {
                String text = texts.get(playerIndex);
                if (Boolean.TRUE.equals(animationConfig.raw())) return Tag.preProcessParsed(text);

                MessageContext textContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), text)
                        .withFlags(messageContext.flags());

                return Tag.inserting(messagePipeline.build(textContext));
            } catch (IndexOutOfBoundsException _) { // reload safety
                return MessagePipeline.ReplacementTag.emptyTag();
            }
        });
    }

    public int increment(UUID player, String animation, int maxInterval, int maxIndex) {
        AnimationKey animationKey = new AnimationKey(player, animation);
        Integer encodedIndex = animationMap.get(animationKey);

        int currentInterval;
        int currentIndex;
        if (encodedIndex == null) {
            currentInterval = 0;
            currentIndex = 0;
        } else {
            currentIndex = encodedIndex / (maxInterval + 1);
            currentInterval = encodedIndex % (maxInterval + 1);
        }

        if (maxInterval <= 0 || currentInterval >= maxInterval) {
            currentInterval = 0;
            currentIndex = (currentIndex + 1) % maxIndex;
        } else {
            currentInterval++;
        }

        int newEncoded = currentIndex * (maxInterval + 1) + currentInterval;
        animationMap.put(animationKey, newEncoded);

        return currentIndex;
    }

    private record AnimationKey(UUID player, String phase) {
    }

}
