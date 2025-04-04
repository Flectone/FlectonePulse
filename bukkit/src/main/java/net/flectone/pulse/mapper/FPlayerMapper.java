package net.flectone.pulse.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FPlayerMapper implements SenderMapper<CommandSender, FPlayer> {

    private final FPlayerService fPlayerService;

    @Inject
    public FPlayerMapper(FPlayerService fPlayerService) {
        this.fPlayerService = fPlayerService;
    }

    @Override
    public @NotNull FPlayer map(@NotNull CommandSender sender) {
        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NotNull CommandSender reverse(@NotNull FPlayer mapped) {
        Object obj = fPlayerService.toPlatformFPlayer(mapped);
        return obj != null ? (CommandSender) obj : Bukkit.getConsoleSender();
    }
}
