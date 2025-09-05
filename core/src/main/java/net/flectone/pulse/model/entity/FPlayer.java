package net.flectone.pulse.model.entity;

import lombok.Getter;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a platform-dynamic, Flectone player. All actions done through Flectone involving a player most likely are done through FPlayer.
 * <hr>
 * <p>
 *     For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 *     and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 *
 * @see FPlayerService
 */
@Getter
public class FPlayer extends FEntity {

    public static final FPlayer UNKNOWN = new FPlayer(FEntity.UNKNOWN_NAME);

    private final int id;
    private final Map<FColor.Type, Set<FColor>> fColors = new EnumMap<>(FColor.Type.class);
    private final Map<MessageType, Boolean> settingsBoolean = new EnumMap<>(MessageType.class);
    private final Map<SettingText, String> settingsText = new EnumMap<>(SettingText.class);
    private final List<Ignore> ignores = new ArrayList<>();

    private boolean online;
    private String ip;
    private String constantName;

    public FPlayer(int id, String name, UUID uuid, String type) {
        super(name, uuid, type);

        this.id = id;
    }

    public FPlayer(int id, String name, UUID uuid) {
        this(id, name, uuid, "player");
    }

    public FPlayer(String name) {
        this(-1, name, FEntity.UNKNOWN_UUID, "unknown");
    }

    @Override
    public boolean isUnknown() {
        return this.getId() == -1;
    }

    public void setOnline(boolean online) {
        if (isUnknown()) return;

        this.online = online;
    }

    public void setIp(String ip) {
        if (isUnknown()) return;

        this.ip = ip;
    }

    public void setConstantName(String constantName) {
        if (isUnknown()) return;

        this.constantName = constantName;
    }

    public boolean isIgnored(@NotNull FPlayer fPlayer) {
        if (ignores.isEmpty()) return false;

        return ignores
                .stream()
                .anyMatch(ignore -> ignore.target() == fPlayer.getId());
    }

    public void setSetting(MessageType messageType, boolean value) {
        settingsBoolean.put(messageType, value);
    }

    public void setSetting(SettingText settingText, String value) {
        settingsText.put(settingText, value);
    }

    @Nullable
    public String getSetting(SettingText settingText) {
        return settingsText.get(settingText);
    }

    @NotNull
    public String getSetting(MessageType messageType) {
        return isSetting(messageType) ? "1" : "0";
    }

    public boolean isSetting(MessageType messageType) {
        Boolean value = settingsBoolean.get(messageType);
        return value == null || value;
    }

    public void removeSetting(SettingText settingText) {
        settingsText.remove(settingText);
    }

    public void removeSetting(MessageType messageType) {
        settingsBoolean.remove(messageType);
    }

    public boolean equals(FPlayer fPlayer) {
        return this.id == fPlayer.getId();
    }

    public Map<Integer, String> getFColors(FColor.Type type) {
        return fColors.getOrDefault(type, Collections.emptySet())
                .stream()
                .collect(Collectors.toMap(FColor::number, FColor::name));
    }
}
