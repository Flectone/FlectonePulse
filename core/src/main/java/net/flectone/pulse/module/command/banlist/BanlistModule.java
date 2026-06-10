package net.flectone.pulse.module.command.banlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ModerationListSender;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BanlistModule implements ModuleCommand<Localization.Command.Banlist> {

    private final FileFacade fileFacade;
    private final UnbanModule unbanModule;
    private final CommandParserProvider commandParserProvider;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ModerationListSender moderationListSender;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptNumber = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.bannedParser())
                .optional(promptNumber, commandParserProvider.integerParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        moderationListSender.send(
                this,
                fPlayer,
                commandContext,
                Moderation.Type.BAN,
                0,
                config().perPage(),
                "/" + commandModuleController.getCommandName(this),
                fTarget -> "/" + commandModuleController.getCommandName(unbanModule) + " " + fTarget.name() + " <id>"
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_BANLIST;
    }

    @Override
    public Command.Banlist config() {
        return fileFacade.command().banlist();
    }

    @Override
    public Permission.Command.Banlist permission() {
        return fileFacade.permission().command().banlist();
    }

    @Override
    public Localization.Command.Banlist localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().banlist();
    }
}
