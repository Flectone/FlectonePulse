package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Optional;

@Singleton
public class ClearmailModule extends AbstractModuleCommand<Localization.Command.Clearmail> {

    private final Command.Clearmail command;
    private final Permission.Command.Clearmail permission;

    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;

    @Inject
    public ClearmailModule(FileManager fileManager,
                           FPlayerService fPlayerService,
                           CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getClearmail(), null);

        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getClearmail();
        permission = fileManager.getPermission().getCommand().getClearmail();

        addPredicate(this::checkCooldown);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptId = getPrompt().getId();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptId, commandRegistry.integerParser(), SuggestionProvider.blockingStrings((commandContext, input) -> {
                            FPlayer fPlayer = commandContext.sender();

                            return fPlayerService.getSenderMails(fPlayer)
                                    .stream()
                                    .map(mail -> String.valueOf(mail.id()))
                                    .toList();
                        }))
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptId = getPrompt().getId();
        int mailID = commandContext.get(promptId);

        Optional<Mail> optionalMail = fPlayerService.getSenderMails(fPlayer)
                .stream()
                .filter(mail -> mail.id() == mailID)
                .findAny();

        if (optionalMail.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Clearmail::getNullMail)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(optionalMail.get().receiver());

        fPlayerService.deleteMail(optionalMail.get());

        builder(fReceiver)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat().replaceFirst("<id>", String.valueOf(mailID)))
                .message(optionalMail.get().message())
                .sound(getSound())
                .sendBuilt();
    }
}
