package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.util.file.FileFacade;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleCommandController {

    private final Object2ObjectOpenHashMap<Class<? extends ModuleSimple>, List<String>> commandPromptsMap = new Object2ObjectOpenHashMap<>();

    private final Provider<CommandRegistry> commandRegistryProvider;
    private final FileFacade fileFacade;
    private final ModuleController moduleController;

    public void registerCommand(ModuleCommand<?> command,
                                UnaryOperator<Command.Builder<FPlayer>> builder) {
        List<String> aliases = command.config().aliases();
        String commandName = getCommandName(command);

        commandRegistryProvider.get().registerCommand(manager ->
                builder.apply(manager.commandBuilder(commandName, aliases, CommandMeta.empty())).handler(command)
        );
    }

    public void registerCustomCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
        commandRegistryProvider.get().registerCommand(builder);
    }

    // all prompt methods for solving the problems of a non-existent argument
    // when changing the plugin language at runtime
    public void clearPrompts(ModuleCommand<?> abstractModuleCommand) {
        if (fileFacade.config().command().unregisterOnReload()) {
            commandPromptsMap.remove(abstractModuleCommand.getClass());
        }
    }

    public String addPrompt(ModuleCommand<?> command,
                            int index,
                            Function<Localization.Command.Prompt, String> promptLocalization) {
        List<String> prompts = getPrompts(command);

        // this command already registered and ignored
        if (prompts.size() != index) return "unknown";

        Class<? extends ModuleSimple> commandClass = moduleController.getRoot(command.getClass());
        String prompt = promptLocalization.apply(fileFacade.localization().command().prompt());

        if (prompts.isEmpty()) {
            commandPromptsMap.put(commandClass, List.of(prompt));
        } else {
            List<String> newPrompts = new ArrayList<>(prompts);
            newPrompts.add(prompt);

            commandPromptsMap.put(commandClass, List.copyOf(newPrompts));
        }

        return prompt;
    }

    public String getPrompt(ModuleCommand<?> command, int index) {
        List<String> prompts = getPrompts(command);
        if (prompts.size() - 1 < index) {
            throw new IllegalArgumentException("Argument at index " + index + " is not registered in the " + getCommandName(command) + " command");
        }

        return prompts.get(index);
    }

    public List<String> getPrompts(ModuleCommand<?> command) {
        return commandPromptsMap.getOrDefault(moduleController.getRoot(command.getClass()), Collections.emptyList());
    }

    public <V extends @NonNull Object> V getArgument(ModuleCommand<?> command,
                                                     CommandContext<FPlayer> context,
                                                     int promptIndex) {
        String prompt = getPrompt(command, promptIndex);
        return context.get(prompt);
    }

    public String getCommandName(ModuleCommand<?> command) {
        List<String> aliases = command.config().aliases();
        if (aliases.isEmpty()) return "flectonepulsenull";

        return aliases.getFirst();
    }

}
