package net.flectone.pulse.model.entity;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This is a platform-dynamic, Flectone player. All actions done through Flectone involving a player most likely are done through FPlayer.
 * <hr>
 * <p>
 * For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 * and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 *
 * @see FPlayerService
 */
public interface FPlayer extends FEntity {

    static FPlayerImpl.FPlayerImplBuilder builder() {
        return new FPlayerImpl.FPlayerImplBuilder();
    }

    String PLAYER_TYPE = "PLAYER";

    String CONSOLE_TYPE = "CONSOLE";

    String INTEGRATION_TYPE = "INTEGRATION";

    FPlayer UNKNOWN = FPlayer.builder().build();

    Integer id();

    boolean isConsole();

    boolean isIntegration();

    boolean isOnline();

    String ip();

    List<Component> constants();

    FPlayer withName(String name);

    FPlayer withUuid(UUID uuid);

    FPlayer withConsole(boolean console);

    FPlayer withOnline(boolean online);

    FPlayer withId(Integer id);

    FPlayer withIp(String ip);

    FPlayer withConstants(List<Component> constants);

    FPlayerImpl.FPlayerImplBuilder toBuilder();

    @Override
    default boolean isUnknown() {
        return id() == -1 && !isConsole();
    }

    @Builder(toBuilder = true)
    @With
    record FPlayerImpl(
            String name,
            UUID uuid,
            String type,
            Integer id,
            boolean console,
            boolean integration,
            boolean online,
            @Nullable String ip,
            List<Component> constants,
            @Nullable Component showEntityName
    ) implements FPlayer {

        public FPlayerImpl {
            if (name == null) name = FEntity.UNKNOWN_NAME;
            if (uuid == null) uuid = FEntity.UNKNOWN_UUID;
            if (type == null) type = PLAYER_TYPE;
            if (id == null) id = -1;
            if (constants == null) constants = List.of();

            console = console || type.equalsIgnoreCase(CONSOLE_TYPE);
            if (console) type = CONSOLE_TYPE;

            integration = integration || type.equalsIgnoreCase(INTEGRATION_TYPE);
            if (integration) type = INTEGRATION_TYPE;
        }

        @Override
        public boolean isConsole() {
            return console;
        }

        @Override
        public boolean isIntegration() {
            return integration;
        }

        @Override
        public boolean isOnline() {
            return online;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof FPlayer fPlayer)) return false;
            if (this.console != fPlayer.isConsole()) return false;
            if (this.integration != fPlayer.isIntegration()) return false;
            if (!Objects.equals(this.uuid, fPlayer.uuid())) return false;

            return Objects.equals(this.id, fPlayer.id());
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, id, console, integration);
        }

        @Override
        public FPlayer withConstants(@Nullable List<Component> constants) {
            if (constants == null || constants.isEmpty()) {
                if (this.constants.isEmpty()) return this;

                return toBuilder()
                        .constants(List.of())
                        .build();
            }

            return toBuilder()
                    .constants(List.copyOf(constants))
                    .build();
        }

    }
}