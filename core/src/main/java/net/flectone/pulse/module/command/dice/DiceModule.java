package net.flectone.pulse.module.command.dice;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class DiceModule extends AbstractModuleCommand<Localization.Command.Dice> {

    @Getter
    private final Command.Dice command;
    @Getter
    private final Permission.Command.Dice permission;

    private final CommandUtil commandUtil;
    private final RandomUtil randomUtil;
    private final Gson gson;

    public DiceModule(FileManager fileManager,
                      CommandUtil commandUtil,
                      RandomUtil randomUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getDice(), fPlayer -> fPlayer.is(FPlayer.Setting.DICE));

        this.commandUtil = commandUtil;
        this.randomUtil = randomUtil;
        this.gson = gson;

        command = fileManager.getCommand().getDice();
        permission = fileManager.getPermission().getCommand().getDice();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        int number = commandUtil.getByClassOrDefault(0, Integer.class, 1, arguments);

        int min = command.getMin();
        int max = command.getMax();

        List<Integer> cubes = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            cubes.add(randomUtil.nextInt(min, max + 1));
        }

        builder(fPlayer)
                .range(command.getRange())
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
                    .replace("<message>", stringBuilder.toString());
        };
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
