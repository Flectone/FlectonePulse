package net.flectone.pulse.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.repository.*;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

@Singleton
public class FPlayerService {

    private final Config config;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerRepository fPlayerRepository;
    private final SocialRepository socialRepository;
    private final ModerationService moderationService;
    private final IntegrationModule integrationModule;
    private final PacketEventsUtil packetEventsUtil;
    private final TaskScheduler taskScheduler;

    @Inject
    public FPlayerService(FileManager fileManager,
                          PlatformPlayerAdapter platformPlayerAdapter,
                          FPlayerRepository fPlayerRepository,
                          SocialRepository socialRepository,
                          ModerationService moderationService,
                          IntegrationModule integrationModule,
                          PacketEventsUtil packetEventsUtil,
                          TaskScheduler taskScheduler) {
        this.config = fileManager.getConfig();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerRepository = fPlayerRepository;
        this.socialRepository = socialRepository;
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
        this.packetEventsUtil = packetEventsUtil;
        this.taskScheduler = taskScheduler;
    }

    public void clear() {
        fPlayerRepository.clearCache();
    }

    public void reload() {
        clear();

        FPlayer console = new FPlayer(config.getConsole());
        fPlayerRepository.add(console);
        fPlayerRepository.saveOrIgnore(console);

        platformPlayerAdapter.getOnlinePlayers().forEach(this::addAndGetFPlayer);
    }

    public FPlayer addAndGetFPlayer(UUID uuid) {
        return addAndGetFPlayer(uuid, platformPlayerAdapter.getEntityId(uuid), platformPlayerAdapter.getName(uuid));
    }

    public FPlayer addAndGetFPlayer(UUID uuid, int entityId, String name) {
        boolean isInserted = fPlayerRepository.save(uuid, name);
        FPlayer fPlayer = fPlayerRepository.get(uuid);

        loadFPlayerData(fPlayer, !isInserted);

        fPlayer.setOnline(true);
        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));
        fPlayer.setCurrentName(name);
        fPlayer.setEntityId(entityId);

        fPlayerRepository.add(fPlayer);

        taskScheduler.runAsync(() -> fPlayerRepository.saveOrUpdate(fPlayer));

        platformPlayerAdapter.update(fPlayer);

        return fPlayer;
    }

    public int getPing(FPlayer player) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(player);
        if (platformPlayer == null) return 0;

        return packetEventsUtil.getPing(platformPlayer);
    }

    public String getIp(FPlayer fPlayer) {
        return platformPlayerAdapter.getIp(fPlayer);
    }

    public String getSortedName(FPlayer fPlayer) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_17)) {
            return fPlayer.getName();
        }

        int weight = integrationModule.getGroupWeight(fPlayer);

        String paddedRank = String.format("%010d", Integer.MAX_VALUE - weight);
        String paddedName = String.format("%-16s", fPlayer.getName());
        return paddedRank + paddedName;
    }

    public void clearAndSave(FPlayer fPlayer) {
        fPlayer.setOnline(false);

        platformPlayerAdapter.clear(fPlayer);
        saveOrUpdateFPlayer(fPlayer);
    }

    public void saveOrUpdateFPlayer(FPlayer fPlayer) {
        fPlayerRepository.saveOrUpdate(fPlayer);
    }

    public int getEntityId(FPlayer fPlayer) {
        return platformPlayerAdapter.getEntityId(fPlayer);
    }

    public FPlayer getFPlayer(int id) {
        return fPlayerRepository.get(id);
    }

    public FPlayer getFPlayer(String name) {
        return fPlayerRepository.get(name);
    }

    public FPlayer getFPlayer(InetAddress inetAddress) {
        return fPlayerRepository.get(inetAddress);
    }

    public FPlayer getFPlayer(UUID uuid) {
        return fPlayerRepository.get(uuid);
    }

    public FPlayer getFPlayer(Object player) {
        String name = platformPlayerAdapter.getName(player);
        if (name == null) return FPlayer.UNKNOWN;

        UUID uuid = platformPlayerAdapter.getUUID(player);
        if (uuid != null) return this.getFPlayer(uuid);

        if (platformPlayerAdapter.isConsole(player)) return this.getFPlayer(FPlayer.UNKNOWN.getUuid());

        return new FPlayer(name);
    }

    public Object toPlatformFPlayer(FPlayer fPlayer) {
        return platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
    }

    public List<FPlayer> findAllFPlayers() {
        return fPlayerRepository.getAllPlayersDatabase();
    }

    public List<FPlayer> findOnlineFPlayers() {
        return fPlayerRepository.getOnlinePlayersDatabase();
    }

    public List<FPlayer> getFPlayers() {
        return fPlayerRepository.getOnlinePlayers();
    }

    public List<FPlayer> getFPlayersWithConsole() {
        return fPlayerRepository.getOnlineFPlayersWithConsole();
    }

    public void kick(FPlayer fPlayer, Component reason) {
        WrapperPlayServerDisconnect packet = new WrapperPlayServerDisconnect(reason);
        packetEventsUtil.sendPacket(fPlayer, packet);
    }

    public void loadFPlayerData(FPlayer fPlayer, boolean load) {
        loadOrSaveDefaultSetting(fPlayer, load);
        loadColors(fPlayer);
        loadIgnores(fPlayer);
        moderationService.load(fPlayer, Moderation.Type.MUTE);
    }

    public void loadColors(FPlayer fPlayer) {
        fPlayerRepository.loadColors(fPlayer);
    }

    public void saveColors(FPlayer fPlayer) {
        fPlayerRepository.saveColors(fPlayer);
    }

    public void loadIgnores(FPlayer fPlayer) {
        socialRepository.loadIgnores(fPlayer);
    }

    public void loadSettings(FPlayer fPlayer) {
        fPlayerRepository.loadSettings(fPlayer);
    }

    public List<Mail> getMails(FPlayer fPlayer) {
        return socialRepository.getMails(fPlayer);
    }

    public Ignore saveAndGetIgnore(FPlayer fPlayer, FPlayer fTarget) {
        return socialRepository.saveAndGetIgnore(fPlayer, fTarget);
    }

    public Mail saveAndGetMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return socialRepository.saveAndGetMail(fPlayer, fTarget, message);
    }

    public void deleteIgnore(Ignore ignore) {
        socialRepository.deleteIgnore(ignore);
    }

    public void deleteMail(Mail mail) {
        socialRepository.deleteMail(mail);
    }

    public void loadOrSaveDefaultSetting(FPlayer fPlayer, boolean load) {
        if (load) {
            fPlayerRepository.loadSettings(fPlayer);
            return;
        }

        fPlayer.setDefaultSettings();
        saveSettings(fPlayer);
    }

    public void saveSettings(FPlayer fPlayer) {
        fPlayerRepository.saveSettings(fPlayer);
    }

    public void deleteSetting(FPlayer fPlayer, FPlayer.Setting setting) {
        fPlayer.removeSetting(setting);
        fPlayerRepository.deleteSetting(fPlayer, setting);
    }

    public void saveOrUpdateSetting(FPlayer fPlayer, FPlayer.Setting setting, @Nullable String value) {
        fPlayer.setSetting(setting, value);
        fPlayerRepository.saveOrUpdateSetting(fPlayer, setting);
    }
}
