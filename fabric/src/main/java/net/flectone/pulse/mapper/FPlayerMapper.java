package net.flectone.pulse.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FPlayerMapper implements SenderMapper<ServerCommandSource, FPlayer> {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FPlayerService fPlayerService;

    @Inject
    public FPlayerMapper(FabricFlectonePulse fabricFlectonePulse,
                         FPlayerService fPlayerService) {
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public @NotNull FPlayer map(@NotNull ServerCommandSource sender) {
        ServerPlayerEntity player = sender.getPlayer();
        if (player != null) {
            return fPlayerService.getFPlayer(player.getUuid());
        }

        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NotNull ServerCommandSource reverse(@NotNull FPlayer mapped) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();

        Object obj = fPlayerService.toPlatformFPlayer(mapped);
        return obj instanceof ServerPlayerEntity player
                ? player.getCommandSource()
                : minecraftServer.getCommandSource();
    }
}
