package net.flectone.pulse.module.message.format.animation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.animation.listener.AnimationPulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnimationModule extends AbstractModuleLocalization<Localization.Message.Format.Animation> {

    private final Map<AnimationKey, Integer> animationMap = new ConcurrentHashMap<>();

    private record AnimationKey(UUID player, String phase) {}

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(AnimationPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        animationMap.clear();
    }

    @Override
    public MessageType messageType() {
        return MessageType.ANIMATION;
    }

    @Override
    public Localization.Message.Format.Animation localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().animation();
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
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().addAll(permission().values().values());
    }

    public MessageContext addAnimationTag(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;
        if (isModuleDisabledFor(messageContext.sender())) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.ANIMATION, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String animation = argumentQueue.pop().value();
            if (!permissionChecker.check(messageContext.receiver(), permission().values().get(animation))) return MessagePipeline.ReplacementTag.emptyTag();

            Localization.Message.Format.Animation.AnimationLocalization animationLocalization = localization(messageContext.receiver()).values().stream()
                    .filter(localization -> animation.equals(localization.name()))
                    .filter(localization -> localization.texts() != null && !localization.texts().isEmpty())
                    .findAny()
                    .orElse(null);
            if (animationLocalization == null) return MessagePipeline.ReplacementTag.emptyTag();

            Message.Format.Animation.AnimationConfig animationConfig = config().values().stream()
                    .filter(config -> animation.equals(config.name()))
                    .filter(config -> StringUtils.isEmpty(config.world())
                            || config.world().equals(platformPlayerAdapter.getWorldName(messageContext.receiver()))
                    )
                    .max(Comparator.comparingInt(Message.Format.Animation.AnimationConfig::interval))
                    .orElse(Message.Format.Animation.AnimationConfig.builder()
                            .raw(false)
                            .interval(0)
                            .build()
                    );
            if (animationConfig.interval() < 0) return MessagePipeline.ReplacementTag.emptyTag();

            UUID player = messageContext.receiver().uuid();
            int playerIndex = increment(player, animation, animationConfig.interval(), animationLocalization.texts().size());

            try {
                String text = animationLocalization.texts().get(playerIndex);
                if (Boolean.TRUE.equals(animationConfig.raw())) return Tag.preProcessParsed(text);

                MessageContext textContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), text)
                        .withFlags(messageContext.flags());

                return Tag.inserting(messagePipeline.build(textContext));
            } catch (IndexOutOfBoundsException e) { // reload safety
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

}
