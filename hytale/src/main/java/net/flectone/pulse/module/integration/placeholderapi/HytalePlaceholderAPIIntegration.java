package net.flectone.pulse.module.integration.placeholderapi;

import at.helpch.placeholderapi.PlaceholderAPI;
import at.helpch.placeholderapi.expansion.PlaceholderExpansion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final HytalePlaceholderAPIModule placeholderAPIModule;
    private final TaskScheduler taskScheduler;
    private final MuteModule muteModule;
    private final FLogger fLogger;

    @Override
    public @NonNull String getIdentifier() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public @NonNull String getAuthor() {
        return BuildConfig.PROJECT_AUTHOR;
    }

    @Override
    public @NonNull String getVersion() {
        return BuildConfig.PROJECT_VERSION;
    }

    @Override
    public void hook() {
        taskScheduler.runSync(this::register);
        fLogger.info("✔ PlaceholderAPI hooked");
    }

    @Override
    public void unhook() {
        taskScheduler.runSync(this::unregister);
        fLogger.info("✖ PlaceholderAPI unhooked");
    }

    @Override
    public String onPlaceholderRequest(PlayerRef player, @NonNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());

        params = params.toLowerCase();
        if (params.equalsIgnoreCase("mute_suffix")) {
            return muteModule.getMuteSuffix(fPlayer, fPlayer);
        }

        if (params.startsWith("fcolor")) {

            String number = params.substring(params.lastIndexOf("_") + 1);
            if (!StringUtils.isNumeric(number)) return null;

            Map<Integer, String> colorsMap = new HashMap<>(fileFacade.message().format().fcolor().defaultColors());
            if (params.startsWith("fcolor_out")) {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.OUT));
            } else if (params.startsWith("fcolor_see")) {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.SEE));
            } else {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.SEE));
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.OUT));
            }

            return colorsMap.get(Integer.parseInt(number));
        }

        SettingText settingText = SettingText.fromString(params);
        if (settingText != null) {
            String value = fPlayer.getSetting(settingText);
            if (settingText == SettingText.CHAT_NAME && value == null) return "default";

            return StringUtils.defaultString(value);
        }

        try {
            MessageType messageType = MessageType.valueOf(params.toUpperCase());
            return PlaceholderAPI.booleanValue(fPlayer.isSetting(messageType));
        } catch (IllegalArgumentException ignored) {
            // ignore exception
        }

        return switch (params) {
            case "player" -> fPlayer.name();
            case "ip" -> fPlayer.ip();
            case "ping" -> String.valueOf(platformPlayerAdapter.getPing(fPlayer));
            case "online" -> String.valueOf(platformServerAdapter.getOnlinePlayerCount());
            case "tps" -> platformServerAdapter.getTPS();
            default -> null;
        };
    }

    @Pulse(priority = Event.Priority.LOW)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        FEntity sender = messageContext.sender();
        if (placeholderAPIModule.isModuleDisabledFor(sender)) return event;

        FPlayer fReceiver = messageContext.receiver();
        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
        if (!permissionChecker.check(sender, placeholderAPIModule.permission().use()) && isUserMessage) return event;
        if (!(sender instanceof FPlayer fPlayer)) return event;

        String message = messageContext.message();

        // switch parsing
        if (!messageContext.isFlag(MessageFlag.SENDER_INTEGRATION_PLACEHOLDERS)) {
            FPlayer tempFPlayer = fPlayer;
            fPlayer = fReceiver;
            fReceiver = tempFPlayer;
        }

        try {
            Universe universe = Universe.get();
            if (universe != null) {
                PlayerRef offlinePlayer = universe.getPlayer(fPlayer.uuid());
                message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

                if (fPlayer.isOnline()) {
                    PlayerRef receiver = universe.getPlayer(fReceiver.uuid());
                    if (receiver == null) {
                        receiver = offlinePlayer;
                    }

                    message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer, receiver, message);
                }
            }

        } catch (Exception ignored) {
            // ignore placeholderapi exceptions
        }

        return event.withContext(messageContext.withMessage(message));
    }

}
