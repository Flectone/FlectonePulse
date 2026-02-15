package net.flectone.pulse.module.message.format.fcolor;

import com.google.common.collect.ImmutableList;
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
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.convertor.LegacyColorConvertor;
import net.flectone.pulse.module.message.format.fcolor.listener.FColorPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.OptionalInt;


@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FColorModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final LegacyColorConvertor legacyColorConvertor;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(FColorPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().addAll(permission().colors().values());
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
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission().legacyColors())) return messageContext;

        FPlayer receiver = messageContext.receiver();
        if (isModuleDisabledFor(receiver)) return messageContext;

        boolean isSenderColorOut = messageContext.isFlag(MessageFlag.SENDER_COLOR_OUT);

        messageContext = messageContext.addTagResolver(MessagePipeline.ReplacementTag.FCOLOR, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            OptionalInt number = argumentQueue.pop().asInt();
            if (number.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            int index = number.getAsInt();
            String color = getFColorOrDefault(receiver, FColor.Type.SEE, index, config().defaultColors().get(index));

            // send out colors
            if (isSenderColorOut) {
                if (sender instanceof FPlayer fPlayer) {
                    color = getFColorOrDefault(fPlayer, FColor.Type.OUT, index, color);
                }
            } else {
                color = getFColorOrDefault(receiver, FColor.Type.OUT, index, color);
            }

            color = legacyColorConvertor.convert(StringUtils.defaultString(color));

            return Tag.preProcessParsed(color);
        });

        // replace deprecated tag
        if (message.contains("/fcolor")) {
            messageContext = messageContext.withMessage(RegExUtils.replaceAll(message, "</fcolor(:\\d+)?>", ""));
        }

        return messageContext;
    }

    private String getFColorOrDefault(FPlayer fPlayer, FColor.Type type, int index, String defaultColor) {
        if (permissionChecker.check(fPlayer, permission().colors().get(type))) {
            Map<Integer, String> colorMap = fPlayer.getFColors(type);
            return colorMap.getOrDefault(index, defaultColor);
        }

        return defaultColor;
    }
}
