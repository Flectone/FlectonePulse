package net.flectone.pulse.module.command;

import dev.jorel.commandapi.CommandTree;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.file.Permission;

import java.util.List;

public class FCommand extends CommandTree {

    public FCommand(String commandName) {
        super(commandName != null ? commandName : "UNKNOWN");
        withShortDescription("flectonepulse");
    }

    public FCommand withAliases(List<String> aliases) {
        return (FCommand) withAliases(aliases.toArray(new String[0]));
    }

    public FCommand withPermission(Permission.IPermission permission) {
        return (FCommand) withPermission(permission.getName());
    }

    @Sync
    @Override
    public void override() {
        super.override();
    }
}
