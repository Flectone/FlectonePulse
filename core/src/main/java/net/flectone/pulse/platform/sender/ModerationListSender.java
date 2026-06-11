package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.config.setting.ModerationListLocalizationSetting;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationListSender {

    private final FPlayerService fPlayerService;
    private final ModuleCommandController moduleCommandController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final ModerationService moderationService;
    private final EventDispatcher eventDispatcher;
    private final SoundPlayer soundPlayer;

    public <L extends LocalizationSetting & ModerationListLocalizationSetting> void send(ModuleCommand<L> module,
                                                                                         FPlayer fPlayer,
                                                                                         CommandContext<FPlayer> commandContext,
                                                                                         Moderation.Type type,
                                                                                         int firstArgumentIndex,
                                                                                         int perPage,
                                                                                         String nextPageCommand,
                                                                                         Function<FPlayer, String> unmoderationCommand) {
        Optional<ListArgument> optionalListArgument = getListArgument(module, commandContext, firstArgumentIndex);
        if (optionalListArgument.isEmpty()) {
            messageDispatcher.dispatchError(module, EventMetadata.<L>builder()
                    .sender(fPlayer)
                    .format(ModerationListLocalizationSetting::nullPlayer)
                    .build()
            );
            return;
        }

        ListArgument listArgument = optionalListArgument.get();

        int size;
        Localization.ListTypeMessage localizationType;
        if (listArgument.target() != null) {
            nextPageCommand+= " " + listArgument.target().name();
            size = moderationService.getTotalValidCount(listArgument.target(), type, moderationService.getServer(type));
            localizationType = module.localization(fPlayer).player();
        } else {
            size = moderationService.getTotalValidCount(type, moderationService.getServer(type));
            localizationType = module.localization(fPlayer).global();
        }

        if (size == 0) {
            messageDispatcher.dispatchError(module, EventMetadata.<L>builder()
                    .sender(fPlayer)
                    .format(ModerationListLocalizationSetting::empty)
                    .build()
            );
            return;
        }

        int countPage = (int) Math.ceil((double) size / perPage);

        if (listArgument.page() > countPage || listArgument.page() < 1) {
            messageDispatcher.dispatchError(module, EventMetadata.<L>builder()
                    .sender(fPlayer)
                    .format(ModerationListLocalizationSetting::nullPage)
                    .build()
            );
            return;
        }

        List<Moderation> moderations;
        if (listArgument.target() != null) {
            moderations = moderationService.getValid(listArgument.target(), type, perPage, (listArgument.page() - 1) * perPage);
        } else {
            moderations = moderationService.getValid(type, perPage, (listArgument.page() - 1) * perPage);
        }

        String header = Strings.CS.replace(localizationType.header(), "<count>", String.valueOf(size));
        Component component = messagePipeline.build(MessageContext.builder().sender(fPlayer).message(header).build())
                .append(Component.newline());

        for (Moderation moderation : moderations) {
            FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());

            String line = moderationMessageFormatter.replacePlaceholders(
                    Strings.CS.replace(localizationType.line(), "<command>", unmoderationCommand.apply(fTarget)),
                    fPlayer,
                    moderation
            );

            component = component
                    .append(messagePipeline.build(MessageContext.builder()
                            .sender(fPlayer)
                            .message(line)
                            .tagResolvers(
                                    messagePipeline.targetTag(fPlayer, fTarget),
                                    messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(moderation.moderator()))
                            )
                            .build()
                    ))
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localizationType.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{nextPageCommand, String.valueOf(listArgument.page() - 1), String.valueOf(listArgument.page() + 1), String.valueOf(listArgument.page()), String.valueOf(countPage)}
        );

        component = component.append(messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(footer)
                .build()
        ));

        MessageSendEvent messageSendEvent = eventDispatcher.dispatch(new MessageSendEvent(module.name(), fPlayer, component));
        if (!messageSendEvent.cancelled()) {
            soundPlayer.play(module.soundOrThrow(), fPlayer);
        }
    }

    public Optional<ListArgument> getListArgument(ModuleCommand<?> command,
                                                  CommandContext<FPlayer> commandContext,
                                                  int startIndex) {
        String promptPlayer = moduleCommandController.getPrompt(command, startIndex);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isEmpty()) return Optional.of(new ListArgument(null, 1));

        String playerName = optionalPlayer.get();
        if (StringUtils.isNumeric(playerName)) {
            int page = Integer.parseInt(playerName);
            return Optional.of(new ListArgument(null, page));
        }

        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (fTarget.isUnknown()) return Optional.empty();

        String promptNumber = moduleCommandController.getPrompt(command, startIndex + 1);
        Optional<Integer> optionalPage = commandContext.optional(promptNumber);
        if (optionalPage.isPresent()) {
            int page = optionalPage.get();
            return Optional.of(new ListArgument(fTarget, page));
        }

        if (startIndex + 2 >= moduleCommandController.getPrompts(command).size()) {
            return Optional.of(new ListArgument(fTarget, 1));
        }

        String promptTime = moduleCommandController.getPrompt(command, startIndex + 2);

        try {
            Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptNumber);

            int page = optionalTime.map(pair -> StringUtils.isNumeric(pair.getRight().trim()) ? Integer.parseInt(pair.getRight().trim()) : 1).orElse(1);
            return Optional.of(new ListArgument(fTarget, page));
        } catch (ClassCastException _) {
            return Optional.empty();
        }
    }

    public record ListArgument(
            FPlayer target,
            int page
    ){}


}
