package net.flectone.pulse.module.message.scoreboard.objective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectiveModule implements ModuleSimple {

    private final FileFacade fileFacade;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SCOREBOARD_OBJECTIVE;
    }

    @Override
    public Message.Scoreboard.Objective config() {
        return fileFacade.message().scoreboard().objective();
    }

    @Override
    public Permission.Message.Scoreboard.Objective permission() {
        return fileFacade.permission().message().scoreboard().objective();
    }

}
