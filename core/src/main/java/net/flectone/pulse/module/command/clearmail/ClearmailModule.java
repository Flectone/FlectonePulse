package net.flectone.pulse.module.command.clearmail;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.clearmail.model.ClearmailMetadata;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearmailModule implements ModuleCommand<Localization.Command.Clearmail> {

    private final Cache<UUID, List<String>> suggestionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(10)
            .build();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptId = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptId, commandParserProvider.integerParser(), SuggestionProvider.blockingStrings((commandContext, _) -> {
                    FPlayer fPlayer = commandContext.sender();

                    List<String> cache = suggestionCache.getIfPresent(fPlayer.uuid());
                    if (cache != null) return cache;

                    List<String> suggestion = socialService.getSenderMails(fPlayer)
                            .stream()
                            .map(mail -> String.valueOf(mail.id()))
                            .toList();
                    suggestionCache.put(fPlayer.uuid(), suggestion);

                    return suggestion;
                }))
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        int mailID = commandModuleController.getArgument(this, commandContext, 0);

        Optional<Mail> optionalMail = socialService.getSenderMails(fPlayer)
                .stream()
                .filter(mail -> mail.id() == mailID)
                .findAny();

        if (optionalMail.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Clearmail>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Clearmail::nullMail)
                    .build()
            );

            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(optionalMail.get().receiver());

        socialService.deleteMail(optionalMail.get());

        messageDispatcher.dispatch(this, ClearmailMetadata.<Localization.Command.Clearmail>builder()
                .base(EventMetadata.<Localization.Command.Clearmail>builder()
                        .sender(fPlayer)
                        .format(string -> Strings.CS.replaceOnce(string.format(), "<id>", String.valueOf(mailID)))
                        .destination(config().destination())
                        .message(optionalMail.get().message())
                        .sound(soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag(fResolver, fReceiver)
                        })
                        .build()
                )
                .mail(optionalMail.get())
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_CLEARMAIL;
    }

    @Override
    public Command.Clearmail config() {
        return fileFacade.command().clearmail();
    }

    @Override
    public Permission.Command.Clearmail permission() {
        return fileFacade.permission().command().clearmail();
    }

    @Override
    public Localization.Command.Clearmail localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().clearmail();
    }
}
