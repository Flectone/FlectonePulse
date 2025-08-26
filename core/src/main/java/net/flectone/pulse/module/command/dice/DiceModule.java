package net.flectone.pulse.module.command.dice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.dice.model.DiceMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class DiceModule extends AbstractModuleCommand<Localization.Command.Dice> {

    private final Command.Dice command;
    private final Permission.Command.Dice permission;
    private final CommandParserProvider commandParserProvider;
    private final RandomUtil randomUtil;

    @Inject
    public DiceModule(FileResolver fileResolver,
                      CommandParserProvider commandParserProvider,
                      RandomUtil randomUtil) {
        super(localization -> localization.getCommand().getDice(), Command::getDice, fPlayer -> fPlayer.isSetting(FPlayer.Setting.DICE), MessageType.COMMAND_DICE);

        this.command = fileResolver.getCommand().getDice();
        this.permission = fileResolver.getPermission().getCommand().getDice();
        this.commandParserProvider = commandParserProvider;
        this.randomUtil = randomUtil;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .optional(promptMessage, commandParserProvider.integerParser(command.getMin(), command.getMax()))
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();

        String promptMessage = getPrompt(0);
        Optional<Integer> optionalNumber = commandContext.optional(promptMessage);

        int number = optionalNumber.orElse(min);

        List<Integer> cubes = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            cubes.add(randomUtil.nextInt(min, max + 1));
        }

        sendMessage(DiceMetadata.<Localization.Command.Dice>builder()
                .sender(fPlayer)
                .cubes(cubes)
                .format(dice -> replaceResult(cubes, dice.getSymbols(), dice.getFormat()))
                .range(command.getRange())
                .destination(command.getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(cubes))
                .integration(string -> replaceResult(cubes, resolveLocalization().getSymbols(), string))
                .build()
        );
    }

    public String replaceResult(List<Integer> cubes, Map<Integer, String> symbols, String format) {
        StringBuilder stringBuilder = new StringBuilder();
        int sum = 0;

        for (Integer integer : cubes) {
            sum += integer;

            stringBuilder
                    .append(symbols.get(integer))
                    .append(" ");
        }

        return StringUtils.replaceEach(
                format,
                new String[]{"<sum>", "<message>"},
                new String[]{String.valueOf(sum), stringBuilder.toString().trim()}
        );
    }
}
