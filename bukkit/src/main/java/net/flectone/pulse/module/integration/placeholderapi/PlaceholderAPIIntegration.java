package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.ServerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration {

    private final Message.Format.Color color;

    private final FPlayerManager fPlayerManager;
    private final TaskScheduler taskScheduler;
    private final ServerUtil serverUtil;
    private final FLogger fLogger;

    @Inject
    public PlaceholderAPIIntegration(FPlayerManager fPlayerManager,
                                     TaskScheduler taskScheduler,
                                     FileManager fileManager,
                                     ServerUtil serverUtil,
                                     FLogger fLogger) {
        this.fPlayerManager = fPlayerManager;
        this.taskScheduler = taskScheduler;
        this.serverUtil = serverUtil;
        this.fLogger = fLogger;

        color = fileManager.getMessage().getFormat().getColor();
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
        taskScheduler.runSync(() -> {
            if (isRegistered()) {
                unregister();
            }

            register();
            fLogger.info("PlaceholderAPI hooked");
        });
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerManager.get(player);

        params = params.toLowerCase();

        if (params.startsWith("fcolor")) {

            String number = params.substring(7);

            return fPlayer.getColors().getOrDefault(number, color.getValues().get(number));
        }

        String placeholder = switch (params) {
            case "world_prefix" -> fPlayer.getWorldPrefix();
            case "stream_prefix" -> fPlayer.getStreamPrefix();
            case "afk_suffix" -> fPlayer.getAfkSuffix();
            case "player" -> fPlayer.getName();
            case "ip" -> fPlayer.getIp();
            case "ping" -> String.valueOf(fPlayerManager.getPing(fPlayer));
            case "online" -> String.valueOf(serverUtil.getOnlineCount());
            case "tps" -> serverUtil.getTPS();
            default -> null;
        };

        return placeholder == null ? "" : placeholder;
    }

    public String setPlaceholders(FEntity sender, FEntity fReceiver, String message) {
        if (!(sender instanceof FPlayer fPlayer)) return message;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        try {
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (!offlinePlayer.isOnline()) return message;

            Player receiver = Bukkit.getPlayer(fReceiver.getUuid());
            if (receiver == null) return message;

            return PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);
        } catch (NullPointerException ignored) {}

        return message;
    }
}
