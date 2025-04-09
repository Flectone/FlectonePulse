package net.flectone.pulse.module.command.mark;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.minecraft.extras.parser.TextColorParser;

import java.util.Optional;
import java.util.function.BiConsumer;

@Singleton
public class MarkModule extends AbstractModuleCommand<Localization.Command> {

    private final Command.Mark command;
    private final Permission.Command.Mark permission;

    private final BiConsumer<FPlayer, NamedTextColor> markConsumer;
    private final CommandRegistry commandRegistry;

    @Inject
    public MarkModule(FileManager fileManager,
                      net.flectone.pulse.module.message.mark.MarkModule markModule,
                      CommandRegistry commandRegistry) {
        super(Localization::getCommand, null);

        this.markConsumer = markModule::mark;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getMark();
        permission = fileManager.getPermission().getCommand().getMark();

        addPredicate(this::checkCooldown);
        addPredicate(this::checkMute);
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptColor = getPrompt().getColor();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptColor, TextColorParser.textColorParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptColor = getPrompt().getColor();
        Optional<TextColor> optionalTextColor = commandContext.optional(promptColor);

        NamedTextColor color = (NamedTextColor) optionalTextColor.orElse(NamedTextColor.WHITE);

        markConsumer.accept(fPlayer, color);
        playSound(fPlayer);
    }
}
