package net.flectone.pulse.service;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.repository.FPlayerRepository;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/**
 * This service manages player storage across the plugin. You can use <code>getFPlayer()</code> to grab players from this service.
 * <hr>
 * <p>
 *     For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 *     and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 * <hr>
 * <p>
 *     Console senders are automatically different players, which unless manually changed, will always have an ID of <code>-1</code>.
 *     You can simply grab console senders by using <code lang="java">{@link FPlayerService}.getFPlayer(-1)</code>
 * </p>
 *
 * @see FPlayer
 */
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

    @Inject
    public FPlayerService(FileResolver fileResolver,
                          PlatformPlayerAdapter platformPlayerAdapter,
                          FPlayerRepository fPlayerRepository,
                          SocialRepository socialRepository,
                          ModerationService moderationService,
                          IntegrationModule integrationModule,
                          PacketSender packetSender,
                          PacketProvider packetProvider) {
        this.config = fileResolver.getConfig();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerRepository = fPlayerRepository;
        this.socialRepository = socialRepository;
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
    }

    public void clear() {
        fPlayerRepository.clearCache();
    }

    public void reload() {
        clear();
        addConsole();

        platformPlayerAdapter.getOnlinePlayers().forEach(uuid -> {
            String name = platformPlayerAdapter.getName(uuid);
            FPlayer fPlayer = addFPlayer(uuid, name);
            loadData(fPlayer);
            saveFPlayerData(fPlayer);
        });
    }

    public void addConsole() {
        FPlayer console = new FPlayer(true, config.getConsole());
        fPlayerRepository.add(console);
        fPlayerRepository.saveOrIgnore(console);
    }

    public FPlayer addFPlayer(UUID uuid, String name) {
        // insert to database
        fPlayerRepository.save(uuid, name);

        moderationService.invalidate(uuid);

        // player can be in the cache and be unknown
        FPlayer fPlayer = fPlayerRepository.get(uuid);
        if (fPlayer.isUnknown()) {
            fPlayerRepository.invalid(uuid);
            fPlayer = fPlayerRepository.get(uuid);
        }

        // most often this is not a real IP (this is server ip) on login,
        // need to update it before calling saveFPlayerData from PlayerJoinEvent
        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));

        // player is not fully online on server,
        // but it should already be
        fPlayer.setOnline(true);

        // add player to online cache and remove from offline
        fPlayerRepository.add(fPlayer);

        return fPlayer;
    }

    // load player data
    public void loadData(FPlayer fPlayer) {
        loadSettings(fPlayer);
        loadColors(fPlayer);
        loadIgnores(fPlayer);
    }

    @Async
    public void saveFPlayerData(FPlayer fPlayer) {
        // skip offline FPlayer
        if (!platformPlayerAdapter.isOnline(fPlayer)) return;

        // update data in database
        updateFPlayer(fPlayer);
    }

    public int getPing(FPlayer player) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(player);
        if (platformPlayer == null) return 0;

        return packetProvider.getPing(platformPlayer);
    }

    public String getSortedName(FPlayer fPlayer) {
        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_17)) {
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

    public List<FPlayer> getOnlineFPlayers() {
        return fPlayerRepository.getOnlinePlayers();
    }

    public List<FPlayer> getVisibleFPlayersFor(FPlayer fViewer) {
        return getOnlineFPlayers()
                .stream()
                .filter(target -> integrationModule.canSeeVanished(target, fViewer))
                .toList();
    }

    public List<FPlayer> getFPlayersWhoCanSee(FPlayer target) {
        return getOnlineFPlayers()
                .stream()
                .filter(viewer -> integrationModule.canSeeVanished(target, viewer))
                .toList();
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

    public void loadIgnoresIfOffline(FPlayer fPlayer) {
        if (platformPlayerAdapter.isOnline(fPlayer)) return;

        loadIgnores(fPlayer);
    }

    public void loadIgnores(FPlayer fPlayer) {
        socialRepository.loadIgnores(fPlayer);
    }

    public void loadSettingsIfOffline(FPlayer fPlayer) {
        if (platformPlayerAdapter.isOnline(fPlayer)) return;

        loadSettings(fPlayer);
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

    public void saveOrUpdateSetting(FPlayer fPlayer, MessageType setting) {
        fPlayerRepository.saveOrUpdateSetting(fPlayer, setting);
    }

    public void saveOrUpdateSetting(FPlayer fPlayer, SettingText setting) {
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

        if (locale.equals(fPlayer.getSetting(SettingText.LOCALE))) return;
        if (fPlayer.isUnknown()) return;

        fPlayer.setSetting(SettingText.LOCALE, locale);
        saveOrUpdateSetting(fPlayer, SettingText.LOCALE);
    }
}
