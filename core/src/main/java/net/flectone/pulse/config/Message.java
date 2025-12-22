package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.CooldownConfigSetting;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.SoundConfigSetting;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.util.*;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.util.constant.AdventureTag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@With
@Builder(toBuilder = true)
@Jacksonized
public record Message(

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
        Boolean enable,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk/")
        Afk afk,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/anvil/")
        Anvil anvil,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto/")
        Auto auto,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/book/")
        Book book,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bossbar/")
        Bossbar bossbar,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/brand/")
        Brand brand,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bubble/")
        Bubble bubble,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/chat/")
        Chat chat,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/")
        Format format,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/greeting/")
        Greeting greeting,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/join/")
        Join join,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/")
        Objective objective,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/quit/")
        Quit quit,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rightclick/")
        Rightclick rightclick,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sidebar/")
        Sidebar sidebar,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sign/")
        Sign sign,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/")
        Status status,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/")
        Tab tab,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/update/")
        Update update,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/vanilla/")
        Vanilla vanilla

) implements EnableSetting {

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Afk(
            Boolean enable,
            Range range,
            Integer delay,
            List<String> ignore,
            Destination destination,
            Ticker ticker
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Anvil(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Auto(
            Boolean enable,
            Map<String, Type> types
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Type(
                Boolean random,
                Destination destination,
                Ticker ticker,
                Sound sound
        ) implements SoundConfigSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Book(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Bossbar(
            Boolean enable,
            Map<String, Announce> announce
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Announce(
                Destination destination,
                Sound sound
        ) implements SoundConfigSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Brand(
            Boolean enable,
            Boolean random,
            Destination destination,
            Ticker ticker
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Bubble(
            Boolean enable,
            Integer maxCount,
            Integer maxLength,
            Float elevation,
            Double distance,
            Double readSpeed,
            Double handicapChars,
            String wordBreakHint,
            Interaction interaction,
            Modern modern
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Interaction(
                Boolean enable,
                Float height
        ) {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Modern(
                Boolean enable,
                Boolean hasShadow,
                Integer animationTime,
                Float scale,
                String background,
                BubbleModule.Billboard billboard
        ) {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Chat(
            Boolean enable,
            Mode mode,
            Event.Priority priority,
            Map<String, Type> types
    ) implements EnableSetting {

        public enum Mode {
            BUKKIT,
            PAPER,
            PACKET
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Type(
                Boolean enable,
                Boolean cancel,
                Range range,
                Integer priority,
                String trigger,
                NullReceiver nullReceiver,
                Destination destination,
                Cooldown cooldown,
                Sound sound
        ) implements CooldownConfigSetting, SoundConfigSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record NullReceiver(
                    Boolean enable,
                    Destination destination
            ) {
            }
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Format(
            Boolean enable,
            Boolean convertLegacyColor,
            List<AdventureTag> adventureTags,
            FColor fcolor,
            Fixation fixation,
            Mention mention,
            Moderation moderation,
            Names names,
            Object object,
            QuestionAnswer questionAnswer,
            Replacement replacement,
            Scoreboard scoreboard,
            Translate translate,
            World world
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record FColor(
                Boolean enable,
                Map<Integer, String> defaultColors
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Fixation(
                Boolean enable,
                Boolean endDot,
                Boolean firstLetterUppercase,
                List<String> nonDotSymbols
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mention(
                Boolean enable,
                String trigger,
                String everyoneTag,
                Destination destination,
                Sound sound
        ) implements EnableSetting, SoundConfigSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Moderation(
                Boolean enable,
                Caps caps,
                Delete delete,
                Newbie newbie,
                Flood flood,
                Swear swear
        ) implements EnableSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Caps(
                    Boolean enable,
                    Double trigger
            ) implements EnableSetting {
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Delete(
                    Boolean enable,
                    Integer historyLength
            ) implements EnableSetting {
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Newbie(
                    Boolean enable,
                    Mode mode,
                    long timeout
            ) implements EnableSetting {
                public enum Mode {
                    PLAYED_TIME,
                    SINCE_JOIN
                }
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Flood(
                    Boolean enable,
                    Boolean trimToSingle,
                    Integer maxRepeatedSymbols,
                    Integer maxRepeatedWords
            ) implements EnableSetting {
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Swear(
                    Boolean enable,
                    List<String> ignore,
                    List<String> trigger
            ) implements EnableSetting {
            }
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Names(
                Boolean enable,
                Boolean shouldCheckInvisibility
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Object(
                Boolean enable,
                Boolean playerHead,
                Boolean sprite,
                Boolean needExtraSpace
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record QuestionAnswer(
                Boolean enable,
                Map<String, Question> questions
        ) implements EnableSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Question(
                    Range range,
                    Destination destination,
                    Cooldown cooldown,
                    Sound sound,
                    String target
            ) implements CooldownConfigSetting, SoundConfigSetting {
            }

        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Replacement(
                Boolean enable,
                Map<String, String> triggers
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Scoreboard(
                Boolean enable,
                Boolean nameVisible,
                String color,
                String prefix,
                String suffix,
                Ticker ticker
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Translate(Boolean enable) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record World(
                Boolean enable,
                WorldModule.Mode mode,
                Ticker ticker,
                Map<String, String> values
        ) implements EnableSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Greeting(
            Boolean enable,
            Destination destination,
            Sound sound
    ) implements EnableSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Join(
            Boolean enable,
            Boolean first,
            Range range,
            Destination destination,
            Sound sound
    ) implements EnableSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Objective(
            Boolean enable,
            Belowname belowname,
            Tabname tabname
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Belowname(
                Boolean enable,
                ObjectiveModule.Mode mode,
                Ticker ticker
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tabname(
                Boolean enable,
                ObjectiveModule.Mode mode,
                Ticker ticker
        ) implements EnableSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Quit(
            Boolean enable,
            Range range,
            Destination destination,
            Sound sound
    ) implements EnableSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Rightclick(
            Boolean enable,
            Boolean shouldCheckSneaking,
            Boolean hideNameWhenInvisible,
            Range range,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements EnableSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Sidebar(
            Boolean enable,
            Boolean random,
            Ticker ticker
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Sign(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Status(
            Boolean enable,
            Icon icon,
            MOTD motd,
            Players players,
            Version version
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record MOTD(
                Boolean enable,
                Boolean random
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Icon(
                Boolean enable,
                Boolean random,
                List<String> values
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Players(
                Boolean enable,
                Boolean control,
                Integer max,
                Integer online
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Version(
                Boolean enable,
                Integer protocol
        ) implements EnableSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Tab(
            Boolean enable,
            Header header,
            Footer footer,
            Playerlistname playerlistname
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Footer(
                Boolean enable,
                Boolean random,
                Destination destination,
                Ticker ticker
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Header(
                Boolean enable,
                Boolean random,
                Destination destination,
                Ticker ticker
        ) implements EnableSetting {
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Playerlistname(
                Boolean enable,
                Boolean proxyMode,
                Ticker ticker
        ) implements EnableSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Update(
            Boolean enable,
            Destination destination,
            Sound sound
    ) implements EnableSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Vanilla(
            Boolean enable,
            List<VanillaMessage> types
    ) implements EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record VanillaMessage(
                String name,
                Boolean multiMessage,
                Range range,
                Destination destination,
                Sound sound,
                List<String> translationKeys
        ) implements SoundConfigSetting {

            @Override
            public String name() {
                return name != null ? name : "";
            }

            @Override
            public Boolean multiMessage() {
                return multiMessage != null;
            }

            @Override
            public Range range() {
                return range != null ? range : Range.get(Range.Type.PLAYER);
            }

            @Override
            public Destination destination() {
                return destination != null ? destination : new Destination();
            }

            @Override
            public Sound sound() {
                return sound != null ? sound : new Sound();
            }

            @Override
            public List<String> translationKeys() {
                return translationKeys != null ? new ArrayList<>(translationKeys) : new ArrayList<>();
            }

            @JsonValue
            public Map<String, Object> toJson() {
                Map<String, Object> result = new LinkedHashMap<>();

                if (name != null && !name.isEmpty()) {
                    result.put("name", name.toUpperCase());
                }

                if (multiMessage != null && multiMessage) {
                    result.put("multi_message", true);
                }

                if (range != null && range.getType() != Range.Type.PLAYER) {
                    result.put("range", range);
                }

                if (destination != null && destination.getType() != Destination.Type.CHAT) {
                    result.put("destination", destination);
                }

                if (sound != null && sound.enable()) {
                    result.put("sound", sound);
                }

                if (translationKeys != null && !translationKeys.isEmpty()) {
                    result.put("translation_keys", translationKeys);
                }

                return result;
            }
        }
    }
}