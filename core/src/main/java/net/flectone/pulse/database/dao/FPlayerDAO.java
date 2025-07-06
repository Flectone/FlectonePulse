package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.FPlayerSQL;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class FPlayerDAO extends BaseDAO<FPlayerSQL> {

    private final Config.Database config;
    private final FLogger logger;
    private final Provider<SettingDAO> settingDAOProvider;

    @Inject
    public FPlayerDAO(FileManager fileManager, Database database,
                      FLogger logger, Provider<SettingDAO> settingDAOProvider) {
        super(database, FPlayerSQL.class);

        this.config = fileManager.getConfig().getDatabase();
        this.logger = logger;
        this.settingDAOProvider = settingDAOProvider;
    }

    public record PlayerInfo(int id, int online, String uuid, String name, @Nullable String ip) {}

    public boolean insert(UUID uuid, String name) {
        return inTransaction(sql -> {
            Optional<PlayerInfo> existingByName = sql.findByName(name);
            if (existingByName.isPresent()) {
                PlayerInfo playerInfo = existingByName.get();

                UUID existingUuid = UUID.fromString(playerInfo.uuid());
                if (!uuid.equals(existingUuid)) {
                    updateAndWarn(sql, playerInfo.id(), uuid, name, playerInfo.ip());
                }

                return false;
            }

            Optional<PlayerInfo> existingByUUID = sql.findByUUID(uuid.toString());
            if (existingByUUID.isPresent()) {
                PlayerInfo playerInfo = existingByUUID.get();

                String existingName = playerInfo.name();
                if (!name.equalsIgnoreCase(existingName)) {
                    updateAndWarn(sql, playerInfo.id(), uuid, existingName, playerInfo.ip());
                }

                return false;
            }

            sql.insert(uuid.toString(), name);
            return true;
        });
    }

    public void insertOrIgnore(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        useHandle(sql -> {
            if (config.getType() == Config.Database.Type.MYSQL) {
                sql.insertOrIgnoreMySQL(fPlayer.getId(), fPlayer.getUuid().toString(), fPlayer.getName());
            } else {
                sql.insertOrIgnoreSQLite(fPlayer.getId(), fPlayer.getUuid().toString(), fPlayer.getName());
            }
        });
    }

    public void save(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        useHandle(sql -> sql.update(
                fPlayer.getId(),
                fPlayer.isOnline(),
                fPlayer.getUuid().toString(),
                fPlayer.getName(),
                fPlayer.getIp()
        ));
    }

    @NotNull
    public List<FPlayer> getOnlineFPlayers() {
        return withHandle(sql -> convertToFPlayers(sql.getOnlinePlayers()));
    }

    @NotNull
    public List<FPlayer> getFPlayers() {
        return withHandle(sql -> convertToFPlayers(sql.getAllPlayers()));
    }

    @NotNull
    public FPlayer getFPlayer(String name) {
        return withHandle(sql -> sql.getPlayerByName(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return withHandle(sql -> sql.getPlayerByIp(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(UUID uuid) {
        return withHandle(sql -> sql.getPlayerByUuid(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(int id) {
        return withHandle(sql -> sql.getPlayerById(id)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    public void updateAllToOffline() {
        useHandle(FPlayerSQL::updateAllToOffline);
    }

    private void updateAndWarn(FPlayerSQL fPlayerSQL, int id, UUID uuid, String name, String ip) {
        logger.warning("Found player {} with different UUID or name, will now use UUID: {} and name: {}", name, uuid, name);
        fPlayerSQL.update(id, true, uuid.toString(), name, ip);
    }

    private FPlayer convertToFPlayer(PlayerInfo entity) {
        return convertToFPlayer(entity, true);
    }

    private FPlayer convertToFPlayer(PlayerInfo entity, boolean loadSetting) {
        FPlayer fPlayer = new FPlayer(entity.id(), entity.name(), UUID.fromString(entity.uuid()));
        fPlayer.setOnline(entity.online() == 1);
        fPlayer.setIp(entity.ip());

        if (loadSetting) {
            settingDAOProvider.get().load(fPlayer);
        }

        return fPlayer;
    }

    private List<FPlayer> convertToFPlayers(List<PlayerInfo> entities) {
        return entities.stream()
                .map(playerInfo -> convertToFPlayer(playerInfo, false))
                .toList();
    }
}