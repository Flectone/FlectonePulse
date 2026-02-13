package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.util.file.FileFacade;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class AbstractModuleCommand<M extends LocalizationSetting> extends AbstractModuleLocalization<M> implements CommandExecutionHandler<FPlayer> {

    private final List<String> prompts = new ObjectArrayList<>();

    @Inject private FileFacade fileFacade;
    @Inject private Provider<CommandRegistry> commandRegistryProvider;

    protected void registerCommand(UnaryOperator<org.incendo.cloud.Command.Builder<FPlayer>> commandBuilderOperator) {
        List<String> aliases = config().aliases();
        String commandName = getCommandName();

        commandRegistryProvider.get().registerCommand(manager ->
                commandBuilderOperator
                        .apply(manager.commandBuilder(commandName, aliases, CommandMeta.empty()))
                        .handler(this)
        );
    }

    protected void registerCustomCommand(Function<CommandManager<FPlayer>, org.incendo.cloud.Command.Builder<FPlayer>> builder) {
        commandRegistryProvider.get().registerCommand(builder);
    }

    // all prompt methods for solving the problems of a non-existent argument
    // when changing the plugin language at runtime
    protected void clearPrompts() {
        if (fileFacade.config().command().unregisterOnReload()) {
            prompts.clear();
        }
    }

    protected String addPrompt(int index, Function<Localization.Command.Prompt, String> promptLocalization) {
        // this command already registered and ignored
        if (prompts.size() != index) return "unknown";

        String prompt = promptLocalization.apply(fileFacade.localization().command().prompt());
        prompts.add(prompt);

        return prompt;
    }

    protected String getPrompt(int index) {
        if (prompts.size() - 1 < index) throw new IllegalArgumentException("Argument at index " + index + " is not registered in the " + getCommandName() + " command");

        return prompts.get(index);
    }

    protected <V extends @NonNull Object> V getArgument(CommandContext<FPlayer> commandContext, int promptIndex) {
        String prompt = getPrompt(promptIndex);
        return commandContext.get(prompt);
    }

    public String getCommandName() {
        List<String> aliases = config().aliases();
        if (aliases.isEmpty()) return "flectonepulsenull";

        return aliases.getFirst();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        clearPrompts();
    }

    @Override
    public void execute(@NonNull CommandContext<FPlayer> commandContext) {
        execute(commandContext.sender(), commandContext);
    }

    public abstract void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    public abstract CommandSetting config();

}
