package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.LazyInstance;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final BukkitPlaceholderAPIModule placeholderAPIModule;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    private final LazyInstance<MuteModule> muteModule;
    private final LazyInstance<ConditionModule> conditionModule;
    private final LazyInstance<AfkModule> afkModule;
    private final LazyInstance<OnlineModule> onlineModule;
    private final LazyInstance<ToponlineModule> toponlineModule;
    private final LazyInstance<ModerationService> moderationService;
    private final ReflectionResolver reflectionResolver;
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
    public @NonNull List<String> getPlaceholders() {
        return List.of(
                "%flectonepulse_mute_suffix%",
                "%flectonepulse_afk_duration%",
                "%flectonepulse_afk_duration_formatted%",
                "%flectonepulse_toponline_<position>%",
                "%flectonepulse_online_<time>%",
                "%flectonepulse_maintenance_<server>%",
                "%flectonepulse_condition_<name>%",
                "%flectonepulse_fcolor_<number>%",
                "%flectonepulse_fcolor_out_<number>%",
                "%flectonepulse_fcolor_see_<number>%",
                "%flectonepulse_setting_<name>%",
                "%flectonepulse_player%",
                "%flectonepulse_ip%",
                "%flectonepulse_ping%",
                "%flectonepulse_online%",
                "%flectonepulse_tps%"
        );
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public void hook() {
        try {
            taskScheduler.runSync(this::register);
            logHook();
        } catch (Exception e) {
            lohHookFailed(e);
        }
    }

    @Override
    public void unhook() {
        try {
            taskScheduler.runSync(this::unregister);
            logUnhook();
        } catch (Exception _) {
            // ignore
        }
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

        params = params.toLowerCase();
        if (params.equalsIgnoreCase("mute_suffix")) {
            return muteModule.get().getMuteSuffix(fPlayer, fPlayer);
        }

        if (params.equalsIgnoreCase("afk_duration")) {
            return String.valueOf(afkModule.get().getAfkDuration(fPlayer));
        }

        if (params.equalsIgnoreCase("afk_duration_formatted")) {
            return afkModule.get().getAfkDurationFormatted(fPlayer, fPlayer);
        }

        if (params.startsWith("toponline_")) {
            String position = params.substring(10);
            if (StringUtils.isEmpty(position)) return null;

            Optional<FPlayer> fTarget = toponlineModule.get().getPlayerByPosition(position);
            return fTarget.isPresent() ? fTarget.get().name() : "";
        }

        if (params.startsWith("online_")) {
            String time = params.substring(7);
            if (StringUtils.isEmpty(time)) return null;

            OnlineModule onlineModuleInstance = onlineModule.get();
            String timeValue = onlineModuleInstance.parseTimeValue(fPlayer, fPlayer, time);
            if (StringUtils.isEmpty(timeValue)) return null;

            return timeValue;
        }

        if (params.startsWith("maintenance_")) {
            String server = params.substring(12);
            if (StringUtils.isEmpty(server)) return null;

            return moderationService.get().getValid(fPlayerService.getConsole(), Moderation.Type.MAINTENANCE, server, 1, 0).isEmpty()
                    ? PlaceholderAPIPlugin.booleanFalse()
                    : PlaceholderAPIPlugin.booleanTrue();
        }

        if (params.startsWith("condition_")) {
            String conditionName = params.substring(10);
            if (StringUtils.isEmpty(conditionName)) return null;

            return StringUtils.defaultString(conditionModule.get().getConditionValue(conditionName, fPlayer));
        }

        if (params.startsWith("fcolor")) {

            String number = params.substring(params.lastIndexOf("_") + 1);
            if (!StringUtils.isNumeric(number)) return null;

            Map<Integer, String> colorsMap = new HashMap<>(fileFacade.message().format().fcolor().defaultColors());
            if (params.startsWith("fcolor_out")) {
                colorsMap.putAll(socialService.loadColors(fPlayer, FColor.Type.OUT));
            } else if (params.startsWith("fcolor_see")) {
                colorsMap.putAll(socialService.loadColors(fPlayer, FColor.Type.SEE));
            } else {
                colorsMap.putAll(socialService.loadColors(fPlayer, FColor.Type.SEE));
                colorsMap.putAll(socialService.loadColors(fPlayer, FColor.Type.OUT));
            }

            return colorsMap.get(Integer.parseInt(number));
        }

        if (params.startsWith("setting_")) {
            String conditionName = params.substring(8);
            if (StringUtils.isEmpty(conditionName)) return null;

            SettingText settingText = SettingText.fromString(conditionName);
            if (settingText != null) {
                String value = socialService.getSetting(fPlayer, settingText);
                if (settingText == SettingText.CHAT_NAME && value == null) return "default";

                return StringUtils.defaultString(value);
            }

            return socialService.isSetting(fPlayer, params.toUpperCase()) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        return switch (params) {
            case "player" -> fPlayer.name();
            case "ip" -> fPlayer.ip();
            case "ping" -> String.valueOf(platformPlayerAdapter.getPing(fPlayer));
            case "online" -> String.valueOf(platformServerAdapter.getOnlinePlayerCount());
            case "tps" -> platformServerAdapter.getTPS(fPlayer);
            default -> null;
        };
    }

    @Pulse(priority = Event.Priority.LOW)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(placeholderAPIModule, sender)) return event;

        FPlayer fReceiver = messageContext.receiver();
        boolean isUserMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);
        if (!permissionChecker.check(sender, placeholderAPIModule.permission().use()) && isUserMessage) return event;
        if (!(sender instanceof FPlayer fPlayer)) return event;

        String message = messageContext.message();

        // switch parsing
        if (!messageContext.isFlag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER)) {
            FPlayer tempFPlayer = fPlayer;
            fPlayer = fReceiver;
            fReceiver = tempFPlayer;
        }

        return event.withContext(messageContext.withMessage(setPlaceholders(fPlayer, fReceiver, message, true)));
    }

    private String setPlaceholders(FPlayer fPlayer, FPlayer fReceiver, String message, boolean firstTry) {
        try {
            // get offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.uuid());

            // set placeholders
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            // if player is offline, then no relation placeholders
            if (!fPlayer.isOnline()) return message;

            Player player = offlinePlayer.getPlayer();
            Player receiver = Bukkit.getPlayer(fReceiver.uuid());
            if (receiver == null) {
                receiver = player;
            }

            // perhaps in the future it is worth checking player and receiver for null, but is it necessary?
            message = PlaceholderAPI.setRelationalPlaceholders(player, receiver, message);
        } catch (NullPointerException _) {
            return message;
        } catch (Exception e) {
            if (firstTry && e.getMessage().contains("any region") && reflectionResolver.isFolia()) {
                FPlayer regionFPlayer = platformPlayerAdapter.isOnline(fPlayer) ? fPlayer : fPlayerService.getRandomFPlayer();

                CompletableFuture<String> completableFuture = new CompletableFuture<>();

                String finalMessage = message;
                taskScheduler.runRegion(regionFPlayer, () -> completableFuture.complete(setPlaceholders(fPlayer, fReceiver, finalMessage, false)));

                return completableFuture.join();
            }
        }

        return message;
    }
}