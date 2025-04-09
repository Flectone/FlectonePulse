package net.flectone.pulse.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.repository.FPlayerRepository;
import net.flectone.pulse.repository.SocialRepository;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
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
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final TaskScheduler taskScheduler;

    @Inject
    public FPlayerService(FileManager fileManager,
                          PlatformPlayerAdapter platformPlayerAdapter,
                          FPlayerRepository fPlayerRepository,
                          SocialRepository socialRepository,
                          ModerationService moderationService,
                          IntegrationModule integrationModule,
                          PacketSender packetSender,
                          PacketProvider packetProvider,
                          TaskScheduler taskScheduler) {
        this.config = fileManager.getConfig();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerRepository = fPlayerRepository;
        this.socialRepository = socialRepository;
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
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
        // insert to database
        boolean isInserted = fPlayerRepository.save(uuid, name);

        // player can be in the cache and be unknown
        FPlayer player = fPlayerRepository.get(uuid);
        if (player.isUnknown()) {
            fPlayerRepository.invalid(uuid);
            player = fPlayerRepository.get(uuid);
        }

        moderationService.invalidate(uuid);

        FPlayer finalPlayer = player;

        // load player data
        loadOrSaveDefaultSetting(finalPlayer, !isInserted);
        loadColors(finalPlayer);
        loadIgnores(finalPlayer);
        finalPlayer.setOnline(true);
        finalPlayer.setIp(platformPlayerAdapter.getIp(finalPlayer));
        finalPlayer.setEntityId(entityId);

        // add player to online cache and remove from offline
        fPlayerRepository.add(finalPlayer);

        // update old database data
        taskScheduler.runAsync(() -> fPlayerRepository.saveOrUpdate(finalPlayer));

        // send info for modules
        platformPlayerAdapter.update(finalPlayer);

        return finalPlayer;
    }

    public int getPing(FPlayer player) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(player);
        if (platformPlayer == null) return 0;

        return packetProvider.getPing(platformPlayer);
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

    public void invalidateOffline(UUID uuid) {
        fPlayerRepository.removeOffline(uuid);
    }

    public void invalidateOnline(UUID uuid) {
        fPlayerRepository.removeOnline(uuid);
    }

    public void clearAndSave(FPlayer fPlayer) {
        fPlayer.setOnline(false);
        invalidateOnline(fPlayer.getUuid());
        platformPlayerAdapter.clear(fPlayer);
        saveOrUpdateFPlayer(fPlayer);
    }

    public void saveOrUpdateFPlayer(FPlayer fPlayer) {
        fPlayerRepository.saveOrUpdate(fPlayer);
    }

    public int getEntityId(FPlayer fPlayer) {
        return platformPlayerAdapter.getEntityId(fPlayer.getUuid());
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
        if (name.isEmpty()) return FPlayer.UNKNOWN;

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
        packetSender.send(fPlayer, packet);
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

    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
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
