package net.flectone.pulse.persistence.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.persistence.database.sql.fplayer.*;
import net.flectone.pulse.util.random.RandomGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerDAO implements BaseDAO<FPlayerSQL> {

    private final DatabaseImpl database;
    private final RandomGenerator randomGenerator;
    private final FLogger logger;

    @Override
    public DatabaseImpl database() {
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

    public void insertOrIgnore(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.insertOrIgnore(fPlayer.id(), fPlayer.uuid().toString(), fPlayer.name()));
    }

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

    public void setOfflineByServer(@NonNull String server) {
        if (database.isClosed()) return;
        if (StringUtils.isEmpty(server)) return;

        useHandle(sql -> sql.setOfflineByServer(server));
    }

    public List<FPlayer> getOnlineFPlayers() {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> convertToFPlayers(sql.getOnlinePlayers()));
    }

    public int getTotalFPlayersCountByIp(@NonNull String ip) {
        if (database.isClosed()) return 0;

        return withHandle(sql -> sql.getTotalPlayersCountByIp(ip));
    }

    public List<FPlayer> getFPlayersByIp(@NonNull String ip, int limit, int offset) {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> convertToFPlayers(sql.getPlayersByIp(ip, limit, offset)));
    }

    public List<FPlayer> getFPlayers() {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> convertToFPlayers(sql.getAllPlayers()));
    }

    public FPlayer getFPlayer(@NonNull String name) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByName(name)
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).name(name).uuid(UUID.randomUUID()).build())
        );
    }

    public FPlayer getFPlayer(@NonNull InetAddress inetAddress) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByIp(inetAddress.getHostAddress())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).ip(inetAddress.getHostAddress()).uuid(UUID.randomUUID()).build())
        );
    }

    public FPlayer getFPlayer(@NonNull UUID uuid) {
        if (database.isClosed()) return FPlayer.UNKNOWN;

        return withHandle(sql -> sql.findByUUID(uuid.toString())
                .map(this::convertToFPlayer)
                .orElse(FPlayer.UNKNOWN.toBuilder().id(nextRandomId()).uuid(uuid).build())
        );
    }

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

    public record PlayerInfo(
            int id,
            boolean online,
            @NonNull String uuid,
            @NonNull String name,
            @Nullable String ip
    ) {
    }
}