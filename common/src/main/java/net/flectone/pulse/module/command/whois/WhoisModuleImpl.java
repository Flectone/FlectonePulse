package net.flectone.pulse.module.command.whois;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WhoisModuleImpl implements WhoisModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;
    private final ModerationService moderationService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

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
        if (config().checkGroupWeight() && !fTargetOrIp.isUnknown() && !fPlayer.equals(fTargetOrIp)
                && !moderationService.hasHigherGroupThan(fPlayer, fTargetOrIp)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).lowerWeightGroup())
                            .build()
                    )
                    .build()
            );
            return;
        }

        String ip = fTargetOrIp.isUnknown()
                ? playerNameOrIp
                : platformPlayerAdapter.isOnline(fTargetOrIp) ? platformPlayerAdapter.getIp(fTargetOrIp) : fTargetOrIp.ip();

        if (StringUtils.isEmpty(ip)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).empty())
                            .build()
                    )
                    .build()
            );
            return;
        }

        List<FPlayer> fTargets = getFPlayersByIp(fPlayer, ip);

        int size = fTargets.size();
        if (size == 0) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).empty())
                            .build()
                    )
                    .build()
            );
            return;
        }

        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);
        if (page > countPage || page < 1) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPage())
                            .build()
                    )
                    .build()
            );
            return;
        }

        List<FPlayer> fPlayers = fTargets.subList((page - 1) * perPage, Math.min(page * perPage, size));

        Localization.Command.Whois localization = localization(fPlayer);

        StringBuilder stringBuilder = new StringBuilder();

        // header
        stringBuilder
                .append(StringUtils.replaceEach(
                        localization.header(),
                        new String[]{"<ip>", "<count>"},
                        new String[]{ip, String.valueOf(size)}
                ))
                .append("<br>");

        TagResolver tagResolvers = TagResolver.empty();

        for (int i = 0; i < fPlayers.size(); i++) {
            FPlayer fTarget = fPlayers.get(i);

            // line
            stringBuilder
                    .append(StringUtils.replaceEach(
                            localization.line(),
                            new String[]{"<ip>", "<target_name>", "<online>", "<command_mutelist>", "<command_banlist>", "<command_warnlist>", "<command_whitelist>", "<command_geolocate>", "<command_online>", "<target"},
                            new String[]{ip, fTarget.name(), fTarget.isOnline() && socialService.canSeeVanished(fTarget, fPlayer) ? localization.online() : localization.offline(),
                                    commandModuleController.getCommandName(mutelistModule),
                                    commandModuleController.getCommandName(banlistModule),
                                    commandModuleController.getCommandName(warnlistModule),
                                    commandModuleController.getCommandName(whitelistModule) + whitelistModule.config().subCommandPlayer(),
                                    commandModuleController.getCommandName(geolocateModule),
                                    commandModuleController.getCommandName(onlineModule),
                                    "<target_" + i
                            }
                    ))
                    .append("<br>");

            tagResolvers = TagResolver.resolver(
                    tagResolvers, messagePipeline.targetTag("target_" + i, fPlayer, fTarget)
            );
        }

        String nextPageCommand = "/" + commandModuleController.getCommandName(this);
        stringBuilder.append(StringUtils.replaceEach(
                localization.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{nextPageCommand, String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        ));

        String message = stringBuilder.toString();
        TagResolver finalTagResolvers = tagResolvers;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .sound(soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(message)
                        .tagResolver(finalTagResolvers)
                        .build()
                )
                .build()
        );
    }

    private List<FPlayer> getFPlayersByIp(FPlayer fPlayer, String ip) {
        List<FPlayer> fTargets = fPlayerService.getFPlayersByIp(ip);
        if (!config().checkGroupWeight()) return fTargets;

        return fTargets.stream()
                .filter(fTarget -> fPlayer.equals(fTarget) || moderationService.hasHigherGroupThan(fPlayer, fTarget))
                .toList();
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
