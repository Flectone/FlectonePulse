package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.setting.ModerationListLocalizationSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationListSenderImpl implements ModerationListSender {

    private final FPlayerService fPlayerService;
    private final ModuleCommandController moduleCommandController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final ModerationService moderationService;

    @Override
    public void send(ModuleCommand module,
                     FPlayer fPlayer,
                     Function<FPlayer, ModerationListLocalizationSetting> localization,
                     CommandContext<FPlayer> commandContext,
                     Moderation.Type type,
                     int firstArgumentIndex,
                     int perPage,
                     String nextPageCommand,
                     Function<FPlayer, String> unmoderationCommand) {
        Optional<ListArgument> optionalListArgument = getListArgument(module, commandContext, firstArgumentIndex);
        if (optionalListArgument.isEmpty()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization.apply(fResolver).nullPlayer())
                            .build()
                    )
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
            localizationType = localization.apply(fPlayer).player();
        } else {
            size = moderationService.getTotalValidCount(type, moderationService.getServer(type));
            localizationType = localization.apply(fPlayer).global();
        }

        if (size == 0) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization.apply(fResolver).empty())
                            .build()
                    )
                    .build()
            );
            return;
        }

        int countPage = (int) Math.ceil((double) size / perPage);

        if (listArgument.page() > countPage || listArgument.page() < 1) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization.apply(fResolver).nullPage())
                            .build()
                    )
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

        StringBuilder stringBuilder = new StringBuilder();

        // header
        stringBuilder
                .append(Strings.CS.replace(localizationType.header(), "<count>", String.valueOf(size)))
                .append("<br>");

        TagResolver tagResolvers = TagResolver.empty();

        for (int i = 0; i < moderations.size(); i++) {
            Moderation moderation = moderations.get(i);
            FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());

            // line
            // <target:...> -> <target_x:...>
            // <moderator:...> -> <moderator_x:...>
            stringBuilder
                    .append(StringUtils.replaceEach(
                            moderationMessageFormatter.replacePlaceholders(Strings.CS.replace(localizationType.line(), "<command>", unmoderationCommand.apply(fTarget)), fPlayer, moderation),
                            new String[]{"<target", "<moderator"},
                            new String[]{"<target_" + i, "<moderator_" + i}
                    ))
                    .append("<br>");

            tagResolvers = TagResolver.resolver(
                    tagResolvers,
                    messagePipeline.targetTag("target_" + i, fPlayer, fTarget),
                    messagePipeline.targetTag("moderator_" + i, fPlayer, fPlayerService.getFPlayer(moderation.moderator()))
            );
        }

        // footer
        stringBuilder.append(StringUtils.replaceEach(
                localizationType.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{nextPageCommand, String.valueOf(listArgument.page() - 1), String.valueOf(listArgument.page() + 1), String.valueOf(listArgument.page()), String.valueOf(countPage)}
        ));

        String message = stringBuilder.toString();
        TagResolver finalTagResolver = tagResolvers;

        messageDispatcher.dispatch(module, EventMetadata.builder()
                .sound(module.soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(message)
                        .tagResolver(finalTagResolver)
                        .build()
                )
                .build()
        );
    }

    @Override
    public Optional<ListArgument> getListArgument(ModuleCommand command, CommandContext<FPlayer> commandContext, int startIndex) {
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

}
