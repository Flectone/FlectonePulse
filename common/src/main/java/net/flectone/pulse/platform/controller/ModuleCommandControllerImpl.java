package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.util.LazyInstance;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleCommandControllerImpl implements ModuleCommandController {

    private final Map<ModuleName, List<String>> commandPromptsMap = new EnumMap<>(ModuleName.class);

    private final LazyInstance<CommandRegistry> commandRegistry;
    private final FileFacade fileFacade;

    @Override
    public void registerCommand(ModuleCommand command, UnaryOperator<Command.Builder<FPlayer>> builder) {
        List<String> aliases = command.config().aliases();
        String commandName = getCommandName(command);

        commandRegistry.get().registerCommand(manager ->
                builder.apply(manager.commandBuilder(commandName, aliases, CommandMeta.empty())).handler(command)
        );
    }

    @Override
    public void registerSubCommand(ModuleCommand command, String subName, UnaryOperator<Command.Builder<FPlayer>> builder) {
        List<String> aliases = command.config().aliases().stream().map(alias -> alias + subName).toList();
        String commandName = getCommandName(command) + subName;

        commandRegistry.get().registerCommand(manager ->
                builder.apply(manager.commandBuilder(commandName, aliases, CommandMeta.empty()))
        );
    }

    // all prompt methods for solving the problems of a non-existent argument
    // when changing the plugin language at runtime
    @Override
    public void clearPrompts(ModuleCommand abstractModuleCommand) {
        if (fileFacade.config().internal().unregisterCommandOnReload()) {
            commandPromptsMap.remove(abstractModuleCommand.name());
        }
    }

    @Override
    public String addPrompt(ModuleCommand command, int index, Function<Localization.Command.Prompt, String> promptLocalization) {
        List<String> prompts = getPrompts(command);

        // this prompt already registered
        if (prompts.size() > index) {
            return prompts.get(index);
        }

        String prompt = promptLocalization.apply(fileFacade.localization().command().prompt());

        if (prompts.isEmpty()) {
            commandPromptsMap.put(command.name(), List.of(prompt));
        } else {
            List<String> newPrompts = new ArrayList<>(prompts);
            newPrompts.add(prompt);

            commandPromptsMap.put(command.name(), List.copyOf(newPrompts));
        }

        return prompt;
    }

    @Override
    public String getPrompt(ModuleCommand command, int index) {
        List<String> prompts = getPrompts(command);
        if (prompts.size() - 1 < index) {
            throw new IllegalArgumentException("Argument at index " + index + " is not registered in the " + getCommandName(command) + " command");
        }

        return prompts.get(index);
    }

    @Override
    public List<String> getPrompts(ModuleCommand command) {
        return commandPromptsMap.getOrDefault(command.name(), List.of());
    }

    @Override
    public <V extends @NonNull Object> V getArgument(ModuleCommand command, CommandContext<FPlayer> context, int promptIndex) {
        String prompt = getPrompt(command, promptIndex);
        return context.get(prompt);
    }

    @Override
    public String getCommandName(ModuleCommand command) {
        List<String> aliases = command.config().aliases();
        if (aliases.isEmpty()) return "flectonepulsenull";

        return aliases.getFirst();
    }

}