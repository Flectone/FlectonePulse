package net.flectone.pulse.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.processing.parser.player.OfflinePlayerParser;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Singleton
public class WhitelistModerationParser extends ModerationParser {

    private final ModerationService moderationService;
    private final OfflinePlayerParser offlinePlayerParser;

    @Inject
    public WhitelistModerationParser(ModerationService moderationService,
                                     OfflinePlayerParser offlinePlayerParser) {
        super(Moderation.Type.WHITELIST, moderationService);

        this.moderationService = moderationService;
        this.offlinePlayerParser = offlinePlayerParser;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        String[] words = input.input().split(" ");
        if (words.length < 2) return List.of();

        return switch (words[1].toUpperCase()) {
            case "REMOVE", "LIST" -> moderationService.getValidNames(Moderation.Type.WHITELIST);
            case "ADD" -> offlinePlayerParser.stringSuggestions(context, input);
            default -> List.of();
        };
    }

}
