package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

import java.util.Collection;
import java.util.Optional;

public abstract class ClearchatModule extends AbstractModuleCommand<Localization.Command.Clearchat> {

    @Getter
    private final Command.Clearchat command;
    @Getter
    private final Permission.Command.Clearchat permission;

    private final FPlayerManager fPlayerManager;
    private final CommandUtil commandUtil;

    @Inject
    public ClearchatModule(FPlayerManager fPlayerManager,
                           FileManager fileManager,
                           CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getClearchat(), null);

        this.fPlayerManager = fPlayerManager;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getClearchat();
        permission = fileManager.getPermission().getCommand().getClearchat();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Optional<Object> object = commandUtil.getOptional(0, arguments);

        if (object.isPresent() && object.get() instanceof Collection<?> collection) {
            collection.forEach(player -> clearChat(fPlayerManager.get(player)));
            return;
        }

        String string = object.map(o -> (String) o).orElse("");

        FPlayer fReceiver = fPlayerManager.getOnline(string);
        if (fReceiver.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Clearchat::getNullPlayer)
                    .sendBuilt();
            return;
        }

        clearChat(fReceiver);
    }

    private void clearChat(FPlayer fPlayer) {
        builder(fPlayer)
                .format("<br> ".repeat(100))
                .sendBuilt();

        builder(fPlayer)
                .format(Localization.Command.Clearchat::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
