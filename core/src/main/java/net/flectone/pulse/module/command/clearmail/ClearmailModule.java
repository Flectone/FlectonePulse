package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.clearmail.model.ClearmailMetadata;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearmailModule extends AbstractModuleCommand<Localization.Command.Clearmail> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptId = addPrompt(0, Localization.Command.Prompt::getId);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptId, commandParserProvider.integerParser(), SuggestionProvider.blockingStrings((commandContext, input) -> {
                    FPlayer fPlayer = commandContext.sender();

                    return fPlayerService.getSenderMails(fPlayer)
                            .stream()
                            .map(mail -> String.valueOf(mail.id()))
                            .toList();
                }))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int mailID = getArgument(commandContext, 0);

        Optional<Mail> optionalMail = fPlayerService.getSenderMails(fPlayer)
                .stream()
                .filter(mail -> mail.id() == mailID)
                .findAny();

        if (optionalMail.isEmpty()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Clearmail::getNullMail)
                    .build()
            );

            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(optionalMail.get().receiver());

        fPlayerService.deleteMail(optionalMail.get());

        sendMessage(ClearmailMetadata.<Localization.Command.Clearmail>builder()
                .sender(fPlayer)
                .format(string -> Strings.CS.replaceOnce(string.getFormat(), "<id>", String.valueOf(mailID)))
                .mail(optionalMail.get())
                .destination(config().getDestination())
                .message(optionalMail.get().message())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_CLEARMAIL;
    }

    @Override
    public Command.Clearmail config() {
        return fileResolver.getCommand().getClearmail();
    }

    @Override
    public Permission.Command.Clearmail permission() {
        return fileResolver.getPermission().getCommand().getClearmail();
    }

    @Override
    public Localization.Command.Clearmail localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getClearmail();
    }
}
