package net.flectone.pulse.platform.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.parser.integer.ColorParserImpl;
import net.flectone.pulse.parser.integer.DurationReasonParserImpl;
import net.flectone.pulse.parser.player.PlayerParserImpl;
import net.flectone.pulse.parser.string.MessageParserImpl;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.parser.standard.StringParser;

public interface BrigadierCommandRegistry extends CommandRegistry {

    default void setupBrigadierManager(CloudBrigadierManager<FPlayer, ?> brigadierManager) {
        brigadierManager.setNativeSuggestions(new TypeToken<StringParser<FPlayer>>() {}, true);

        brigadierManager.registerMapping(new TypeToken<PlayerParserImpl>() {},
                builder -> builder.cloudSuggestions()
                        .to(_ -> StringArgumentType.string())
        );

        brigadierManager.registerMapping(new TypeToken<DurationReasonParserImpl>() {},
                builder -> builder.cloudSuggestions()
                        .to(_ -> StringArgumentType.greedyString())
        );

        brigadierManager.registerMapping(new TypeToken<ColorParserImpl>() {},
                builder -> builder.cloudSuggestions()
                        .to(_ -> StringArgumentType.greedyString())
        );

        brigadierManager.registerMapping(new TypeToken<MessageParserImpl>() {},
                builder -> builder.cloudSuggestions()
                        .to(_ -> StringArgumentType.greedyString())
        );

        brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
    }

}
