package net.flectone.pulse.module.command.reply;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitReplyModule extends ReplyModule {

    @Inject
    public BukkitReplyModule(FileManager fileManager,
                             TellModule tellModule,
                             CommandUtil componentUtil) {
        super(fileManager, tellModule, componentUtil);
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getMessage();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new GreedyStringArgument(prompt)
                        .executesPlayer(this::executesFPlayer)
                )
                .override();
    }
}
