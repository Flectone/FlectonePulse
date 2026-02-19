package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final PlaceholderAPIModule placeholderAPIModule;
    private final TaskScheduler taskScheduler;
    private final MuteModule muteModule;
    @Getter private final FLogger fLogger;

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
    public String getIntegrationName() {
        return "PlaceholderAPI";
    }

    @Override
    public void hook() {
        taskScheduler.runSync(this::register);
        logHook();
    }

    @Override
    public void unhook() {
        taskScheduler.runSync(this::unregister);
        logUnhook();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

        params = params.toLowerCase();
        if (params.equalsIgnoreCase("mute_suffix")) {
            return muteModule.getMuteSuffix(fPlayer, fPlayer);
        }

        if (params.startsWith("fcolor")) {

            String number = params.substring(params.lastIndexOf("_") + 1);
            if (!StringUtils.isNumeric(number)) return null;

            Map<Integer, String> colorsMap = new Object2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
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
            return fPlayer.isSetting(messageType) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
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
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.uuid());
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (fPlayer.isOnline()) {
                Player receiver = Bukkit.getPlayer(fReceiver.uuid());
                if (receiver == null) {
                    receiver = offlinePlayer.getPlayer();
                }

                message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);
            }

        } catch (Exception ignored) {
            // ignore placeholderapi exceptions
        }

        return event.withContext(messageContext.withMessage(message));
    }
}
