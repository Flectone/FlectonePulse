package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.adapter.BukkitServerAdapter;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.handler.CommandExceptionHandler;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.parser.integer.ColorParser;
import net.flectone.pulse.parser.integer.DurationReasonParser;
import net.flectone.pulse.parser.player.PlayerParser;
import net.flectone.pulse.parser.string.MessageParser;
import net.flectone.pulse.resolver.FileResolver;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry {

    private final Config config;

    @Inject
    public ModernBukkitCommandRegistry(FileResolver fileResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       FPlayerMapper fPlayerMapper) {
        super(fileResolver, commandExceptionHandler, plugin, fPlayerMapper);

        this.config = fileResolver.getConfig();

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            CloudBrigadierManager<FPlayer, ?> brigadierManager = manager.brigadierManager();
            brigadierManager.setNativeSuggestions(new TypeToken<StringParser<FPlayer>>() {}, true);

            brigadierManager.registerMapping(new TypeToken<PlayerParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.string())
            );

            brigadierManager.registerMapping(new TypeToken<DurationReasonParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.registerMapping(new TypeToken<ColorParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.registerMapping(new TypeToken<MessageParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);

        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
    }

    @Override
    public void reload() {
        if (!config.isUnregisterOwnCommands()) return;

        if (BukkitServerAdapter.IS_PAPER) {
            removeCommands();
        } else {
            syncRemoveCommands();
        }
    }
}
