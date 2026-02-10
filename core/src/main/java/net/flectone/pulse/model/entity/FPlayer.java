package net.flectone.pulse.model.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static final String TYPE = "PLAYER";

    private final int id;
    private final boolean console;
    private final Map<FColor.Type, Set<FColor>> fColors = new EnumMap<>(FColor.Type.class);
    private final Map<String, Boolean> settingsBoolean = new Object2BooleanArrayMap<>();
    private final Map<SettingText, String> settingsText = new EnumMap<>(SettingText.class);
    private final List<Ignore> ignores = new ObjectArrayList<>();
    private final List<Component> constants = new CopyOnWriteArrayList<>();

    private boolean online;
    private String ip;

    public FPlayer(int id, boolean console, String name, UUID uuid, String type) {
        super(name, uuid, type);
        this.id = id;
        this.console = console;
    }

    public FPlayer(int id, String name, UUID uuid, String type) {
        this(id, false, name, uuid, type);
    }

    public FPlayer(int id, String name, UUID uuid) {
        this(id, name, uuid, FPlayer.TYPE);
    }

    public FPlayer(String name, UUID uuid, String type) {
        this(-1, false, name, uuid, type);
    }

    public FPlayer(boolean console, String name) {
        this(-1, console, name, FEntity.UNKNOWN_UUID, FEntity.UNKNOWN_TYPE);
    }

    public FPlayer(String name) {
        this(-1, false, name, FEntity.UNKNOWN_UUID, FEntity.UNKNOWN_TYPE);
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

    public void setConstants(List<Component> constants) {
        if (isUnknown()) return;
        this.constants.clear();
        this.constants.addAll(constants);
    }

    public boolean isIgnored(@NonNull FPlayer fPlayer) {
        if (ignores.isEmpty()) return false;
        return ignores.stream().anyMatch(ignore -> ignore.target() == fPlayer.getId());
    }

    public void setSetting(String messageType, boolean value) {
        settingsBoolean.put(messageType, value);
    }

    public void setSetting(SettingText settingText, String value) {
        settingsText.put(settingText, value);
    }

    public @Nullable String getSetting(SettingText settingText) {
        return settingsText.get(settingText);
    }

    public @NonNull String getSetting(MessageType messageType) {
        return getSetting(messageType.name());
    }

    public @NonNull String getSetting(String messageType) {
        return isSetting(messageType) ? "1" : "0";
    }

    public boolean isSetting(MessageType messageType) {
        return isSetting(messageType.name());
    }

    public boolean isSetting(String messageType) {
        Boolean value = settingsBoolean.get(messageType);
        return value == null || value;
    }

    public void removeSetting(SettingText settingText) {
        settingsText.remove(settingText);
    }

    public void removeSetting(String messageType) {
        settingsBoolean.remove(messageType);
    }

    public void removeSetting(MessageType messageType) {
        removeSetting(messageType.name());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        FPlayer fPlayer = (FPlayer) object;
        return this.id == fPlayer.getId();
    }

    public Map<Integer, String> getFColors(FColor.Type type) {
        Set<FColor> colors = fColors.get(type);
        if (colors == null || colors.isEmpty()) {
            return Collections.emptyMap();
        }

        Int2ObjectArrayMap<String> result = new Int2ObjectArrayMap<>(colors.size());
        for (FColor color : colors) {
            result.put(color.number(), color.name());
        }

        return result;
    }

}