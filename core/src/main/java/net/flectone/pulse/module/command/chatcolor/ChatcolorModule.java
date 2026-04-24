package net.flectone.pulse.module.command.chatcolor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatcolorModule implements ModuleCommand<Localization.Command.Chatcolor> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final ColorConverter colorConverter;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptColor = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::color);
        String promptPlayer = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, commandBuilder -> {
            commandBuilder = commandBuilder
                    .permission(permission().name())
                    .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion());

            for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
                commandBuilder = commandBuilder.optional(promptColor + " " + (i + 1), commandParserProvider.colorParser());
            }

            return commandBuilder.optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission().other()));
        });
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder()
                .add(permission().other())
                .addAll(permission().colors().values());
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, _) -> Arrays.stream(FColor.Type.values())
                .filter(type -> permissionChecker.check(context.sender(), permission().colors().get(type.name())))
                .map(setting -> Suggestion.suggestion(setting.name().toLowerCase()))
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String type = commandModuleController.getArgument(this, commandContext, 0);
        Optional<FColor.Type> fColorType = switch (type.toLowerCase()) {
            case "out" -> Optional.of(FColor.Type.OUT);
            case "see" -> Optional.of(FColor.Type.SEE);
            default -> Optional.empty();
        };

        if (fColorType.isEmpty() || !permissionChecker.check(fPlayer, permission().colors().get(fColorType.get().name()))) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Chatcolor>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatcolor::nullType)
                    .build()
            );

            return;
        }

        boolean hasOtherPermission = permissionChecker.check(fPlayer, permission().other());

        FPlayer fTarget = fPlayer;

        String promptPlayer = commandModuleController.getPrompt(this, 2);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);
        if (optionalTarget.isPresent() && hasOtherPermission) {
            fTarget = fPlayerService.getFPlayer(optionalTarget.get());
            if (fTarget.isUnknown()) {
                fTarget = fPlayer;
            }
        }

        String promptColor = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalClear = commandContext.optional(promptColor + " 1");
        if (optionalClear.isPresent() && optionalClear.get().equalsIgnoreCase("clear")) {
            setColors(fTarget, fColorType.get(), Collections.emptySet());
            return;
        }

        Int2ObjectArrayMap<FColor> newFColors = new Int2ObjectArrayMap<>();
        fTarget.fColors().getOrDefault(fColorType.get(), Collections.emptySet())
                .forEach(fColor -> newFColors.put(fColor.number(), fColor));

        for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
            Optional<String> optionalColor = commandContext.optional(promptColor + " " + (i + 1));
            if (optionalColor.isEmpty()) continue;

            String name = hasOtherPermission
                    ? optionalColor.get() // allow any input
                    : colorConverter.isCorrect(optionalColor.get().toLowerCase());
            if (name == null || name.equals("null")) continue;

            int number = i + 1;
            FColor fColor = new FColor(number, StringUtils.left(name, 255));

            newFColors.put(number, fColor);
        }

        if (newFColors.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Chatcolor>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatcolor::nullColor)
                    .build()
            );

            return;
        }

        setColors(fTarget, fColorType.get(), new ObjectOpenHashSet<>(newFColors.values()));
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_CHATCOLOR;
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
        Map<FColor.Type, Set<FColor>> fColors = fPlayer.fColors();
        Set<FColor> oldFColors = fColors.getOrDefault(type, Collections.emptySet());

        UUID metadataUUID = UUID.randomUUID();

        if (!oldFColors.equals(newFColors)) {
            fPlayer = fPlayer.withFColors(type, newFColors);
            fPlayerService.saveColors(fPlayer);

            // update proxy players
            proxySender.send(fPlayer, ModuleName.COMMAND_CHATCOLOR, _ -> {}, metadataUUID);
        }

        sendMessageWithUpdatedColors(fPlayer, metadataUUID);
    }

    public void sendMessageWithUpdatedColors(FPlayer fPlayer, UUID metadataUUID) {
        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Chatcolor>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(Localization.Command.Chatcolor::format)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }
}
