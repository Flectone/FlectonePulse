package net.flectone.pulse.module.message.format.fcolor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.format.convertor.LegacyColorConvertor;
import net.flectone.pulse.module.message.format.fcolor.listener.PulseFColorListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.OptionalInt;


@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FColorModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final LegacyColorConvertor legacyColorConvertor;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseFColorListener.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleSimple.super.permissionBuilder().addAll(permission().colors().values());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_FCOLOR;
    }

    @Override
    public Message.Format.FColor config() {
        return fileFacade.message().format().fcolor();
    }

    @Override
    public Permission.Message.Format.FColor permission() {
        return fileFacade.permission().message().format().fcolor();
    }

    public Permission.Message.Format formatPermission() {
        return fileFacade.permission().message().format();
    }

    public MessageContext format(MessageContext messageContext) {
        String message = messageContext.message();
        if (!message.contains(MessagePipeline.ReplacementTag.FCOLOR.getTagName())) return messageContext;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE) && !permissionChecker.check(sender, formatPermission().legacyColors())) return messageContext;

        FPlayer receiver = messageContext.receiver();
        if (moduleController.isDisabledFor(this, receiver)) return messageContext;

        boolean isSenderColorOut = messageContext.isFlag(MessageFlag.COLOR_CONTEXT_SENDER);

        messageContext = messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.FCOLOR.getTagName(), (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            OptionalInt number = argumentQueue.pop().asInt();
            if (number.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            int index = number.getAsInt();

            // get receiver SEE color
            String color = getFColorOrDefault(receiver, FColor.Type.SEE, index, config().defaultColors().getOrDefault(index, ""));

            // if colors must be from sender, we must check OUT colors from sender, otherwise we take them from receiver
            // We can't make it so that we take OUT receiver first and then OUT sender,
            // because the display will be mixed up and will be incorrect
            if (isSenderColorOut) {
                if (sender instanceof FPlayer fPlayer) {
                    // get sender OUT color
                    color = getFColorOrDefault(fPlayer, FColor.Type.OUT, index, color);
                }
            } else {
                // get receiver OUT color
                color = getFColorOrDefault(receiver, FColor.Type.OUT, index, color);
            }

            // convert legacy color to modern
            color = legacyColorConvertor.convert(StringUtils.defaultString(color));

            return Tag.preProcessParsed(color);
        }));

        // replace deprecated tag
        if (message.contains("/fcolor")) {
            messageContext = messageContext.withMessage(RegExUtils.replaceAll(message, "</fcolor(:\\d+)?>", ""));
        }

        return messageContext;
    }

    private String getFColorOrDefault(FPlayer fPlayer, FColor.Type type, int index, String defaultColor) {
        if (permissionChecker.check(fPlayer, permission().colors().get(type))) {
            Map<Integer, String> colorMap = socialService.loadColors(fPlayer, type);
            return colorMap.getOrDefault(index, defaultColor);
        }

        return defaultColor;
    }
}
