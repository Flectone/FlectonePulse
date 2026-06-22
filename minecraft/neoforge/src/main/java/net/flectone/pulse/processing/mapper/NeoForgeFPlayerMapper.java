package net.flectone.pulse.processing.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.incendo.cloud.SenderMapper;
import org.jspecify.annotations.NonNull;

/**
 * Mapper that converts between NeoForge CommandSourceStack and FPlayer entities.
 * Used by the Cloud command framework to map command senders to FPlayer objects.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgeFPlayerMapper implements SenderMapper<CommandSourceStack, FPlayer> {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public @NonNull FPlayer map(@NonNull CommandSourceStack sender) {
        ServerPlayer player = sender.getPlayer();
        if (player != null) {
            return fPlayerService.getFPlayer(player.getUUID());
        }

        return fPlayerService.getFPlayer(sender);
    }

    @Override
    public @NonNull CommandSourceStack reverse(@NonNull FPlayer mapped) {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();

        Object obj = platformPlayerAdapter.convertToPlatformPlayer(mapped);
        return obj instanceof ServerPlayer player
                ? player.createCommandSourceStack()
                : minecraftServer.createCommandSourceStack();
    }
}
