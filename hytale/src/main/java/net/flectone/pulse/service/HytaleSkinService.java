package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.Strings;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleSkinService implements SkinService {

    private final FileFacade fileFacade;

    @Override
    public String getAvatarUrl(FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().avatarApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public String getBodyUrl(FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().bodyApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public String getSkin(FEntity entity) {
        return entity.uuid().toString();
    }

}
