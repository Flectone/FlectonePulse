package net.flectone.pulse.module.command.dice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.dice.model.DiceMetadata;
import net.flectone.pulse.platform.controller.CommandModuleController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiceModule implements ModuleCommand<Localization.Command.Dice> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final RandomUtil randomUtil;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptMessage, commandParserProvider.integerParser(config().min(), config().max()))
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        int min = config().min();
        int max = config().max();

        String promptMessage = commandModuleController.getPrompt(this, 0);
        Optional<Integer> optionalNumber = commandContext.optional(promptMessage);

        int number = optionalNumber.orElse(min);

        List<Integer> cubes = new ObjectArrayList<>();
        for (int i = 0; i < number; i++) {
            cubes.add(randomUtil.nextInt(min, max + 1));
        }

        messageDispatcher.dispatch(this, DiceMetadata.<Localization.Command.Dice>builder()
                .base(EventMetadata.<Localization.Command.Dice>builder()
                        .sender(fPlayer)
                        .format(dice -> replaceResult(cubes, dice.symbols(), dice.format()))
                        .range(config().range())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> dataOutputStream.writeAsJson(cubes))
                        .integration(string -> replaceResult(cubes, localization().symbols(), string))
                        .build()
                )
                .cubes(cubes)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_DICE;
    }

    @Override
    public Command.Dice config() {
        return fileFacade.command().dice();
    }

    @Override
    public Permission.Command.Dice permission() {
        return fileFacade.permission().command().dice();
    }

    @Override
    public Localization.Command.Dice localization(FEntity sender) {
        return fileFacade.localization(sender).command().dice();
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
