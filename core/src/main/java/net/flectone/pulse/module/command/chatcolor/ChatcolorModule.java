package net.flectone.pulse.module.command.chatcolor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;

import java.util.*;

@Singleton
public class ChatcolorModule extends AbstractModuleCommand<Localization.Command.Chatcolor> {

    private final Message.Format.Color color;
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

        this.color = fileResolver.getMessage().getFormat().getColor();
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

        String promptColor = addPrompt(0, Localization.Command.Prompt::getColor);
        registerCommand(commandBuilder -> {
            commandBuilder = commandBuilder
                    .permission(permission.getName());

            for (int i = 0; i < color.getValues().size(); i++) {
                commandBuilder = commandBuilder.optional(promptColor + " " + (i + 1), commandParserProvider.colorParser());
            }

            return commandBuilder;
        });

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String[] inputColors = null;

        if (commandContext.rawInput().input().split(" ").length != 1) {
            String promptColor = getArgument(commandContext, 0);

            List<String> inputList = new ArrayList<>();
            for (int i = 0; i < color.getValues().size(); i++) {
                Optional<String> optionalColor = commandContext.optional(promptColor + " " + (i + 1));
                if (optionalColor.isEmpty()) continue;

                inputList.add(optionalColor.get());
            }

            if (!inputList.isEmpty()) {
                inputColors = inputList.toArray(new String[0]);
            }
        }

        if (inputColors == null) {
            builder(fPlayer)
                    .format(Localization.Command.Chatcolor::getNullColor)
                    .sendBuilt();
            return;
        }

        if (inputColors[0].equalsIgnoreCase("clear")) {
            setColors(fPlayer, null);
            return;
        }

        FPlayer fTarget = fPlayer;

        if (permissionChecker.check(fPlayer, permission.getOther()) && inputColors.length > 1) {
            String player = inputColors[0];
            if (!player.startsWith("#") && !player.startsWith("&") && !player.equalsIgnoreCase("clear")) {
                fTarget = fPlayerService.getFPlayer(player);

                if (fTarget.isUnknown()) {
                    builder(fPlayer)
                            .format(Localization.Command.Chatcolor::getNullPlayer)
                            .sendBuilt();
                    return;
                }

                String[] finalInputColors = inputColors;
                proxySender.send(fTarget, MessageType.COMMAND_CHATCOLOR, dataOutputStream ->
                        dataOutputStream.writeUTF(String.join(" ", finalInputColors))
                );

                if (inputColors[1].equalsIgnoreCase("clear")) {
                    setColors(fTarget, null);
                    return;
                }

                inputColors = Arrays.copyOfRange(inputColors, 1, inputColors.length);
            }
        }

        Map<String, String> defaultColors = color.getValues();
        Iterator<String> mapKeyIterator = defaultColors.keySet().iterator();

        Map<String, String> newColors = new HashMap<>();

        for (int i = 0; i < inputColors.length; i++) {
            if (i >= defaultColors.size()) {
                break;
            }

            String inputColor = colorConverter.convertOrDefault(inputColors[i], null);

            if (inputColor == null) {
                builder(fPlayer)
                        .format(Localization.Command.Chatcolor::getNullColor)
                        .sendBuilt();
                return;
            }

            newColors.put(mapKeyIterator.next(), inputColor);
        }

        setColors(fTarget, newColors);
    }

    private void setColors(FPlayer fPlayer, Map<String, String> newColors) {
        fPlayer.getColors().clear();

        if (newColors != null) {
            fPlayer.getColors().putAll(newColors);
        }

        fPlayerService.saveColors(fPlayer);

        builder(fPlayer)
                .destination(command.getDestination())
                .format((fResolver, s) -> s.getFormat())
                .sound(getSound())
                .sendBuilt();
    }
}
