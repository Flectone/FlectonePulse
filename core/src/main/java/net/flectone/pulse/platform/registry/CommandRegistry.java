package net.flectone.pulse.platform.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.parser.integer.ColorParser;
import net.flectone.pulse.processing.parser.integer.DurationReasonParser;
import net.flectone.pulse.processing.parser.player.PlayerParser;
import net.flectone.pulse.processing.parser.string.MessageParser;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.Function;

public abstract class CommandRegistry implements Registry {

    protected CommandRegistry() {
    }

    public abstract void registerCommand(Function<CommandManager<FPlayer> , Command.Builder<FPlayer>> builder);

    public abstract void unregisterCommand(String name);


    public void setupBrigadierManager(CloudBrigadierManager<FPlayer, ?> brigadierManager) {
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
    }

}
