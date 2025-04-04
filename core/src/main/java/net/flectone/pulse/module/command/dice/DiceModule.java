package net.flectone.pulse.module.command.dice;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class DiceModule extends AbstractModuleCommand<Localization.Command.Dice> {

    @Getter private final Command.Dice command;
    private final Permission.Command.Dice permission;

    private final CommandRegistry commandRegistry;
    private final RandomUtil randomUtil;
    private final Gson gson;

    @Inject
    public DiceModule(FileManager fileManager,
                      CommandRegistry commandRegistry,
                      RandomUtil randomUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getDice(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.DICE));

        this.commandRegistry = commandRegistry;
        this.randomUtil = randomUtil;
        this.gson = gson;

        command = fileManager.getCommand().getDice();
        permission = fileManager.getPermission().getCommand().getDice();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptMessage, commandRegistry.integerParser(command.getMin(), command.getMax()))
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();

        String promptMessage = getPrompt().getMessage();
        Optional<Integer> optionalNumber = commandContext.optional(promptMessage);

        int number = optionalNumber.orElse(min);

        List<Integer> cubes = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            cubes.add(randomUtil.nextInt(min, max + 1));
        }

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_DICE)
                .format(replaceResult(cubes))
                .proxy(output -> output.writeUTF(gson.toJson(cubes)))
                .integration(s -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    int sum = 0;

                    Map<Integer, String> symbols = resolveLocalization().getSymbols();

                    for (Integer integer : cubes) {
                        sum += integer;

                        stringBuilder
                                .append(symbols.get(integer))
                                .append(" ");
                    }

                    return s.replace("<sum>", String.valueOf(sum))
                            .replace("<message>", stringBuilder.toString());
                })
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Dice, String> replaceResult(List<Integer> cubes) {
        return message -> {
            StringBuilder stringBuilder = new StringBuilder();
            int sum = 0;

            Map<Integer, String> symbols = message.getSymbols();

            for (Integer integer : cubes) {
                sum += integer;

                stringBuilder
                        .append(symbols.get(integer))
                        .append(" ");
            }

            return message.getFormat()
                    .replace("<sum>", String.valueOf(sum))
                    .replace("<message>", stringBuilder.toString().trim());
        };
    }
}
