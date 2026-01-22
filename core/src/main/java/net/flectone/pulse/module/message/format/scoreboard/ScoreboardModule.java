package net.flectone.pulse.module.message.format.scoreboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScoreboardModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public Message.Format.Scoreboard config() {
        return fileFacade.message().format().scoreboard();
    }

    @Override
    public Permission.Message.Format.Scoreboard permission() {
        return fileFacade.permission().message().format().scoreboard();
    }

}
