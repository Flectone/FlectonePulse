package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.parser.integer.ColorParser;
import net.flectone.pulse.parser.integer.DurationReasonParser;
import net.flectone.pulse.parser.moderation.BanModerationParser;
import net.flectone.pulse.parser.moderation.MuteModerationParser;
import net.flectone.pulse.parser.moderation.WarnModerationParser;
import net.flectone.pulse.parser.player.OfflinePlayerParser;
import net.flectone.pulse.parser.player.PlayerParser;
import net.flectone.pulse.parser.string.MessageParser;
import net.flectone.pulse.parser.string.SingleMessageParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.DurationParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;

@Singleton
public class CommandParserRegistry implements Registry {

    private final PlayerParser playerParser;
    private final OfflinePlayerParser offlinePlayerParser;
    private final DurationReasonParser durationReasonParser;
    private final BanModerationParser banModerationParser;
    private final MuteModerationParser muteModerationParser;
    private final WarnModerationParser warnModerationParser;
    private final ColorParser colorParser;
    private final MessageParser messageParser;
    private final SingleMessageParser singleMessageParser;
    private final StringParser<FPlayer> singleStringParser = new StringParser<>(StringParser.StringMode.SINGLE);
    private final StringParser<FPlayer> greedyStringParser = new StringParser<>(StringParser.StringMode.GREEDY);
    private final IntegerParser<FPlayer> integerParser = new IntegerParser<>(0, Integer.MAX_VALUE);
    private final BooleanParser<FPlayer> booleanParser = new BooleanParser<>(false);
    private final DurationParser<FPlayer> durationParser = new DurationParser<>();

    @Inject
    public CommandParserRegistry(PlayerParser playerParser,
                                 OfflinePlayerParser offlinePlayerParser,
                                 DurationReasonParser durationReasonParser,
                                 BanModerationParser banModerationParser,
                                 MuteModerationParser muteModerationParser,
                                 WarnModerationParser warnModerationParser,
                                 ColorParser colorParser,
                                 MessageParser messageParser,
                                 SingleMessageParser singleMessageParser) {
        this.banModerationParser = banModerationParser;
        this.muteModerationParser = muteModerationParser;
        this.warnModerationParser = warnModerationParser;
        this.playerParser = playerParser;
        this.offlinePlayerParser = offlinePlayerParser;
        this.durationReasonParser = durationReasonParser;
        this.colorParser = colorParser;
        this.messageParser = messageParser;
        this.singleMessageParser = singleMessageParser;
    }

    @Override
    public void reload() {}

    public @NonNull ParserDescriptor<FPlayer, String> playerParser(boolean offlinePlayers) {
        return offlinePlayers ? offlinePlayerParser() : playerParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> offlinePlayerParser() {
        return ParserDescriptor.of(offlinePlayerParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> playerParser() {
        return ParserDescriptor.of(playerParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, Integer> integerParser() {
        return ParserDescriptor.of(integerParser, Integer.class);
    }

    public @NonNull ParserDescriptor<FPlayer, Boolean> booleanParser() {
        return ParserDescriptor.of(booleanParser, Boolean.class);
    }

    public @NonNull ParserDescriptor<FPlayer, Integer> integerParser(int min, int max) {
        return ParserDescriptor.of(new IntegerParser<>(min, max), Integer.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> singleStringParser() {
        return ParserDescriptor.of(singleStringParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> greedyStringParser() {
        return ParserDescriptor.of(greedyStringParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> messageParser() {
        return ParserDescriptor.of(messageParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> singleMessageParser() {
        return ParserDescriptor.of(singleMessageParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> bannedParser() {
        return ParserDescriptor.of(banModerationParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> mutedParser() {
        return ParserDescriptor.of(muteModerationParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> warnedParser() {
        return ParserDescriptor.of(warnModerationParser, String.class);
    }

    public @NonNull ParserDescriptor<FPlayer, Pair<Long, String>> durationReasonParser() {
        return ParserDescriptor.of(durationReasonParser, new TypeToken<>() {});
    }

    public @NonNull ParserDescriptor<FPlayer, Duration> durationParser() {
        return ParserDescriptor.of(durationParser, Duration.class);
    }

    public @NonNull ParserDescriptor<FPlayer, String> colorParser() {
        return ParserDescriptor.of(colorParser, String.class);
    }
}
