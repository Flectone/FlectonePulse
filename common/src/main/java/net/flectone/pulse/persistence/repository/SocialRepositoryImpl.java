package net.flectone.pulse.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.persistence.database.dao.FColorDao;
import net.flectone.pulse.persistence.database.dao.IgnoreDAO;
import net.flectone.pulse.persistence.database.dao.MailDAO;
import net.flectone.pulse.persistence.database.dao.SettingDAO;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepositoryImpl implements SocialRepository {

    private final @Named("playerColor") Cache<UUID, Map<FColor.Type, Set<FColor>>> playerColorCache;
    private final @Named("playerSetting") Cache<UUID, Settings> playerSettingCache;
    private final @Named("playerIgnore") Cache<UUID, List<Ignore>> playerIgnoreCache;

    private final IgnoreDAO ignoreDAO;
    private final MailDAO mailDAO;
    private final SettingDAO settingDAO;
    private final FColorDao fColorDao;

    @Override
    public void invalidate() {
        playerColorCache.invalidateAll();
        playerSettingCache.invalidateAll();
        playerIgnoreCache.invalidateAll();
    }

    @Override
    public List<Ignore> loadIgnores(FPlayer fPlayer) {
        return playerIgnoreCache.get(fPlayer.uuid(), _ -> ignoreDAO.load(fPlayer));
    }

    @Override
    public void invalidateIgnores(UUID uuid) {
        playerIgnoreCache.invalidate(uuid);
    }

    @Override
    public Optional<Ignore> saveIgnore(FPlayer fPlayer, FPlayer fTarget) {
        Ignore ignore = ignoreDAO.insert(fPlayer, fTarget);
        if (ignore == null) return Optional.empty();

        List<Ignore> ignores = new ArrayList<>(loadIgnores(fPlayer));
        ignores.add(ignore);

        playerIgnoreCache.put(fPlayer.uuid(), List.copyOf(ignores));

        return Optional.of(ignore);
    }

    @Override
    public void deleteIgnore(FPlayer fPlayer, Ignore ignore) {
        // invalidate record in database
        ignoreDAO.invalidate(ignore);

        // update cache
        List<Ignore> ignores = new ArrayList<>(loadIgnores(fPlayer));
        ignores.remove(ignore);

        playerIgnoreCache.put(fPlayer.uuid(), List.copyOf(ignores));
    }

    @Override
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return mailDAO.getReceiver(fPlayer);
    }

    @Override
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return mailDAO.getSender(fPlayer);
    }

    @NonNull
    @Override
    public Optional<Mail> saveMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return mailDAO.insert(fPlayer, fTarget, message);
    }

    @Override
    public void deleteMail(Mail mail) {
        mailDAO.delete(mail);
    }

    @NonNull
    @Override
    public Map<FColor.Type, Set<FColor>> loadColors(@NonNull FPlayer fPlayer) {
        return playerColorCache.get(fPlayer.uuid(), _ -> fColorDao.load(fPlayer));
    }

    @Override
    public void invalidateColors(UUID uuid) {
        playerColorCache.invalidate(uuid);
    }

    @Override
    public void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        // save colors to database
        fColorDao.save(fPlayer, colors);

        // update cache
        playerColorCache.put(fPlayer.uuid(), colors);
    }

    @Override
    public Settings loadSettings(@NonNull FPlayer fPlayer) {
        return playerSettingCache.get(fPlayer.uuid(), _ -> settingDAO.load(fPlayer).orElse(Settings.EMPTY));
    }

    @Override
    public void invalidateSettings(UUID uuid) {
        playerSettingCache.invalidate(uuid);
    }

    @Override
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value) {
        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting, value ? "1" : "0");

        Settings settings = loadSettings(fPlayer);

        Map<String, Boolean> newBooleans = new HashMap<>(settings.booleans());

        newBooleans.put(setting, value);

        settings = settings.withBooleans(Map.copyOf(newBooleans));

        playerSettingCache.put(fPlayer.uuid(), settings);
    }

    @Override
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting.name(), value);

        Settings settings = loadSettings(fPlayer);

        Map<SettingText, String> newTexts = settings.texts().isEmpty()
                ? new EnumMap<>(SettingText.class)
                : new EnumMap<>(settings.texts());

        if (value == null) {
            newTexts.remove(setting);
        } else {
            newTexts.put(setting, value);
        }

        settings = settings.withTexts(Map.copyOf(newTexts));

        playerSettingCache.put(fPlayer.uuid(), settings);
    }

}