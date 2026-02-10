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
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.RegExUtils;

import java.util.HashMap;
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

        // default map colors
        Map<Integer, String> colorsMap = new HashMap<>(config().defaultColors());

        // receivers see colors
        updateColorsMap(colorsMap, receiver, FColor.Type.SEE);

        // send out colors
        if (messageContext.isFlag(MessageFlag.SENDER_COLOR_OUT)) {
            if (sender instanceof FPlayer fPlayer) {
                updateColorsMap(colorsMap, fPlayer, FColor.Type.OUT);
            }
        } else {
            updateColorsMap(colorsMap, receiver, FColor.Type.OUT);
        }

        // convert legacy colors
        colorsMap.forEach((integer, string) -> colorsMap.put(integer, legacyColorConvertor.convert(string)));

        messageContext = messageContext.addTagResolver(MessagePipeline.ReplacementTag.FCOLOR, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            OptionalInt number = argumentQueue.pop().asInt();
            if (number.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            return Tag.preProcessParsed(colorsMap.getOrDefault(number.getAsInt(), ""));
        });

        // replace deprecated tag
        if (message.contains("/fcolor")) {
            messageContext = messageContext.withMessage(RegExUtils.replaceAll(message, "</fcolor(:\\d+)?>", ""));
        }

        return messageContext;
    }

    private void updateColorsMap(Map<Integer, String> colorsMap, FPlayer fPlayer, FColor.Type type) {
        if (permissionChecker.check(fPlayer, permission().colors().get(type))) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }
    }
}
