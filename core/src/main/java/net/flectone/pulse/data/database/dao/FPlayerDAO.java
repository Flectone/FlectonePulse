package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.FPlayerSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for player data in FlectonePulse.
 * Handles player registration, retrieval, and updates in the database.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerDAO implements BaseDAO<FPlayerSQL> {

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

    /**
     * Represents player information retrieved from the database.
     *
     * @param id the player's database ID
     * @param online whether the player is online
     * @param uuid the player's UUID
     * @param name the player's name
     * @param ip the player's IP address, may be null
     */
    public record PlayerInfo(
            int id,
            boolean online,
            @NonNull String uuid,
            @NonNull String name,
            @Nullable String ip
    ) {}

    /**
     * Inserts a new player into the database.
     * Handles UUID and name conflicts by updating existing records.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @return true if a new player was inserted, false if an existing player was updated
     */
    public boolean insert(@NonNull UUID uuid, @NonNull String name) {
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

    /**
     * Inserts a player or ignores if already exists.
     *
     * @param fPlayer the player to insert
     */
    public void insertOrIgnore(@NonNull FPlayer fPlayer) {
        useHandle(sql -> {
            Optional<FPlayerDAO.PlayerInfo> existingPlayer = sql.findByUUID(fPlayer.uuid().toString());

            if (existingPlayer.isEmpty()) {
                sql.insertWithId(fPlayer.id(), fPlayer.uuid().toString(), fPlayer.name());
            }
        });
    }

    /**
     * Updates an existing player in the database.
     *
     * @param fPlayer the player to update
     */
    public void update(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        useHandle(sql -> sql.update(
                fPlayer.id(),
                fPlayer.isOnline(),
                fPlayer.uuid().toString(),
                fPlayer.name(),
                fPlayer.ip()
        ));
    }

    /**
     * Gets all online players from the database.
     *
     * @return list of online players
     */
    public List<FPlayer> getOnlineFPlayers() {
        return withHandle(sql -> convertToFPlayers(sql.getOnlinePlayers()));
    }

    /**
     * Gets all players from the database.
     *
     * @return list of all players
     */
    public List<FPlayer> getFPlayers() {
        return withHandle(sql -> convertToFPlayers(sql.getAllPlayers()));
    }

    /**
     * Gets a player by name.
     *
     * @param name the player name
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull String name) {
        return withHandle(sql -> sql.findByName(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    /**
     * Gets a player by IP address.
     *
     * @param inetAddress the IP address
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull InetAddress inetAddress) {
        return withHandle(sql -> sql.findByIp(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    /**
     * Gets a player by UUID.
     *
     * @param uuid the player UUID
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull UUID uuid) {
        return withHandle(sql -> sql.findByUUID(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN)
        );
    }

    /**
     * Gets a player by database ID.
     *
     * @param id the player database ID
     * @return the player or FPlayer.UNKNOWN if not found
     */
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
        FPlayer fPlayer = FPlayer.builder()
                .id(info.id())
                .name(info.name())
                .uuid(UUID.fromString(info.uuid()))
                .online(info.online())
                .ip(info.ip())
                .build();

        return loadSetting ? settingDAOProvider.get().load(fPlayer) : fPlayer;
    }

    private List<FPlayer> convertToFPlayers(List<PlayerInfo> entities) {
        return entities.stream()
                .map(playerInfo -> convertToFPlayer(playerInfo, false))
                .toList();
    }
}