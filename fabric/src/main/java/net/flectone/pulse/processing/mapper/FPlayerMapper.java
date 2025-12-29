package net.flectone.pulse.processing.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerMapper implements SenderMapper<ServerCommandSource, FPlayer> {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

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

        Object obj = platformPlayerAdapter.convertToPlatformPlayer(mapped);
        return obj instanceof ServerPlayerEntity player
                ? player.getCommandSource()
                : minecraftServer.getCommandSource();
    }
}
