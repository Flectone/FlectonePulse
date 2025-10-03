package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.processing.parser.integer.ColorParser;
import net.flectone.pulse.processing.parser.integer.DurationReasonParser;
import net.flectone.pulse.processing.parser.player.PlayerParser;
import net.flectone.pulse.processing.parser.string.MessageParser;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry {

    private final Config config;
    private final ReflectionResolver reflectionResolver;

    @Inject
    public ModernBukkitCommandRegistry(FileResolver fileResolver,
                                       ReflectionResolver reflectionResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       FPlayerMapper fPlayerMapper) {
        super(fileResolver, commandExceptionHandler, plugin, fPlayerMapper);

        this.config = fileResolver.getConfig();
        this.reflectionResolver = reflectionResolver;

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
        if (!config.getCommand().isUnregisterOnReload()) return;

        if (reflectionResolver.isPaper()) {
            removeCommands();
        } else {
            syncRemoveCommands();
        }
    }
}
