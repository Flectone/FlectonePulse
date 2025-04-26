package net.flectone.pulse.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FPlayerMapper implements SenderMapper<ServerCommandSource, FPlayer> {

    private final MinecraftServer minecraftServer;
    private final FPlayerService fPlayerService;

    @Inject
    public FPlayerMapper(MinecraftServer minecraftServer,
                         FPlayerService fPlayerService) {
        this.minecraftServer = minecraftServer;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public @NotNull FPlayer map(@NotNull ServerCommandSource sender) {
        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NotNull ServerCommandSource reverse(@NotNull FPlayer mapped) {
        Object obj = fPlayerService.toPlatformFPlayer(mapped);
        return obj != null ? (ServerCommandSource) obj : minecraftServer.getCommandSource();
    }
}
