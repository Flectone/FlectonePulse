package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.FPlayerSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerDAO extends BaseDAO<FPlayerSQL> {

    private final Database database;
    private final FLogger logger;
    private final Provider<SettingDAO> settingDAOProvider;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<FPlayerSQL> sqlClass() {
        return FPlayerSQL.class;
    }

    public record PlayerInfo(int id, boolean online, String uuid, String name, @Nullable String ip) {}

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
                    updateAndWarn(sql, playerInfo.id(), uuid, name, playerInfo.ip());
                }

                return false;
            }

            sql.insert(uuid.toString(), name);
            return true;
        });
    }

    public void insertOrIgnore(FPlayer fPlayer) {
        useHandle(sql -> {
            Optional<FPlayerDAO.PlayerInfo> existingPlayer = sql.findByUUID(fPlayer.getUuid().toString());

            if (existingPlayer.isEmpty()) {
                sql.insertWithId(fPlayer.getId(), fPlayer.getUuid().toString(), fPlayer.getName());
            }
        });
    }

    public void update(FPlayer fPlayer) {
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
        return withHandle(sql -> sql.findByName(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return withHandle(sql -> sql.findByIp(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(UUID uuid) {
        return withHandle(sql -> sql.findByUUID(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    @NotNull
    public FPlayer getFPlayer(int id) {
        return withHandle(sql -> sql.findById(id)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    private void updateAndWarn(FPlayerSQL fPlayerSQL, int id, UUID uuid, String name, String ip) {
        logger.warning("Found player " + name + " with different UUID or name, will now use UUID: " + uuid + " and name: " + name);
        fPlayerSQL.update(id, true, uuid.toString(), name, ip);
    }

    private FPlayer convertToFPlayer(PlayerInfo entity) {
        return convertToFPlayer(entity, true);
    }

    private FPlayer convertToFPlayer(PlayerInfo info, boolean loadSetting) {
        FPlayer fPlayer = new FPlayer(info.id(), info.name(), UUID.fromString(info.uuid()));
        fPlayer.setOnline(info.online());
        fPlayer.setIp(info.ip());

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