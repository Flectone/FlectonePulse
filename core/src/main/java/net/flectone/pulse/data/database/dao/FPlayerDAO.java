package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.fplayer.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
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
    private final RandomGenerator randomGenerator;
    private final FLogger logger;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<? extends FPlayerSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> FPlayerH2.class;
            case MARIADB -> FPlayerMariaDB.class;
            case MYSQL -> FPlayerMySQL.class;
            case POSTGRESQL -> FPlayerPostgreSQL.class;
            case SQLITE -> FPlayerSQLite.class;
        };
    }

    /**
     * Inserts a new player into the database or updates an existing player.
     * Handles conflicts by checking for existing records with the same UUID or name.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @param ip the player's IP address, can be null
     * @param online whether the player is currently online
     * @return the created or updated FPlayer object with assigned database ID
     */
    public FPlayer insertOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        try {
            return insertOrUpdateInTransaction(uuid, name, ip, online);
        } catch (Exception e) {
            // player has changed after select and an error appears due to cache, trying to insert a second time
            if (e.getMessage().contains("Record has changed since last read")) {
                return insertOrUpdateInTransaction(uuid, name, ip, online);
            }

            logger.warning(e);
        }

        return FPlayer.UNKNOWN;
    }

    private FPlayer insertOrUpdateInTransaction(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        return inTransaction(sql -> {
            int id;

            Optional<PlayerInfo> existingByUUID = sql.findByUUID(uuid.toString());
            if (existingByUUID.isPresent()) {
                PlayerInfo playerInfo = existingByUUID.get();

                // get current id
                id = playerInfo.id();

                // update old record
                String existingName = playerInfo.name();
                if (!name.equalsIgnoreCase(existingName)) {
                    logger.warning("Player with UUID '%s' changed name: '%s' -> '%s'", uuid, existingName, name);
                }

                sql.update(playerInfo.id(), online, uuid.toString(), name, ip);
            } else {
                Optional<PlayerInfo> existingByName = sql.findByName(name);
                if (existingByName.isPresent()) {
                    PlayerInfo playerInfo = existingByName.get();

                    // get current id
                    id = playerInfo.id();

                    // update old record
                    UUID existingUuid = UUID.fromString(playerInfo.uuid());
                    if (!uuid.equals(existingUuid)) {
                        logger.warning("Player with name '%s' changed UUID: '%s' -> '%s'", name, existingUuid, uuid);
                    }

                    sql.update(playerInfo.id(), online, uuid.toString(), name, ip);
                } else {
                    // insert new record
                    id = sql.insert(online, uuid.toString(), name, ip);
                }
            }

            return FPlayer.builder()
                    .id(id)
                    .uuid(uuid)
                    .name(name)
                    .ip(ip)
                    .online(online)
                    .build();
        });
    }

    /**
     * Inserts a player or ignores if already exists.
     *
     * @param fPlayer the player to insert
     */
    public void insertOrIgnore(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.insertOrIgnore(fPlayer.id(), fPlayer.uuid().toString(), fPlayer.name()));
    }

    /**
     * Updates an existing player in the database.
     *
     * @param fPlayer the player to update
     */
    public void update(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;
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
     * Sets all players associated with the specified server to offline status.
     *
     * @param server the server identifier to match against player server settings
     */
    public void setOfflineByServer(@NonNull String server) {
        if (database.isClosed()) return;
        if (StringUtils.isEmpty(server)) return;

        useHandle(sql -> sql.setOfflineByServer(server));
    }

    /**
     * Gets all online players from the database.
     *
     * @return list of online players
     */
    public List<FPlayer> getOnlineFPlayers() {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> convertToFPlayers(sql.getOnlinePlayers()));
    }

    /**
     * Gets all players from the database.
     *
     * @return list of all players
     */
    public List<FPlayer> getFPlayers() {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> convertToFPlayers(sql.getAllPlayers()));
    }

    /**
     * Gets a player by name.
     *
     * @param name the player name
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull String name) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByName(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).name(name).uuid(UUID.randomUUID()).build())
        );
    }

    /**
     * Gets a player by IP address.
     *
     * @param inetAddress the IP address
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull InetAddress inetAddress) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByIp(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).ip(inetAddress.getHostAddress()).uuid(UUID.randomUUID()).build())
        );
    }

    /**
     * Gets a player by UUID.
     *
     * @param uuid the player UUID
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull UUID uuid) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByUUID(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).uuid(uuid).build())
        );
    }

    /**
     * Gets a player by database ID.
     *
     * @param id the player database ID
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(int id) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findById(id)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(id).uuid(UUID.randomUUID()).build())
        );
    }

    private int nextRandomId() {
        return randomGenerator.nextInt(Integer.MIN_VALUE, -1);
    }

    private FPlayer convertToFPlayer(PlayerInfo info) {
        return FPlayer.builder()
                .id(info.id())
                .name(info.name())
                .uuid(UUID.fromString(info.uuid()))
                .online(info.online())
                .ip(info.ip())
                .build();
    }

    private List<FPlayer> convertToFPlayers(List<PlayerInfo> entities) {
        return entities.stream()
                .map(this::convertToFPlayer)
                .toList();
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
    ) {
    }
}