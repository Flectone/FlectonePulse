package net.flectone.pulse.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ignore;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.repository.FPlayerRepository;
import net.flectone.pulse.repository.SocialRepository;
import net.flectone.pulse.resolver.FileResolver;
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
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public FPlayerService(FileResolver fileResolver,
                          PlatformPlayerAdapter platformPlayerAdapter,
                          FPlayerRepository fPlayerRepository,
                          SocialRepository socialRepository,
                          ModerationService moderationService,
                          IntegrationModule integrationModule,
                          PacketSender packetSender,
                          PacketProvider packetProvider,
                          EventProcessRegistry eventProcessRegistry) {
        this.config = fileResolver.getConfig();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerRepository = fPlayerRepository;
        this.socialRepository = socialRepository;
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    public void clear() {
        fPlayerRepository.clearCache();
    }

    public void reload() {
        clear();

        FPlayer console = new FPlayer(config.getConsole());
        fPlayerRepository.add(console);
        fPlayerRepository.saveOrIgnore(console);

        platformPlayerAdapter.getOnlinePlayers().forEach(uuid -> {
            String name = platformPlayerAdapter.getName(uuid);
            FPlayer fPlayer = addFPlayer(uuid, name);
            loadData(fPlayer);
        });

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_PERSIST_AND_DISPOSE, this::clearAndSave);
    }

    public FPlayer addFPlayer(UUID uuid, String name) {
        // insert to database
        boolean isNew = fPlayerRepository.save(uuid, name);

        moderationService.invalidate(uuid);

        // player can be in the cache and be unknown
        FPlayer fPlayer = fPlayerRepository.get(uuid);
        if (fPlayer.isUnknown()) {
            fPlayerRepository.invalid(uuid);
            fPlayer = fPlayerRepository.get(uuid);
        }

        if (isNew) {
            fPlayer.setDefaultSettings();
            saveSettings(fPlayer);
        }

        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));

        // add player to online cache and remove from offline
        fPlayerRepository.add(fPlayer);

        return fPlayer;
    }

    public void loadData(UUID uuid) {
        FPlayer fPlayer = getFPlayer(uuid);
        loadData(fPlayer);
    }

    public void loadData(FPlayer fPlayer) {
        fPlayer.setOnline(true);

        // load player data
        loadSettings(fPlayer);
        loadColors(fPlayer);
        loadIgnores(fPlayer);

        // update old database data
        fPlayerRepository.update(fPlayer);
    }

    public int getPing(FPlayer player) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(player);
        if (platformPlayer == null) return 0;

        return packetProvider.getPing(platformPlayer);
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
        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));
        invalidateOnline(fPlayer.getUuid());
        platformPlayerAdapter.clear(fPlayer);
        updateFPlayer(fPlayer);
    }

    public void updateFPlayer(FPlayer fPlayer) {
        fPlayerRepository.update(fPlayer);
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

    public List<FPlayer> getPlatformFPlayers() {
        return fPlayerRepository.getOnlinePlayers().stream()
                .filter(platformPlayerAdapter::isOnline)
                .toList();
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

    @Async(delay = 40L)
    public void updateLocaleLater(UUID uuid, String wrapperLocale) {
        FPlayer fPlayer = getFPlayer(uuid);
        updateLocale(fPlayer, wrapperLocale);
    }

    public void updateLocale(FPlayer fPlayer, String newLocale) {
        String locale = integrationModule.getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        if (locale.equals(fPlayer.getSettingValue(FPlayer.Setting.LOCALE))) return;
        if (fPlayer.isUnknown()) return;

        saveOrUpdateSetting(fPlayer, FPlayer.Setting.LOCALE, locale);
    }
}
