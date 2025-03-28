package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;

import java.util.Collection;
import java.util.Optional;

public abstract class ClearchatModule extends AbstractModuleCommand<Localization.Command.Clearchat> {

    @Getter private final Command.Clearchat command;
    @Getter private final Permission.Command.Clearchat permission;

    private final FPlayerService fPlayerService;
    private final CommandUtil commandUtil;

    @Inject
    public ClearchatModule(FPlayerService fPlayerService,
                           FileManager fileManager,
                           CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getClearchat(), null);

        this.fPlayerService = fPlayerService;
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
            collection.forEach(player -> clearChat(fPlayerService.getFPlayer(player)));
            return;
        }

        String string = object.map(o -> (String) o).orElse("");

        FPlayer fReceiver = fPlayerService.getFPlayer(string);
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
                .destination(command.getDestination())
                .format("<br> ".repeat(100))
                .sendBuilt();

        builder(fPlayer)
                .destination(command.getDestination())
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

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
