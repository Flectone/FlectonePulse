package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, PulseListener {

    private final Message.Format.Color color;
    private final Permission.Integration.Placeholderapi permission;
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final PlaceholderAPIModule placeholderAPIModule;
    private final FLogger fLogger;

    @Inject
    public PlaceholderAPIIntegration(FPlayerService fPlayerService,
                                     FileResolver fileResolver,
                                     PlatformServerAdapter platformServerAdapter,
                                     PermissionChecker permissionChecker,
                                     PlaceholderAPIModule placeholderAPIModule,
                                     FLogger fLogger) {
        this.color = fileResolver.getMessage().getFormat().getColor();
        this.permission = fileResolver.getPermission().getIntegration().getPlaceholderapi();
        this.fPlayerService = fPlayerService;
        this.platformServerAdapter = platformServerAdapter;
        this.permissionChecker = permissionChecker;
        this.placeholderAPIModule = placeholderAPIModule;
        this.fLogger = fLogger;
    }

    @Override
    public @NotNull String getIdentifier() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public @NotNull String getAuthor() {
        return BuildConfig.PROJECT_AUTHOR;
    }

    @Override
    public @NotNull String getVersion() {
        return BuildConfig.PROJECT_VERSION;
    }

    @Override
    public void hook() {
        syncHook();
        fLogger.info("✔ PlaceholderAPI hooked");
    }

    @Override
    public void unhook() {
        syncUnhook();
        fLogger.info("✖ PlaceholderAPI unhooked");
    }

    @Sync
    public void syncHook() {
        register();
    }

    @Sync
    public void syncUnhook() {
        unregister();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

        params = params.toLowerCase();

        if (params.startsWith("fcolor")) {

            String number = params.substring(7);

            return fPlayer.getColors().getOrDefault(number, color.getValues().get(number));
        }

        FPlayer.Setting setting = FPlayer.Setting.fromString(params);
        if (setting != null) {
            String value = fPlayer.getSettingValue(setting);
            if (setting == FPlayer.Setting.CHAT && value == null) return "default";
            if (setting == FPlayer.Setting.STREAM_PREFIX && value != null && value.isBlank()) return "";

            return value == null ? ""
                    : value.isEmpty() ? "true"
                    : value;
        }

        String placeholder = switch (params) {
            case "player" -> fPlayer.getName();
            case "ip" -> fPlayer.getIp();
            case "ping" -> String.valueOf(fPlayerService.getPing(fPlayer));
            case "online" -> String.valueOf(platformServerAdapter.getOnlinePlayerCount());
            case "tps" -> platformServerAdapter.getTPS();
            default -> null;
        };

        return placeholder == null ? "" : placeholder;
    }

    @Pulse(priority = Event.Priority.LOW)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (placeholderAPIModule.isModuleDisabledFor(sender)) return;

        FEntity fReceiver = messageContext.getReceiver();
        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
        if (!permissionChecker.check(sender, permission.getUse()) && isUserMessage) return;
        if (!permissionChecker.check(fReceiver, permission.getUse()) && isUserMessage) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        String message = messageContext.getMessage();

        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.getUuid());
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (fPlayer.isOnline()) {
                Player receiver = Bukkit.getPlayer(fReceiver.getUuid());
                if (receiver == null) {
                    receiver = offlinePlayer.getPlayer();
                }

                message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);
            }

        } catch (NullPointerException | ClassCastException ignored) {
            // ignore placeholderapi exceptions
        }

        messageContext.setMessage(message);
    }
}
