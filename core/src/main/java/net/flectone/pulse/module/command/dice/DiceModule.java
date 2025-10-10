package net.flectone.pulse.module.command.dice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.dice.model.DiceMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiceModule extends AbstractModuleCommand<Localization.Command.Dice> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;
    private final RandomUtil randomUtil;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .optional(promptMessage, commandParserProvider.integerParser(config().getMin(), config().getMax()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int min = config().getMin();
        int max = config().getMax();

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
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(cubes))
                .integration(string -> replaceResult(cubes, localization().getSymbols(), string))
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_DICE;
    }

    @Override
    public Command.Dice config() {
        return fileResolver.getCommand().getDice();
    }

    @Override
    public Permission.Command.Dice permission() {
        return fileResolver.getPermission().getCommand().getDice();
    }

    @Override
    public Localization.Command.Dice localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getDice();
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
