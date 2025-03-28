package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;

import java.util.Optional;

public abstract class ClearmailModule extends AbstractModuleCommand<Localization.Command.Clearmail> {

    @Getter private final Command.Clearmail command;
    @Getter private final Permission.Command.Clearmail permission;

    private final FPlayerService fPlayerService;
    private final CommandUtil commandUtil;

    @Inject
    public ClearmailModule(FileManager fileManager,
                           FPlayerService fPlayerService,
                           CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getClearmail(), null);

        this.fPlayerService = fPlayerService;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getClearmail();
        permission = fileManager.getPermission().getCommand().getClearmail();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        int mailID = commandUtil.getInteger(0, arguments);
        Optional<Mail> optionalMail = fPlayerService.getMails(fPlayer)
                .stream()
                .filter(mail -> mail.id() == mailID)
                .findAny();

        if (optionalMail.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Clearmail::getNullMail)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(optionalMail.get().receiver());

        fPlayerService.deleteMail(optionalMail.get());

        builder(fReceiver)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat().replaceFirst("<id>", String.valueOf(mailID)))
                .message(optionalMail.get().message())
                .sound(getSound())
                .sendBuilt();
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
