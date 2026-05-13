package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.fplayer.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
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
     * Inserts a new player into the database.
     * Handles UUID and name conflicts by updating existing records.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @return true if a new player was inserted, false if an existing player was updated
     */
    public boolean insert(@NonNull UUID uuid, @NonNull String name) {
        return inTransaction(sql -> {
            Optional<PlayerInfo> existingByUUID = sql.findByUUID(uuid.toString());
            if (existingByUUID.isPresent()) {
                PlayerInfo playerInfo = existingByUUID.get();

                String existingName = playerInfo.name();
                if (!name.equalsIgnoreCase(existingName)) {
                    logger.warning("Player with UUID '%s' changed name: '%s' -> '%s'", uuid, existingName, name);

                    sql.update(playerInfo.id(), true, uuid.toString(), name, playerInfo.ip());
                }

                return false;
            }

            Optional<PlayerInfo> existingByName = sql.findByName(name);
            if (existingByName.isPresent()) {
                PlayerInfo playerInfo = existingByName.get();

                UUID existingUuid = UUID.fromString(playerInfo.uuid());
                if (!uuid.equals(existingUuid)) {
                    logger.warning("Player with name '%s' changed UUID: '%s' -> '%s'", name, existingUuid, uuid);

                    sql.update(playerInfo.id(), true, uuid.toString(), name, playerInfo.ip());
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
        useHandle(sql -> sql.insertOrIgnore(fPlayer.id(), fPlayer.uuid().toString(), fPlayer.name()));
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
        return withHandle(sql -> sql.findByNameWithSettings(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().name(name).uuid(UUID.randomUUID()).build())
        );
    }

    /**
     * Gets a player by IP address.
     *
     * @param inetAddress the IP address
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull InetAddress inetAddress) {
        return withHandle(sql -> sql.findByIpWithSettings(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().ip(inetAddress.getHostAddress()).uuid(UUID.randomUUID()).build())
        );
    }

    /**
     * Gets a player by UUID.
     *
     * @param uuid the player UUID
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(@NonNull UUID uuid) {
        return withHandle(sql -> sql.findByUUIDWithSettings(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.withUuid(uuid))
        );
    }

    /**
     * Gets a player by database ID.
     *
     * @param id the player database ID
     * @return the player or FPlayer.UNKNOWN if not found
     */
    public FPlayer getFPlayer(int id) {
        return withHandle(sql -> sql.findByIdWithSettings(id)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(id).uuid(UUID.randomUUID()).build())
        );
    }

    private FPlayer convertToFPlayer(PlayerInfo entity) {
        return convertToFPlayer(entity, true);
    }

    private FPlayer convertToFPlayer(PlayerInfo info, boolean loadSetting) {
        FPlayer.FPlayerImpl.FPlayerImplBuilder fPlayer = FPlayer.builder()
                .id(info.id())
                .name(info.name())
                .uuid(UUID.fromString(info.uuid()))
                .online(info.online())
                .ip(info.ip());

        if (loadSetting) {
            Map<String, Boolean> settingsBoolean = info.settingsBoolean();
            if (settingsBoolean != null && !settingsBoolean.isEmpty()) {
                fPlayer = fPlayer.settingsBoolean(Map.copyOf(settingsBoolean));
            }

            Map<SettingText, String> settingsText = info.settingsText();
            if (settingsText != null && !settingsText.isEmpty()) {
                fPlayer = fPlayer.settingsText(Map.copyOf(settingsText));
            }
        }

        return fPlayer.build();
    }

    private List<FPlayer> convertToFPlayers(List<PlayerInfo> entities) {
        return entities.stream()
                .map(playerInfo -> convertToFPlayer(playerInfo, false))
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
            @Nullable String ip,

            // ignore value for sql
            @Nullable Map<String, Boolean> settingsBoolean,

            // ignore value for sql
            @Nullable Map<SettingText, String> settingsText
    ) {
    }
}