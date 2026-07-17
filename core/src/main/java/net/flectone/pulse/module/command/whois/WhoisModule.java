package net.flectone.pulse.module.command.whois;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WhoisModule implements ModuleCommand<Localization.Command.Whois> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SoundPlayer soundPlayer;
    private final EventDispatcher eventDispatcher;

    private final MutelistModule mutelistModule;
    private final BanlistModule banlistModule;
    private final WarnlistModule warnlistModule;
    private final WhitelistModule whitelistModule;
    private final GeolocateModule geolocateModule;
    private final OnlineModule onlineModule;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptNumber = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(true))
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

        String playerNameOrIp = commandModuleController.getArgument(this, commandContext, 0);
        String promptNumber = commandModuleController.getPrompt(this, 1);
        Optional<Integer> optionalPage = commandContext.optional(promptNumber);
        int page = optionalPage.orElse(1);

        FPlayer fTargetOrIp = fPlayerService.getFPlayer(playerNameOrIp);
        String ip = fTargetOrIp.isUnknown()
                ? playerNameOrIp
                : platformPlayerAdapter.isOnline(fTargetOrIp) ? platformPlayerAdapter.getIp(fTargetOrIp) : fTargetOrIp.ip();

        if (StringUtils.isEmpty(ip)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whois>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whois::empty)
                    .build()
            );
            return;
        }

        int size = fPlayerService.getTotalFPlayersCountByIp(ip);
        if (size == 0) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whois>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whois::empty)
                    .build()
            );
            return;
        }

        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);
        if (page > countPage || page < 1) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whois>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whois::nullPage)
                    .build()
            );
            return;
        }

        List<FPlayer> fPlayers = fPlayerService.getFPlayersByIp(ip, perPage, (page - 1) * perPage);

        Localization.Command.Whois localization = localization(fPlayer);

        String header = StringUtils.replaceEach(
                localization.header(),
                new String[]{"<ip>", "<count>"},
                new String[]{ip, String.valueOf(size)}
        );
        Component component = messagePipeline.build(MessageContext.builder().sender(fPlayer).message(header).build())
                .append(Component.newline());

        for (FPlayer fTarget : fPlayers) {
            String line = StringUtils.replaceEach(
                    localization.line(),
                    new String[]{"<ip>", "<target_name>", "<online>", "<command_mutelist>", "<command_banlist>", "<command_warnlist>", "<command_whitelist>", "<command_geolocate>", "<command_online>"},
                    new String[]{ip, fTarget.name(), fTarget.isOnline() ? localization.online() : localization.offline(),
                            commandModuleController.getCommandName(mutelistModule),
                            commandModuleController.getCommandName(banlistModule),
                            commandModuleController.getCommandName(warnlistModule),
                            commandModuleController.getCommandName(whitelistModule) + whitelistModule.config().subCommandPlayer(),
                            commandModuleController.getCommandName(geolocateModule),
                            commandModuleController.getCommandName(onlineModule)
                    }
            );

            component = component
                    .append(messagePipeline.build(MessageContext.builder()
                            .sender(fPlayer)
                            .message(line)
                            .tagResolvers(
                                    messagePipeline.targetTag(fPlayer, fTarget)
                            )
                            .build()
                    ))
                    .append(Component.newline());
        }

        String nextPageCommand = "/" + commandModuleController.getCommandName(this);
        String footer = StringUtils.replaceEach(
                localization.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{nextPageCommand, String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        );

        component = component.append(messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(footer)
                .build()
        ));

        MessageSendEvent messageSendEvent = eventDispatcher.dispatch(new MessageSendEvent(this.name(), fPlayer, component));
        if (!messageSendEvent.cancelled()) {
            soundPlayer.play(this.soundOrThrow(), fPlayer);
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_WHOIS;
    }

    @Override
    public Command.Whois config() {
        return fileFacade.command().whois();
    }

    @Override
    public Permission.Command.Whois permission() {
        return fileFacade.permission().command().whois();
    }

    @Override
    public Localization.Command.Whois localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().whois();
    }

}
