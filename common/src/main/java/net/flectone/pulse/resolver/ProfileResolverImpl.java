package net.flectone.pulse.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProfileResolverImpl implements ProfileResolver {

    @NonNull
    @Override
    public String resolveName(FEntity entity) {
        return StringUtils.isNotEmpty(entity.name()) ? entity.name() : entity.uuid().toString();
    }

    @NonNull
    @Override
    public String resolveOnlineName(@NonNull UUID uuid) {
        return "";
    }

    @Nullable
    @Override
    public UUID resolveOnlineUUID(@NonNull String name) {
        return null;
    }

    @NonNull
    @Override
    public UUID resolveOfflineUUID(String name) {
        return UUID.randomUUID();
    }

}
