package net.flectone.pulse.module.message.format.fading;

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
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.format.fading.listener.PulseFadingListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FadingModule implements ModuleLocalization {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;
    private final RandomGenerator randomGenerator;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseFadingListener.class);
    }

    @Override
    public Localization.Message.Format.Fading localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().fading();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_FADING;
    }

    @Override
    public Message.Format.Fading config() {
        return fileFacade.message().format().fading();
    }

    @Override
    public Permission.Message.Format.Fading permission() {
        return fileFacade.permission().message().format().fading();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().bypassSend(), permission().bypassView());
    }

    public MessageContext addTag(StringMessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.FADING.getTagName(), (_, _) -> {
            FPlayer receiver = messageContext.receiver();
            if (permissionChecker.check(sender, permission().bypassSend())) return createTag(messageContext);
            if (permissionChecker.check(receiver, permission().bypassView())) return createTag(messageContext);

            double distance = platformPlayerAdapter.distance(sender, receiver);
            double minDistance = config().minDistance();

            if (distance == -1.0 && config().hideDifferentWorld()) {
                String message = garble(messageContext.string(), 1.0, localization(receiver).symbol());
                return createTag(messageContext, message);
            }

            if (distance < minDistance) return createTag(messageContext);

            double maxDistance = Math.max(config().maxDistance(), config().minDistance() + 1);
            double ratio = Math.min(1.0, (distance - minDistance) / (maxDistance - minDistance));

            String message = garble(messageContext.string(), ratio, localization(receiver).symbol());
            return createTag(messageContext, message);
        }));
    }

    private Tag createTag(@NonNull StringMessageContext messageContext) {
        return createTag(messageContext, messageContext.string());
    }

    private Tag createTag(@NonNull MessageContext messageContext, @NonNull String message) {
        return Tag.inserting(messagePipeline.messageComponent(messageContext.sender(), messageContext.receiver(), message));
    }

    private String garble(@NonNull String message, double ratio, @NonNull String symbol) {
        if (ratio <= 0 || message.isEmpty()) return message;

        char placeholderChar = symbol.charAt(0);
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') continue;
            if (randomGenerator.nextDouble() < ratio) {
                chars[i] = placeholderChar;
            }
        }

        return new String(chars);
    }

}
