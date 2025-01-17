package net.flectone.pulse.module.command.mark;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.AdventureChatColorArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitMarkModule extends MarkModule {

    @Inject
    public BukkitMarkModule(FileManager fileManager,
                            net.flectone.pulse.module.message.contact.mark.MarkModule markModule,
                            CommandUtil commandUtil) {
        super(fileManager, markModule::mark, commandUtil);
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getColor();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executesPlayer(this::executesFPlayer)
                .then(new AdventureChatColorArgument(prompt)
                        .executesPlayer(this::executesFPlayer)
                )
                .override();
    }
}
