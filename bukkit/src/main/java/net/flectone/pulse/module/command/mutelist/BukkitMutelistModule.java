package net.flectone.pulse.module.command.mutelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitMutelistModule extends MutelistModule {

    private final ModerationService moderationService;

    @Inject
    public BukkitMutelistModule(FileManager fileManager,
                                FPlayerService fPlayerService,
                                ModerationService moderationService,
                                ModerationUtil moderationUtil,
                                UnmuteModule unmuteModule,
                                ComponentUtil componentUtil,
                                CommandUtil commandUtil,
                                MessageSender messageSender) {
        super(fileManager, fPlayerService, moderationService, moderationUtil, unmuteModule, componentUtil, commandUtil, messageSender);

        this.moderationService = moderationService;
    }

    @Override
    public void createCommand() {
        String promptPlayer = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(promptNumber).setOptional(true)
                        .executesPlayer(this::executesFPlayer)
                )
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
                                CompletableFuture.supplyAsync(() -> moderationService.getValidNames(Moderation.Type.MUTE))))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executesPlayer(this::executesFPlayer)
                        )
                )
                .override();
    }
}
