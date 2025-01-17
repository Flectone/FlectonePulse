package net.flectone.pulse.module;

import com.google.inject.Inject;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.Range;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractModuleCommand<M extends Localization.ILocalization> extends AbstractModuleMessage<M> {

    private final Predicate<FPlayer> commandPredicate;

    @Inject private FPlayerManager fPlayerManager;
    @Inject protected CommandUtil commandUtil;
    @Inject private FileManager fileManager;
    @Inject private FLogger fLogger;
    @Inject private Database database;

    public AbstractModuleCommand(Function<Localization, M> messageFunction, Predicate<FPlayer> commandPredicate) {
        super(messageFunction);

        this.commandPredicate = commandPredicate;
    }

    @Async
    protected void executesFPlayer(Object commandSender, Object arguments) {
        FPlayer fPlayer = fPlayerManager.convert(commandSender);

        onCommand(fPlayer, arguments);
    }

    @Async
    protected void executesFPlayerDatabase(Object commandSender, Object commandArguments) {
        FPlayer fPlayer = fPlayerManager.convert(commandSender);

        try {
            onCommand(database, fPlayer, commandArguments);
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void onCommand(FPlayer fPlayer, Object commandArguments) {}
    public void onCommand(Database database, FPlayer fPlayer, Object commandArguments) throws SQLException {}

    public Localization.Command.Prompt getPrompt() {
        return fileManager.getLocalization().getCommand().getPrompt();
    }

    public String getName(Command.ICommandFile command) {
        List<String> aliases = command.getAliases();
        if (aliases.isEmpty()) return "flectonepulsenull";

        return aliases.get(0);
    }

    public boolean checkDisable(FEntity entity, @NotNull FEntity receiver, DisableAction action) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (commandPredicate == null || commandPredicate.test(fReceiver)) return false;

        return super.sendDisableMessage(entity, fReceiver, action);
    }

    public abstract void createCommand();

    @Override
    public Predicate<FPlayer> rangeFilter(FEntity sender, double range) {
        Predicate<FPlayer> filter = super.rangeFilter(sender, range);

        if (range == Range.PLAYER) {
            return filter;
        }

        return this.commandPredicate == null ? filter : filter.and(commandPredicate);
    }
}
