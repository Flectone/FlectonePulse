package net.flectone.pulse.processing.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerMapper implements SenderMapper<CommandSourceStack, FPlayer> {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FPlayerService fPlayerService;

    @Override
    public @NotNull FPlayer map(@NotNull CommandSourceStack sender) {
        ServerPlayer player = sender.getPlayer();
        if (player != null) {
            return fPlayerService.getFPlayer(player.getUUID());
        }

        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NotNull CommandSourceStack reverse(@NotNull FPlayer mapped) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();

        Object obj = fPlayerService.toPlatformFPlayer(mapped);
        if (obj instanceof ServerPlayer player) {
            return player.createCommandSourceStack();
        }

        return minecraftServer.createCommandSourceStack();
    }
}