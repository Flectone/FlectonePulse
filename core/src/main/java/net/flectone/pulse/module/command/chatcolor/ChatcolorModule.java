package net.flectone.pulse.module.command.chatcolor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.*;

@Singleton
public class ChatcolorModule extends AbstractModuleCommand<Localization.Command.Chatcolor> {

    private final Message.Format.FColor fColorMessage;
    private final Permission.Message.Format.FColor fColorPermission;
    private final Command.Chatcolor command;
    private final Permission.Command.Chatcolor permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final ColorConverter colorConverter;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public ChatcolorModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PermissionChecker permissionChecker,
                           ProxySender proxySender,
                           ColorConverter colorConverter,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getChatcolor(), Command::getChatcolor);

        this.fColorMessage = fileResolver.getMessage().getFormat().getFcolor();
        this.fColorPermission = fileResolver.getPermission().getMessage().getFormat().getFcolor();
        this.command = fileResolver.getCommand().getChatcolor();
        this.permission = fileResolver.getPermission().getCommand().getChatcolor();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.proxySender = proxySender;
        this.colorConverter = colorConverter;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        String promptType = addPrompt(0, Localization.Command.Prompt::getType);
        String promptColor = addPrompt(1, Localization.Command.Prompt::getColor);
        String promptPlayer = addPrompt(2, Localization.Command.Prompt::getPlayer);
        registerCommand(commandBuilder -> {
            commandBuilder = commandBuilder
                    .permission(permission.getName())
                    .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion());

            for (int i = 0; i < fColorMessage.getDefaultColors().size(); i++) {
                commandBuilder = commandBuilder.optional(promptColor + " " + (i + 1), commandParserProvider.colorParser());
            }

            return commandBuilder.optional(promptPlayer, commandParserProvider.nativeMessageParser(), commandParserProvider.playerSuggestionPermission(true, permission.getOther()));
        });

        addPredicate(this::checkCooldown);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> Arrays.stream(FColor.Type.values())
                .filter(type -> permissionChecker.check(context.sender(), fColorPermission.getTypes().get(type)))
                .map(setting -> Suggestion.suggestion(setting.name().toLowerCase()))
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        String type = getArgument(commandContext, 0);
        Optional<FColor.Type> fColorType = FColor.Type.fromString(type);
        if (fColorType.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Chatcolor::getNullType)
                    .sendBuilt();
            return;
        }

        FPlayer fTarget = fPlayer;

        String promptPlayer = getPrompt(2);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);
        if (optionalTarget.isPresent()) {
            fTarget = fPlayerService.getFPlayer(optionalTarget.get());
            if (fTarget.isUnknown()) {
                fTarget = fPlayer;
            }
        }

        String promptColor = getPrompt(1);
        Optional<String> optionalClear = commandContext.optional(promptColor + " 1");
        if (optionalClear.isPresent() && optionalClear.get().equalsIgnoreCase("clear")) {
            setColors(fTarget, fColorType.get(), null);
            return;
        }

        Set<FColor> newFColors = HashSet.newHashSet(fColorMessage.getDefaultColors().size());
        for (int i = 0; i < fColorMessage.getDefaultColors().size(); i++) {
            Optional<String> optionalColor = commandContext.optional(promptColor + " " + (i + 1));
            if (optionalColor.isEmpty()) break;

            int number = i + 1;
            String name = colorConverter.convert(optionalColor.get());
            if (name == null) break;

            FColor fColor = new FColor(number, name);
            newFColors.add(fColor);
        }

        if (newFColors.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Chatcolor::getNullColor)
                    .sendBuilt();
            return;
        }

        setColors(fTarget, fColorType.get(), newFColors);
    }

    private void setColors(FPlayer fPlayer, FColor.Type type, Set<FColor> newFColors) {
        Map<FColor.Type, Set<FColor>> fColors = fPlayer.getFColors();
        Set<FColor> oldFColors = fColors.getOrDefault(type, Collections.emptySet());

        if (!oldFColors.equals(newFColors)) {
            if (newFColors == null || newFColors.isEmpty()) {
                fColors.remove(type);
            }  else {
                if (newFColors.size() < oldFColors.size()) {
                    oldFColors.stream()
                            .skip(newFColors.size())
                            .forEach(newFColors::add);
                }

                fColors.put(type, newFColors);
            }

            fPlayerService.saveColors(fPlayer);

            // update proxy players
            proxySender.send(fPlayer, MessageType.COMMAND_CHATCOLOR, dataOutputStream -> {});
        }

        builder(fPlayer)
                .destination(command.getDestination())
                .format(Localization.Command.Chatcolor::getFormat)
                .sound(getSound())
                .sendBuilt();
    }
}
