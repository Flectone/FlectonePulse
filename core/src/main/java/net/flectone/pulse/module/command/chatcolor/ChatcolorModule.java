package net.flectone.pulse.module.command.chatcolor;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatcolorModule extends AbstractModuleCommand<Localization.Command.Chatcolor> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final ColorConverter colorConverter;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::type);
        String promptColor = addPrompt(1, Localization.Command.Prompt::color);
        String promptPlayer = addPrompt(2, Localization.Command.Prompt::player);
        registerCommand(commandBuilder -> {
            commandBuilder = commandBuilder
                    .permission(permission().name())
                    .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion());

            for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
                commandBuilder = commandBuilder.optional(promptColor + " " + (i + 1), commandParserProvider.colorParser());
            }

            return commandBuilder.optional(promptPlayer, commandParserProvider.nativeMessageParser(), commandParserProvider.playerSuggestionPermission(true, permission().other()));
        });
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().other())
                .addAll(permission().colors().values());
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> Arrays.stream(FColor.Type.values())
                .filter(type -> permissionChecker.check(context.sender(), permission().colors().get(type.name())))
                .map(setting -> Suggestion.suggestion(setting.name().toLowerCase()))
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String type = getArgument(commandContext, 0);
        Optional<FColor.Type> fColorType = switch (type.toLowerCase()) {
            case "out" -> Optional.of(FColor.Type.OUT);
            case "see" -> Optional.of(FColor.Type.SEE);
            default -> Optional.empty();
        };

        if (fColorType.isEmpty() || !permissionChecker.check(fPlayer, permission().colors().get(fColorType.get().name()))) {
            sendErrorMessage(EventMetadata.<Localization.Command.Chatcolor>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatcolor::nullType)
                    .build()
            );

            return;
        }

        boolean hasOtherPermission = permissionChecker.check(fPlayer, permission().other());

        FPlayer fTarget = fPlayer;

        String promptPlayer = getPrompt(2);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);
        if (optionalTarget.isPresent() && hasOtherPermission) {
            fTarget = fPlayerService.getFPlayer(optionalTarget.get());
            if (fTarget.isUnknown()) {
                fTarget = fPlayer;
            }
        }

        String promptColor = getPrompt(1);
        Optional<String> optionalClear = commandContext.optional(promptColor + " 1");
        if (optionalClear.isPresent() && optionalClear.get().equalsIgnoreCase("clear")) {
            setColors(fTarget, fColorType.get(), Collections.emptySet());
            return;
        }

        Map<Integer, FColor> newFColors = new HashMap<>();
        fTarget.getFColors().getOrDefault(fColorType.get(), Set.of())
                .forEach(c -> newFColors.put(c.number(), c));

        for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
            Optional<String> optionalColor = commandContext.optional(promptColor + " " + (i + 1));
            if (optionalColor.isEmpty()) continue;

            String name = hasOtherPermission
                    ? optionalColor.get() // allow any input
                    : colorConverter.isCorrect(optionalColor.get().toLowerCase());
            if (name == null || name.equals("null")) continue;

            int number = i + 1;
            FColor fColor = new FColor(number, name);

            newFColors.put(number, fColor);
        }

        if (newFColors.isEmpty()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Chatcolor>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatcolor::nullColor)
                    .build()
            );

            return;
        }

        setColors(fTarget, fColorType.get(), new HashSet<>(newFColors.values()));
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_CHATCOLOR;
    }

    @Override
    public Command.Chatcolor config() {
        return fileFacade.command().chatcolor();
    }

    @Override
    public Permission.Command.Chatcolor permission() {
        return fileFacade.permission().command().chatcolor();
    }

    @Override
    public Localization.Command.Chatcolor localization(FEntity sender) {
        return fileFacade.localization(sender).command().chatcolor();
    }

    public Message.Format.FColor fColorConfig() {
        return fileFacade.message().format().fcolor();
    }

    private void setColors(FPlayer fPlayer, FColor.Type type, Set<FColor> newFColors) {
        Map<FColor.Type, Set<FColor>> fColors = fPlayer.getFColors();
        Set<FColor> oldFColors = fColors.getOrDefault(type, Collections.emptySet());

        UUID metadataUUID = UUID.randomUUID();

        if (!oldFColors.equals(newFColors)) {
            if (newFColors.isEmpty()) {
                fColors.remove(type);
            }  else {
                fColors.put(type, newFColors);
            }

            fPlayerService.saveColors(fPlayer);

            // update proxy players
            proxySender.send(fPlayer, MessageType.COMMAND_CHATCOLOR, dataOutputStream -> {}, metadataUUID);
        }

        sendMessageWithUpdatedColors(fPlayer, metadataUUID);
    }

    public void sendMessageWithUpdatedColors(FPlayer fPlayer, UUID metadataUUID) {
        sendMessage(EventMetadata.<Localization.Command.Chatcolor>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(Localization.Command.Chatcolor::format)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }
}
