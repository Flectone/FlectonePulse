package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Getter
@SuperBuilder
public class EventMetadata<L extends LocalizationSetting> {

    @Builder.Default
    private final UUID uuid = UUID.randomUUID();

    @NonNull
    private final FEntity sender;

    @Nullable
    private final FPlayer filterPlayer;

    @Builder.Default
    private final Predicate<FPlayer> filter = fPlayer -> true;

    @Builder.Default
    private final boolean senderColorOut = true;

    @NonNull
    private final BiFunction<FPlayer, L, String> format;

    @NonNull
    @Builder.Default
    private final Destination destination = Destination.EMPTY_CHAT;

    @NonNull
    private final Range range;

    @Nullable
    private final Pair<Sound, PermissionSetting> sound;

    @Nullable
    private final String message;

    @Nullable
    private final Function<FPlayer, TagResolver[]> tagResolvers;

    @Nullable
    private final ProxyDataConsumer<SafeDataOutputStream> proxy;

    @Nullable
    private final UnaryOperator<String> integration;

    public abstract static class EventMetadataBuilder<
                L extends LocalizationSetting,
                C extends EventMetadata<L>,
                B extends EventMetadataBuilder<L, C, B>> {

        public B proxy(ProxyDataConsumer<SafeDataOutputStream> proxy) {
            this.proxy = proxy;
            return self();
        }

        public B proxy() {
            return proxy(dataOutputStream -> {});
        }

        public B integration(UnaryOperator<String> integrationFunction) {
            this.integration = integrationFunction;
            return self();
        }

        public B integration() {
            return integration(string -> string);
        }

        public B sender(FEntity sender) {
            this.sender = sender;

            return filterPlayer(sender);
        }

        public B filterPlayer(FEntity entity) {
            this.range = Range.get(Range.Type.PLAYER);
            this.filterPlayer = entity instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN;

            return self();
        }

        public B filterPlayer(FPlayer receiver, boolean senderColorOut) {
            return filterPlayer(receiver).senderColorOut(senderColorOut);
        }

        public B format(String format) {
            this.format = (fPlayer, l) -> format;

            return self();
        }

        public B format(Function<L, String> format) {
            this.format = (fResolver, s) -> format.apply(s);

            return self();
        }

        public B format(BiFunction<FPlayer, L, String> format) {
            this.format = format;

            return self();
        }
    }

    @Nullable
    public TagResolver[] getTagResolvers(FPlayer fPlayer) {
        return this.tagResolvers == null ? null : tagResolvers.apply(fPlayer);
    }

    @NonNull
    public String resolveFormat(FPlayer fPlayer, L localization) {
        return StringUtils.defaultString(format.apply(fPlayer, localization));
    }
}
