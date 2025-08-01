package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Optional;

@Singleton
public class ClearmailModule extends AbstractModuleCommand<Localization.Command.Clearmail> {

    private final Command.Clearmail command;
    private final Permission.Command.Clearmail permission;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public ClearmailModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getClearmail(), Command::getClearmail);

        this.command = fileResolver.getCommand().getClearmail();
        this.permission = fileResolver.getPermission().getCommand().getClearmail();
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptId = addPrompt(0, Localization.Command.Prompt::getId);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptId, commandParserProvider.integerParser(), SuggestionProvider.blockingStrings((commandContext, input) -> {
                    FPlayer fPlayer = commandContext.sender();

                    return fPlayerService.getSenderMails(fPlayer)
                            .stream()
                            .map(mail -> String.valueOf(mail.id()))
                            .toList();
                }))
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        int mailID = getArgument(commandContext, 0);

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
