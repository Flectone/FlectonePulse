package net.flectone.pulse.module.command.try_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.function.Function;

@Singleton
public class TryModule extends AbstractModuleCommand<Localization.Command.Try> {

    @Getter private final Command.Try command;
    private final Permission.Command.Try permission;
    private final RandomUtil randomUtil;
    private final CommandRegistry commandRegistry;

    @Inject
    public TryModule(FileResolver fileResolver,
                     RandomUtil randomUtil,
                     CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getTry(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TRY));

        this.command = fileResolver.getCommand().getTry();
        this.permission = fileResolver.getPermission().getCommand().getTry();
        this.randomUtil = randomUtil;
        this.commandRegistry = commandRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();

        int random = randomUtil.nextInt(min, max);

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_TRY)
                .format(replacePercent(random))
                .message((fResolver, s)  -> message)
                .proxy(output -> {
                    output.writeInt(random);
                    output.writeUTF(message);
                })
                .integration(s -> s
                        .replace("<message>", message)
                        .replace("<percent>", String.valueOf(random))
                )
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Try, String> replacePercent(int value) {
        return message -> (value >= command.getGood() ? message.getFormatTrue() : message.getFormatFalse())
                .replace("<percent>", String.valueOf(value));
    }
}