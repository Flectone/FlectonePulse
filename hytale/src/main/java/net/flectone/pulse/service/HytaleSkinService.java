package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleSkinService implements SkinService {

    private final FileFacade fileFacade;

    @Override
    public @NonNull String getAvatarUrl(@NonNull FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().avatarApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public @NonNull String getBodyUrl(@NonNull FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().bodyApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public void updateProfileProperty(@NonNull FPlayer fPlayer, boolean updatePlayerlistname, boolean sendToProxy) {
    }

    @Override
    public void invalidateProfileProperty(@NonNull UUID player) {
    }

    @Override
    public @NonNull String getSkin(@NonNull FEntity entity) {
        return entity.uuid().toString();
    }

}
