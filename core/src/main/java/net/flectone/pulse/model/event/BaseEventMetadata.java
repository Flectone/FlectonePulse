package net.flectone.pulse.model.event;

import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public record BaseEventMetadata<L extends LocalizationSetting>(
        @NonNull UUID uuid,
        @NonNull FEntity sender,
        @Nullable FPlayer filterPlayer,
        @NonNull Predicate<FPlayer> filter,
        @NonNull Map<MessageFlag, Boolean> flags,
        @NonNull BiFunction<FPlayer, L, String> format,
        @NonNull Destination destination,
        @NonNull Range range,
        @Nullable Pair<Sound, PermissionSetting> sound,
        @Nullable String message,
        @Nullable Function<FPlayer, TagResolver[]> tagResolvers,
        @Nullable ProxyDataConsumer<SafeDataOutputStream> proxy,
        @Nullable UnaryOperator<String> integration
) implements EventMetadata<L> {

    @Override
    public BaseEventMetadata<L> base() {
        return this;
    }

    @Override
    public @Nullable TagResolver[] resolveTags(FPlayer fPlayer) {
        return this.tagResolvers == null ? null : tagResolvers.apply(fPlayer);
    }

    @Override
    public @NonNull String resolveFormat(FPlayer fPlayer, L localization) {
        return StringUtils.defaultString(format.apply(fPlayer, localization));
    }

}
