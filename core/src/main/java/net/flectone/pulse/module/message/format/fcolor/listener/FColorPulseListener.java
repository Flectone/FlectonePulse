package net.flectone.pulse.module.message.format.fcolor.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class FColorPulseListener implements PulseListener {

    private final Pattern fColorPattern = Pattern.compile("(<fcolor:(\\d+)>)|(</fcolor(:\\d+)?>)");

    private final Message.Format.FColor message;
    private final Permission.Message.Format.FColor permission;
    private final Permission.Message.Format formatPermission;
    private final FColorModule fColorModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public FColorPulseListener(FileResolver fileResolver,
                               FColorModule fColorModule,
                               PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat().getFcolor();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getFcolor();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.fColorModule = fColorModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;

        FPlayer receiver = messageContext.getReceiver();
        if (fColorModule.isModuleDisabledFor(receiver)) return;

        // default map colors
        Map<Integer, String> colorsMap = new HashMap<>(message.getDefaultColors());

        // receivers see colors
        updateColorsMap(colorsMap, receiver, FColor.Type.SEE);

        // send out colors
        if (sender instanceof FPlayer fPlayer) {
            updateColorsMap(colorsMap, fPlayer, FColor.Type.OUT);
        }

        String replacedMessage = replaceFColorPlaceholders(messageContext.getMessage(), colorsMap);
        messageContext.setMessage(replacedMessage);
    }

    public String replaceFColorPlaceholders(String contextMessage, Map<Integer, String> colorsMap) {
        Matcher matcher = fColorPattern.matcher(contextMessage);
        StringBuilder stringBuilder = new StringBuilder(contextMessage.length());
        int lastEnd = 0;
        while (matcher.find()) {
            stringBuilder.append(contextMessage, lastEnd, matcher.start());
            if (matcher.group(1) != null) {
                // regex already catch NumberFormatException
                int number = Integer.parseInt(matcher.group(2));

                String color = colorsMap.get(number);
                stringBuilder.append(color != null ? color : "");
            } // </fcolor:number> skipped

            lastEnd = matcher.end();
        }

        stringBuilder.append(contextMessage.substring(lastEnd));
        return stringBuilder.toString();
    }

    private void updateColorsMap(Map<Integer, String> colorsMap, FPlayer fPlayer, FColor.Type type) {
        if (permissionChecker.check(fPlayer, permission.getColors().get(type))) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }
    }

}
