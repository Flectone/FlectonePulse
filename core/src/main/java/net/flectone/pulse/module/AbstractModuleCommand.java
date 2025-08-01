package net.flectone.pulse.module;

import com.google.inject.Inject;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.meta.CommandMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class AbstractModuleCommand<M extends Localization.Localizable> extends AbstractModuleLocalization<M> implements CommandExecutionHandler<FPlayer> {

    private final List<String> prompts = new ArrayList<>();
    private final Predicate<FPlayer> commandPredicate;
    private final Function<Command, Command.ICommandFile> commandFunction;

    @Inject private FileResolver fileResolver;
    @Inject private CommandRegistry commandParserProvider;

    protected AbstractModuleCommand(Function<Localization, M> messageFunction,
                                    Function<Command, Command.ICommandFile> commandFunction) {
        this(messageFunction, commandFunction, null);
    }

    protected AbstractModuleCommand(Function<Localization, M> messageFunction,
                                    Function<Command, Command.ICommandFile> commandFunction,
                                    Predicate<FPlayer> commandPredicate) {
        super(messageFunction);

        this.commandFunction = commandFunction;
        this.commandPredicate = commandPredicate;
    }

    protected void registerCommand(UnaryOperator<org.incendo.cloud.Command.Builder<FPlayer>> commandBuilderOperator) {
        List<String> aliases = resolveCommand().getAliases();
        String commandName = getCommandName();

        commandParserProvider.registerCommand(manager ->
                commandBuilderOperator
                        .apply(manager.commandBuilder(commandName, aliases, CommandMeta.empty()))
                        .handler(this)
        );
    }

    protected void registerCustomCommand(Function<CommandManager<FPlayer>, org.incendo.cloud.Command.Builder<FPlayer>> builder) {
        commandParserProvider.registerCommand(builder);
    }

    // all prompt methods for solving the problems of a non-existent argument
    // when changing the plugin language at runtime
    protected void clearPrompts() {
        if (fileResolver.getConfig().isUnregisterOwnCommands()) {
            prompts.clear();
        }
    }

    protected String addPrompt(int index, Function<Localization.Command.Prompt, String> promptLocalization) {
        // this command already registered and ignored
        if (!prompts.isEmpty() && prompts.size() - 1 != index) return "unknown";

        String prompt = promptLocalization.apply(fileResolver.getLocalization().getCommand().getPrompt());
        prompts.add(prompt);

        return prompt;
    }

    protected String getPrompt(int index) {
        if (prompts.size() - 1 < index) throw new IllegalArgumentException("Argument at index " + index + " is not registered in the " + getCommandName() + "  command");

        return prompts.get(index);
    }

    protected <V extends @NonNull Object> V getArgument(CommandContext<FPlayer> commandContext, int promptIndex) {
        String prompt = getPrompt(promptIndex);
        return commandContext.get(prompt);
    }

    public String getCommandName() {
        List<String> aliases = resolveCommand().getAliases();
        if (aliases.isEmpty()) return "flectonepulsenull";

        return aliases.getFirst();
    }

    @Override
    protected boolean isConfigEnable() {
        return resolveCommand().isEnable();
    }

    @Override
    public void onDisable() {
        clearPrompts();
    }

    @Override
    public void execute(@NonNull CommandContext<FPlayer> commandContext) {
        execute(commandContext.sender(), commandContext);
    }

    public abstract void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    public boolean checkDisable(FEntity entity, @NotNull FEntity receiver, DisableSource action) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (commandPredicate == null || commandPredicate.test(fReceiver)) return false;

        return super.sendDisableMessage(entity, fReceiver, action);
    }

    @Override
    public Predicate<FPlayer> rangeFilter(FEntity sender, Range range) {
        Predicate<FPlayer> filter = super.rangeFilter(sender, range);

        if (range.is(Range.Type.PLAYER)) {
            return filter;
        }

        return this.commandPredicate == null ? filter : filter.and(commandPredicate);
    }

    private Command.ICommandFile resolveCommand() {
        return commandFunction.apply(fileResolver.getCommand());
    }
}
