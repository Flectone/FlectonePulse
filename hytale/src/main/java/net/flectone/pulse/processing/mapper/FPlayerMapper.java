package net.flectone.pulse.processing.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.SenderMapper;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerMapper implements SenderMapper<CommandSender, FPlayer> {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public @NonNull FPlayer map(@NonNull CommandSender sender) {
        return fPlayerService.getFPlayer(sender.getUuid());
    }

    @Override
    public @NonNull CommandSender reverse(@NonNull FPlayer mapped) {
        Object obj = platformPlayerAdapter.convertToPlatformPlayer(mapped);
        return obj instanceof CommandSender commandSender
                ? commandSender
                : ConsoleSender.INSTANCE;
    }
}
