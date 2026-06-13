package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

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
    private final Provider<MuteModule> muteModuleProvider;
    private final Provider<ConditionModule> conditionModuleProvider;
    private final Provider<AfkModule> afkModuleProvider;
    private final Provider<OnlineModule> onlineModuleProvider;
    private final Provider<ToponlineModule> toponlineModuleProvider;
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
            return muteModuleProvider.get().getMuteSuffix(fPlayer, fPlayer);
        }

        if (params.equalsIgnoreCase("afk_duration")) {
            return String.valueOf(afkModuleProvider.get().getAfkDuration(fPlayer));
        }

        if (params.equalsIgnoreCase("afk_duration_formatted")) {
            return afkModuleProvider.get().getAfkDurationFormatted(fPlayer, fPlayer);
        }

        if (params.startsWith("toponline_")) {
            String position = params.substring(10);
            if (StringUtils.isEmpty(position)) return null;

            Optional<FPlayer> fTarget = toponlineModuleProvider.get().getPlayerByPosition(position);
            return fTarget.isPresent() ? fTarget.get().name() : "";
        }

        if (params.startsWith("online_")) {
            String time = params.substring(7);
            if (StringUtils.isEmpty(time)) return null;

            OnlineModule onlineModule = onlineModuleProvider.get();
            String timeValue = onlineModule.parseTimeValue(fPlayer, fPlayer, time);
            if (StringUtils.isEmpty(timeValue)) return null;

            return timeValue;
        }

        if (params.startsWith("condition_")) {
            String conditionName = params.substring(10);
            if (StringUtils.isEmpty(conditionName)) return null;

            return StringUtils.defaultString(conditionModuleProvider.get().getConditionValue(conditionName, fPlayer));
        }

        if (params.startsWith("fcolor")) {

            String number = params.substring(params.lastIndexOf("_") + 1);
            if (!StringUtils.isNumeric(number)) return null;

            Map<Integer, String> colorsMap = new Object2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
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
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.uuid());
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (fPlayer.isOnline()) {
                Player receiver = Bukkit.getPlayer(fReceiver.uuid());
                if (receiver == null) {
                    receiver = offlinePlayer.getPlayer();
                }

                message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);
            }

        } catch (Exception e) {
            if (firstTry && e.getMessage().contains("any region")  && reflectionResolver.isFolia()) {
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
