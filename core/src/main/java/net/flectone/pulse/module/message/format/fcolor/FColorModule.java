package net.flectone.pulse.module.message.format.fcolor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.convertor.LegacyColorConvertor;
import net.flectone.pulse.module.message.format.fcolor.listener.FColorPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
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

    private final FileResolver fileResolver;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final LegacyColorConvertor legacyColorConvertor;

    @Override
    public void onEnable() {
        super.onEnable();

        // register fColor types
        permission().getColors().forEach((key, value) -> registerPermission(value));

        listenerRegistry.register(FColorPulseListener.class);
    }

    @Override
    public Message.Format.FColor config() {
        return fileResolver.getMessage().getFormat().getFcolor();
    }

    @Override
    public Permission.Message.Format.FColor permission() {
        return fileResolver.getPermission().getMessage().getFormat().getFcolor();
    }

    public Permission.Message.Format formatPermission() {
        return fileResolver.getPermission().getMessage().getFormat();
    }

    public void format(MessageContext messageContext) {
        String message = messageContext.getMessage();
        if (!message.contains(MessagePipeline.ReplacementTag.FCOLOR.getTagName())) return;

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission().getLegacyColors())) return;

        FPlayer receiver = messageContext.getReceiver();
        if (isModuleDisabledFor(receiver)) return;

        // default map colors
        Map<Integer, String> colorsMap = new HashMap<>(config().getDefaultColors());

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

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.FCOLOR, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            OptionalInt number = argumentQueue.pop().asInt();
            if (number.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(colorsMap.getOrDefault(number.getAsInt(), ""));
        });

        // replace deprecated tag
        if (message.contains("/fcolor")) {
            messageContext.setMessage(RegExUtils.replaceAll(message, "</fcolor(:\\d+)?>", ""));
        }
    }

    private void updateColorsMap(Map<Integer, String> colorsMap, FPlayer fPlayer, FColor.Type type) {
        if (permissionChecker.check(fPlayer, permission().getColors().get(type))) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }
    }
}
