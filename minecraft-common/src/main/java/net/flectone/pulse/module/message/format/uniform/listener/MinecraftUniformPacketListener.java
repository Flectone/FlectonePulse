package net.flectone.pulse.module.message.format.uniform.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementHolder;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.uniform.MinecraftUniformModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftUniformPacketListener implements PacketListener {

    private static final Set<PacketTypeCommon> HANDLED_TYPES = Set.of(
            PacketType.Play.Server.ACTION_BAR,
            PacketType.Play.Server.BOSS_BAR,
            PacketType.Play.Server.CHAT_MESSAGE,
            PacketType.Play.Server.DISGUISED_CHAT,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.OPEN_WINDOW,
            PacketType.Play.Server.PLAYER_INFO,
            PacketType.Play.Server.PLAYER_INFO_UPDATE,
            PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER,
            PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
            PacketType.Play.Server.SET_TITLE_SUBTITLE,
            PacketType.Play.Server.SET_TITLE_TEXT,
            PacketType.Play.Server.SYSTEM_CHAT_MESSAGE,
            PacketType.Play.Server.TEAMS,
            PacketType.Play.Server.TITLE,
            PacketType.Play.Server.UPDATE_ADVANCEMENTS,
            PacketType.Play.Server.UPDATE_SCORE
    );

    private final MinecraftUniformModule uniformModule;
    private final FPlayerService fPlayerService;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (handleLegacyTab(event)) return;
        if (!HANDLED_TYPES.contains(event.getPacketType())) return;

        UUID receiverUUID = event.getUser().getUUID();
        if (receiverUUID == null) return;

        FPlayer receiver = fPlayerService.getFPlayer(receiverUUID);

        switch (event.getPacketType()) {
            case PacketType.Play.Server.ACTION_BAR -> handleActionBar(event, receiver);
            case PacketType.Play.Server.BOSS_BAR -> handleBossBar(event, receiver);
            case PacketType.Play.Server.CHAT_MESSAGE -> handleChatMessage(event, receiver);
            case PacketType.Play.Server.DISGUISED_CHAT -> handleDisguisedChat(event, receiver);
            case PacketType.Play.Server.ENTITY_METADATA -> handleEntityMetadata(event, receiver);
            case PacketType.Play.Server.OPEN_WINDOW -> handleOpenWindow(event, receiver);
            case PacketType.Play.Server.PLAYER_INFO -> handlePlayerInfo(event, receiver);
            case PacketType.Play.Server.PLAYER_INFO_UPDATE -> handlePlayerInfoUpdate(event, receiver);
            case PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER -> handleTab(event, receiver);
            case PacketType.Play.Server.SCOREBOARD_OBJECTIVE -> handleObjective(event, receiver);
            case PacketType.Play.Server.SET_TITLE_SUBTITLE -> handleTitleSubtitle(event, receiver);
            case PacketType.Play.Server.SET_TITLE_TEXT -> handleTitleText(event, receiver);
            case PacketType.Play.Server.SYSTEM_CHAT_MESSAGE -> handleSystemChatMessage(event, receiver);
            case PacketType.Play.Server.TEAMS -> handleTeams(event, receiver);
            case PacketType.Play.Server.TITLE -> handleTitle(event, receiver);
            case PacketType.Play.Server.UPDATE_ADVANCEMENTS -> handleUpdateAdvancements(event, receiver);
            case PacketType.Play.Server.UPDATE_SCORE -> handleUpdateScore(event, receiver);
            default -> {
            }
        }
    }

    private void handleChatMessage(PacketSendEvent event, FPlayer receiver) {
        WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);

        ChatMessage message = wrapper.getMessage();
        if (message == null) return;
        if (!uniformModule.formatChatMessage(message, receiver)) return;

        wrapper.setMessage(message);
        event.markForReEncode(true);
    }

    private void handleSystemChatMessage(PacketSendEvent event, FPlayer receiver) {
        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);

        boolean enabled = wrapper.isOverlay() ? uniformModule.config().types().actionBar() : uniformModule.config().types().systemChat();
        if (!enabled) return;

        Component content = wrapper.getMessage();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setMessage(converted);
        event.markForReEncode(true);
    }

    private void handleDisguisedChat(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().systemChat()) return;

        WrapperPlayServerDisguisedChat wrapper = new WrapperPlayServerDisguisedChat(event);
        Component content = wrapper.getMessage();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setMessage(converted);
        event.markForReEncode(true);
    }

    private void handleActionBar(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().actionBar()) return;

        WrapperPlayServerActionBar wrapper = new WrapperPlayServerActionBar(event);
        Component content = wrapper.getActionBarText();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setActionBarText(converted);
        event.markForReEncode(true);
    }

    private void handleTitle(PacketSendEvent event, FPlayer receiver) {
        WrapperPlayServerTitle wrapper = new WrapperPlayServerTitle(event);

        switch (wrapper.getAction()) {
            case SET_TITLE -> {
                if (!uniformModule.config().types().title()) return;

                Component content = wrapper.getTitle();
                Component converted = uniformModule.formatComponent(content, receiver);
                if (converted == content) return;

                wrapper.setTitle(converted);
            }
            case SET_SUBTITLE -> {
                if (!uniformModule.config().types().title()) return;

                Component content = wrapper.getSubtitle();
                Component converted = uniformModule.formatComponent(content, receiver);
                if (converted == content) return;

                wrapper.setSubtitle(converted);
            }
            case SET_ACTION_BAR -> {
                if (!uniformModule.config().types().actionBar()) return;

                Component content = wrapper.getActionBar();
                Component converted = uniformModule.formatComponent(content, receiver);
                if (converted == content) return;

                wrapper.setActionBar(converted);
            }
            default -> {
                return;
            }
        }

        event.markForReEncode(true);
    }

    private void handleTitleText(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().title()) return;

        WrapperPlayServerSetTitleText wrapper = new WrapperPlayServerSetTitleText(event);
        Component content = wrapper.getTitle();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setTitle(converted);
        event.markForReEncode(true);
    }

    private void handleTitleSubtitle(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().title()) return;

        WrapperPlayServerSetTitleSubtitle wrapper = new WrapperPlayServerSetTitleSubtitle(event);
        Component content = wrapper.getSubtitle();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setSubtitle(converted);
        event.markForReEncode(true);
    }

    // PacketEvents uses 1.9 packet table for 1.9.4, where header packet
    // is one id ahead and is decoded as SOUND_EFFECT. The raw packet id is still
    // right, so decode it as a header and let PacketEvents re-encode it with that id.
    private boolean handleLegacyTab(PacketSendEvent event) {
        if (event.getServerVersion() != ServerVersion.V_1_9_4) return false;
        if (event.getPacketId() != 0x47) return false;

        UUID receiverUuid = event.getUser().getUUID();
        if (receiverUuid == null) return true;

        boolean headerEnabled = uniformModule.config().types().tabHeader();
        boolean footerEnabled = uniformModule.config().types().tabFooter();
        if (!headerEnabled && !footerEnabled) return true;

        FPlayer receiver = fPlayerService.getFPlayer(receiverUuid);
        WrapperPlayServerPlayerListHeaderAndFooter wrapper = new WrapperPlayServerPlayerListHeaderAndFooter(event);
        boolean changed = false;

        if (headerEnabled) {
            Component header = wrapper.getHeader();
            Component convertedHeader = uniformModule.formatComponent(header, receiver);
            if (convertedHeader != header) {
                wrapper.setHeader(convertedHeader);
                changed = true;
            }
        }

        if (footerEnabled) {
            Component footer = wrapper.getFooter();
            Component convertedFooter = uniformModule.formatComponent(footer, receiver);
            if (convertedFooter != footer) {
                wrapper.setFooter(convertedFooter);
                changed = true;
            }
        }

        if (changed) {
            event.markForReEncode(true);
        }

        return true;
    }

    private void handleTab(PacketSendEvent event, FPlayer receiver) {
        boolean headerEnabled = uniformModule.config().types().tabHeader();
        boolean footerEnabled = uniformModule.config().types().tabFooter();
        if (!headerEnabled && !footerEnabled) return;

        WrapperPlayServerPlayerListHeaderAndFooter wrapper = new WrapperPlayServerPlayerListHeaderAndFooter(event);
        boolean changed = false;

        if (headerEnabled) {
            Component header = wrapper.getHeaderComponent();
            Component convertedHeader = uniformModule.formatComponent(header, receiver);
            if (convertedHeader != header) {
                wrapper.setHeaderComponent(convertedHeader);
                changed = true;
            }
        }

        if (footerEnabled) {
            Component footer = wrapper.getFooterComponent();
            Component convertedFooter = uniformModule.formatComponent(footer, receiver);
            if (convertedFooter != footer) {
                wrapper.setFooterComponent(convertedFooter);
                changed = true;
            }
        }

        if (changed) {
            event.markForReEncode(true);
        }
    }

    private void handleBossBar(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().bossBar()) return;

        WrapperPlayServerBossBar wrapper = new WrapperPlayServerBossBar(event);
        WrapperPlayServerBossBar.Action action = wrapper.getAction();
        if (action != WrapperPlayServerBossBar.Action.ADD
                && action != WrapperPlayServerBossBar.Action.UPDATE_TITLE) {
            return;
        }

        Component content = wrapper.getTitle();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setTitle(converted);
        event.markForReEncode(true);
    }

    private void handleObjective(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().scoreboard()) return;

        WrapperPlayServerScoreboardObjective wrapper = new WrapperPlayServerScoreboardObjective(event);

        Component content = wrapper.getDisplayName();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted != content) {
            wrapper.setDisplayName(converted);
        }

        ScoreFormat originalScoreFormat = wrapper.getScoreFormat();
        ScoreFormat scoreFormat = uniformModule.formatScoreFormat(originalScoreFormat, receiver);
        if (scoreFormat != originalScoreFormat) {
            wrapper.setScoreFormat(scoreFormat);
        }

        if (converted != content || scoreFormat != originalScoreFormat) {
            event.markForReEncode(true);
        }
    }

    private void handleUpdateScore(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().scoreboard()) return;

        WrapperPlayServerUpdateScore wrapper = new WrapperPlayServerUpdateScore(event);

        Component content = wrapper.getEntityDisplayName();
        Component converted = content == null ? null : uniformModule.formatComponent(content, receiver);
        if (converted != content) {
            wrapper.setEntityDisplayName(converted);
        }

        ScoreFormat originalScoreFormat = wrapper.getScoreFormat();
        ScoreFormat scoreFormat = uniformModule.formatScoreFormat(originalScoreFormat, receiver);
        if (scoreFormat != originalScoreFormat) {
            wrapper.setScoreFormat(scoreFormat);
        }

        if (converted != content || scoreFormat != originalScoreFormat) {
            event.markForReEncode(true);
        }
    }

    private void handleTeams(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().team()) return;

        WrapperPlayServerTeams wrapper = new WrapperPlayServerTeams(event);
        Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> optionalInfo = wrapper.getTeamInfo();
        if (optionalInfo.isEmpty()) return;

        if (uniformModule.formatTeamInfo(optionalInfo.get(), receiver)) {
            event.markForReEncode(true);
        }
    }

    private void handlePlayerInfo(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().playerlistname()) return;

        WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo(event);
        boolean changed = false;

        for (WrapperPlayServerPlayerInfo.PlayerData playerData : wrapper.getPlayerDataList()) {
            Component displayName = playerData.getDisplayName();
            Component converted = displayName == null ? null : uniformModule.formatComponent(displayName, receiver);
            if (converted != displayName) {
                playerData.setDisplayName(converted);
                changed = true;
            }
        }

        if (changed) {
            event.markForReEncode(true);
        }
    }

    private void handlePlayerInfoUpdate(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().playerlistname()) return;

        WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);
        boolean changed = false;

        for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo : wrapper.getEntries()) {
            Component displayName = playerInfo.getDisplayName();
            Component converted = displayName == null ? null : uniformModule.formatComponent(displayName, receiver);
            if (converted != displayName) {
                playerInfo.setDisplayName(converted);
                changed = true;
            }
        }

        if (changed) {
            event.markForReEncode(true);
        }
    }

    private void handleUpdateAdvancements(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().advancement()) return;

        WrapperPlayServerUpdateAdvancements wrapper = new WrapperPlayServerUpdateAdvancements(event);
        List<AdvancementHolder> advancements = wrapper.getAddedAdvancements();
        if (!uniformModule.formatAdvancements(advancements, receiver)) return;

        wrapper.setAddedAdvancements(advancements);
        event.markForReEncode(true);
    }

    private void handleOpenWindow(PacketSendEvent event, FPlayer receiver) {
        if (!uniformModule.config().types().inventoryTitle()) return;

        WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);
        Component content = wrapper.getTitle();
        Component converted = uniformModule.formatComponent(content, receiver);
        if (converted == content) return;

        wrapper.setTitle(converted);
        event.markForReEncode(true);
    }

    private void handleEntityMetadata(PacketSendEvent event, FPlayer receiver) {
        WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
        List<EntityData<?>> metadata = wrapper.getEntityMetadata();
        List<EntityData<?>> convertedMetadata = uniformModule.formatEntityMetadata(metadata, receiver);
        if (convertedMetadata == metadata) return;

        wrapper.setEntityMetadata(convertedMetadata);
        event.markForReEncode(true);
    }

}
