package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, MessageProcessor {

    private final Message.Format.Color color;

    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;

    @Inject
    public PlaceholderAPIIntegration(FPlayerService fPlayerService,
                                     FileResolver fileResolver,
                                     PlatformServerAdapter platformServerAdapter,
                                     FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.platformServerAdapter = platformServerAdapter;
        this.fLogger = fLogger;

        color = fileResolver.getMessage().getFormat().getColor();
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
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player);

        params = params.toLowerCase();

        if (params.startsWith("fcolor")) {

            String number = params.substring(7);

            return fPlayer.getColors().getOrDefault(number, color.getValues().get(number));
        }

        FPlayer.Setting setting = FPlayer.Setting.fromString(params);
        if (setting != null) {
            String value = fPlayer.getSettingValue(setting);
            if (setting == FPlayer.Setting.CHAT && value == null) return "default";

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

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (!(sender instanceof FPlayer fPlayer)) return;

        String message = messageContext.getMessage();

        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.getUuid());
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (!fPlayer.isOnline()) return;

            Player receiver = Bukkit.getPlayer(messageContext.getReceiver().getUuid());
            if (receiver == null) {
                receiver = offlinePlayer.getPlayer();
            }

            message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);

        } catch (NullPointerException | ClassCastException ignored) {
            // ignore placeholderapi exceptions
        }

        messageContext.setMessage(message);
    }
}
