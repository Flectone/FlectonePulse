package net.flectone.pulse.processing.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerMapper implements SenderMapper<CommandSender, FPlayer> {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public @NotNull FPlayer map(@NotNull CommandSender sender) {
        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NotNull CommandSender reverse(@NotNull FPlayer mapped) {
        Object obj = platformPlayerAdapter.convertToPlatformPlayer(mapped);
        return obj != null ? (CommandSender) obj : Bukkit.getConsoleSender();
    }
}
