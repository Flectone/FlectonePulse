package net.flectone.pulse.module.message.format.fcolor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fcolor.listener.FColorPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Singleton
public class FColorModule extends AbstractModule {

    private final Pattern fColorPattern = Pattern.compile("(<fcolor:(\\d+)>)|(</fcolor(:\\d+)?>)");

    private final FileResolver fileResolver;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FColorModule(FileResolver fileResolver,
                        PermissionChecker permissionChecker,
                        ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.permissionChecker = permissionChecker;
        this.listenerRegistry = listenerRegistry;
    }

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
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)
                && !permissionChecker.check(sender, formatPermission().getLegacyColors())) return;

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

        String contextMessage = messageContext.getMessage();
        String formattedMessage = replaceFColorPlaceholders(contextMessage, colorsMap);
        messageContext.setMessage(formattedMessage);
    }

    private String replaceFColorPlaceholders(String contextMessage, Map<Integer, String> colorsMap) {
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
        if (permissionChecker.check(fPlayer, permission().getColors().get(type))) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }
    }
}
