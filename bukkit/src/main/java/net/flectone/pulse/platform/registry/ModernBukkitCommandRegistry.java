package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.processing.parser.integer.ColorParser;
import net.flectone.pulse.processing.parser.integer.DurationReasonParser;
import net.flectone.pulse.processing.parser.player.PlayerParser;
import net.flectone.pulse.processing.parser.string.MessageParser;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry {

    @Inject
    public ModernBukkitCommandRegistry(FileFacade fileFacade,
                                       ReflectionResolver reflectionResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       TaskScheduler taskScheduler,
                                       FPlayerMapper fPlayerMapper) {
        super(fileFacade, commandExceptionHandler, plugin, reflectionResolver, taskScheduler, fPlayerMapper);

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
}
