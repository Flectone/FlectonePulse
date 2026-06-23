package net.flectone.pulse.module.message.scoreboard.objective.belowname;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.scoreboard.objective.MinecraftObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.listener.MinecraftPacketBelownameListener;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.listener.MinecraftPulseBelownameListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBelownameModule implements ModuleLocalization<Localization.Message.Scoreboard.Objective.Belowname> {

    private static final int ATTRIBUTE_BASE_VALUE = 10;

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MinecraftObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final SocialService socialService;
    private final MinecraftPacketSender packetSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MinecraftPacketProvider packetProvider;
    private final @Named("isNewerThanOrEqualsV_26_2") boolean isNewerThanOrEqualsV262;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::updateScore, ticker.period());
        }

        listenerRegistry.register(MinecraftPulseBelownameListener.class);

        if (isNewerThanOrEqualsV262) {
            listenerRegistry.register(MinecraftPacketBelownameListener.class);

            sendForAll(false);
        }
    }

    @Override
    public void onDisable() {
        fPlayerService.getPlatformFPlayers().forEach(this::remove);

        if (!FlectonePulseAPI.isDisabling()) {
            sendForAll(true);
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SCOREBOARD_OBJECTIVE_BELOWNAME;
    }

    @Override
    public Message.Scoreboard.Objective.Belowname config() {
        return fileFacade.message().scoreboard().objective().belowname();
    }

    @Override
    public Permission.Message.Scoreboard.Objective.Belowname permission() {
        return fileFacade.permission().message().scoreboard().objective().belowname();
    }

    @Override
    public Localization.Message.Scoreboard.Objective.Belowname localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().scoreboard().objective().belowname();
    }

    public void create(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Localization.Message.Scoreboard.Objective.Belowname localization = localization(fPlayer);
        Component displayFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.displayFormat());
        Component scoreFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.scoreFormat());

        objectiveModule.createObjective(fPlayer, displayFormat, scoreFormat, ScoreboardPosition.BELOWNAME);
        updateScore(fPlayer);
    }

    public void updateScore(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(vanishedPlayer -> socialService.canSeeVanished(vanishedPlayer, fPlayer))
                .forEach(fObjective -> {
                    Localization.Message.Scoreboard.Objective.Belowname localization = localization(fPlayer);
                    Component scoreFormat = objectiveModule.buildFormat(fObjective, fPlayer, localization.score(), localization.scoreFormat(), false);

                    objectiveModule.updateObjective(fPlayer, fObjective, scoreFormat, ScoreboardPosition.BELOWNAME);
                });
    }

    public void remove(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.BELOWNAME);
    }

    public boolean isModernPlayer(UUID uuid) {
        User user = packetProvider.getUser(uuid);
        if (user == null) return false;

        return user.getPacketVersion().isNewerThanOrEquals(ClientVersion.V_26_2);
    }

    public void sendForAll(boolean baseValue) {
        List<UUID> onlinePlayers = platformPlayerAdapter.getOnlinePlayers();
        onlinePlayers.forEach(player -> {
            int entityId = platformPlayerAdapter.getEntityId(player);
            if (entityId == 0) return;

            onlinePlayers.stream()
                    .filter(this::isModernPlayer)
                    .forEach(receiver -> send(receiver, entityId, baseValue));
        });
    }

    public void send(UUID uuid, int entityId, boolean baseValue) {
        packetSender.send(uuid, new WrapperPlayServerUpdateAttributes(
                entityId,
                List.of(new WrapperPlayServerUpdateAttributes.Property(Attributes.BELOW_NAME_DISTANCE, baseValue ? ATTRIBUTE_BASE_VALUE : config().distance(), List.of())))
        );
    }


}
