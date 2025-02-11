package net.flectone.pulse.module.command.flectonepulse;

import lombok.Getter;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.TimeUtil;

import java.time.Duration;
import java.time.Instant;

public abstract class FlectonepulseModule extends AbstractModuleCommand<Localization.Command.Flectonepulse> {

    @Getter private final Command.Flectonepulse command;
    @Getter private final Permission.Command.Flectonepulse permission;

    private final FlectonePulse flectonePulse;
    private final FileManager fileManager;
    private final CommandUtil commandUtil;
    private final TimeUtil timeUtil;
    private final FLogger fLogger;

    public FlectonepulseModule(FileManager fileManager,
                               CommandUtil commandUtil,
                               TimeUtil timeUtil,
                               FlectonePulse flectonePulse,
                               FLogger fLogger) {
        super(localization -> localization.getCommand().getFlectonepulse(), null);

        this.flectonePulse = flectonePulse;
        this.fileManager = fileManager;
        this.commandUtil = commandUtil;
        this.timeUtil = timeUtil;
        this.fLogger = fLogger;

        command = fileManager.getCommand().getFlectonepulse();
        permission = fileManager.getPermission().getCommand().getFlectonepulse();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String type = commandUtil.getLiteral(1, arguments);

        if (type.equals("text")) {
            fileManager.reload();

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatTrueText)
                    .sound(getSound())
                    .sendBuilt();

            return;
        }

        try {
            Instant start = Instant.now();

            flectonePulse.reload();

            Instant end = Instant.now();

            String formattedTime = timeUtil.format(fPlayer, Duration.between(start, end).toMillis());

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(flectonepulse -> flectonepulse.getFormatTrue().replace("<time>", formattedTime))
                    .sound(getSound())
                    .sendBuilt();

        } catch (Exception e) {
            fLogger.warning(e);

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatFalse)
                    .sendBuilt();
        }
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
