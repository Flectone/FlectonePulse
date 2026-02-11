package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.FPlayerRepository;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerService {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerRepository fPlayerRepository;
    private final SocialRepository socialRepository;
    private final ModerationService moderationService;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;
    private final RandomUtil randomUtil;

    public void clear() {
        fPlayerRepository.clearCache();
    }

    public void reload() {
        // invalidate and load console FPlayer to reload name
        fPlayerRepository.invalid(getConsole().uuid());
        addConsole();

        // invalidate and load all platform players
        platformPlayerAdapter.getOnlinePlayers().forEach(uuid -> {
            fPlayerRepository.invalid(uuid);

            String name = platformPlayerAdapter.getName(uuid);
            saveFPlayerData(loadData(addFPlayer(uuid, name)));
        });
    }

    public void addConsole() {
        FPlayer console = FPlayer.builder()
                .console(true)
                .name(fileFacade.config().logger().console())
                .build();

        fPlayerRepository.add(console);
        fPlayerRepository.saveOrIgnore(console);
    }

    public FPlayer updateCache(FPlayer fPlayer) {
        fPlayerRepository.updateCache(fPlayer);
        return fPlayer;
    }

    public FPlayer addFPlayer(UUID uuid, String name) {
        // insert to database
        fPlayerRepository.save(uuid, name);

        moderationService.invalidate(uuid);

        // player can be in cache and be unknown
        // or uuid and name can be invalid
        FPlayer fPlayer = fPlayerRepository.get(uuid);
        if (fPlayer.isUnknown() || !fPlayer.uuid().equals(uuid) || !fPlayer.name().equals(name)) {
            fPlayerRepository.invalid(uuid);
            fPlayer = fPlayerRepository.get(uuid);
        }

        // most often this is not a real IP (this is server ip) on login,
        // need to update it before calling saveFPlayerData from PlayerJoinEvent
        fPlayer = fPlayer.withIp(platformPlayerAdapter.getIp(fPlayer));

        // player is not fully online on server,
        // but it should already be
        fPlayer = fPlayer.withOnline(true);

        // add player to online cache and remove from offline
        fPlayerRepository.add(fPlayer);

        return fPlayer;
    }

    // load player data
    public FPlayer loadData(FPlayer fPlayer) {
        return loadIgnores(loadColors(loadSettings(fPlayer)));
    }

    public void saveFPlayerData(FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            // skip offline FPlayer
            if (!platformPlayerAdapter.isOnline(fPlayer)) return;

            // update data in database
            updateFPlayer(fPlayer);
        });
    }

    public void invalidateOffline(UUID uuid) {
        fPlayerRepository.removeOffline(uuid);
    }

    public void invalidateOnline(UUID uuid) {
        fPlayerRepository.removeOnline(uuid);
    }

    public FPlayer clearAndSave(FPlayer fPlayer) {
        fPlayer = fPlayer.toBuilder()
                .online(false)
                .ip(platformPlayerAdapter.getIp(fPlayer))
                .build();

        fPlayerRepository.removeOnline(fPlayer);

        updateFPlayer(fPlayer);

        return fPlayer;
    }

    public void updateFPlayer(FPlayer fPlayer) {
        updateCache(fPlayer);
        fPlayerRepository.update(fPlayer);
    }

    public FPlayer getFPlayer(int id) {
        return fPlayerRepository.get(id);
    }

    public FPlayer getConsole() {
        return fPlayerRepository.get(-1);
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

    public FPlayer getFPlayer(FEntity fEntity) {
        return getFPlayer(fEntity.uuid());
    }

    public FPlayer getFPlayer(Object player) {
        String name = platformPlayerAdapter.getName(player);
        if (name.isEmpty()) return FPlayer.UNKNOWN;

        UUID uuid = platformPlayerAdapter.getUUID(player);
        if (uuid == null) {
            if (platformPlayerAdapter.isConsole(player)) {
                return getFPlayer(-1);
            }

            return FPlayer.builder().name(name).build();
        }

        FPlayer fPlayer = getFPlayer(uuid);
        if (fPlayer.isUnknown()) {
            return FPlayer.builder()
                    .name(name)
                    .uuid(uuid)
                    .type(platformPlayerAdapter.getEntityTranslationKey(player))
                    .build();
        }

        return fPlayer;
    }

    public FPlayer getRandomFPlayer() {
        List<FPlayer> fPlayers = getPlatformFPlayers();
        if (fPlayers.isEmpty()) return FPlayer.UNKNOWN;

        int randomIndex = randomUtil.nextInt(0, fPlayers.size());
        return fPlayers.get(randomIndex);
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

    public FPlayer loadColors(FPlayer fPlayer) {
        return fPlayerRepository.loadColors(fPlayer);
    }

    public void saveColors(FPlayer fPlayer) {
        updateCache(fPlayer);
        fPlayerRepository.saveColors(fPlayer);
    }

    public FPlayer loadIgnoresIfOffline(FPlayer fPlayer) {
        if (platformPlayerAdapter.isOnline(fPlayer)) return fPlayer;

        return loadIgnores(fPlayer);
    }

    public FPlayer loadIgnores(FPlayer fPlayer) {
        return socialRepository.loadIgnores(fPlayer);
    }

    public FPlayer loadSettingsIfOffline(FPlayer fPlayer) {
        if (platformPlayerAdapter.isOnline(fPlayer)) return fPlayer;

        return loadSettings(fPlayer);
    }

    public FPlayer loadSettings(FPlayer fPlayer) {
        return fPlayerRepository.loadSettings(fPlayer);
    }

    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
    }

    public FPlayer saveIgnore(FPlayer fPlayer, FPlayer fTarget) {
        Ignore ignore = socialRepository.saveAndGetIgnore(fPlayer, fTarget);
        if (ignore == null) return fPlayer;

        return updateCache(fPlayer.withIgnore(ignore));
    }

    public Mail saveMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return socialRepository.saveAndGetMail(fPlayer, fTarget, message);
    }

    public FPlayer deleteIgnore(FPlayer fPlayer, Ignore ignore) {
        socialRepository.deleteIgnore(ignore);
        return updateCache(fPlayer.withoutIgnore(ignore));
    }

    public void deleteMail(Mail mail) {
        socialRepository.deleteMail(mail);
    }

    public void saveOrUpdateSetting(FPlayer fPlayer, String setting) {
        updateCache(fPlayer);
        fPlayerRepository.saveOrUpdateSetting(fPlayer, setting);
    }

    public void saveOrUpdateSetting(FPlayer fPlayer, SettingText setting) {
        updateCache(fPlayer);
        fPlayerRepository.saveOrUpdateSetting(fPlayer, setting);
    }

    public void updateLocaleLater(UUID uuid, String wrapperLocale) {
        taskScheduler.runAsyncLater(() -> {
            FPlayer fPlayer = getFPlayer(uuid);
            updateLocale(fPlayer, wrapperLocale);
        }, 40L);
    }

    public boolean updateLocale(FPlayer fPlayer, String newLocale) {
        String locale = integrationModule.getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        SettingText settingName = SettingText.LOCALE;
        if (locale.equals(fPlayer.getSetting(settingName))) return false;
        if (fPlayer.isUnknown()) return false;

        saveOrUpdateSetting(fPlayer.withSetting(settingName, locale), settingName);
        return true;
    }

    public boolean hasHigherGroupThan(FPlayer source, FPlayer target) {
        if (source.isConsole()) return true;

        return integrationModule.getGroupWeight(source) > integrationModule.getGroupWeight(target)
                || platformPlayerAdapter.isOperator(source) && !platformPlayerAdapter.isOperator(target);
    }
}
