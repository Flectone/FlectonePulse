package net.flectone.pulse.module.message.format.scoreboard;

import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

public abstract class ScoreboardModule extends AbstractModule {

    private final Message.Format.Scoreboard message;
    private final Permission.Message.Format.Scoreboard permission;

    public ScoreboardModule(FileManager fileManager) {
        message = fileManager.getMessage().getFormat().getScoreboard();
        permission = fileManager.getPermission().getMessage().getFormat().getScoreboard();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public abstract void add(FPlayer fPlayer);

    public abstract void remove(FPlayer fPlayer);

}
