package net.flectone.pulse.module.message.chat.model;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Sound;
import org.jspecify.annotations.Nullable;

/**
 * One configured chat, together with the settings and permissions attached to it.
 *
 * @param name the chat name, or null when no chat matched
 * @param config the chat settings, or null
 * @param permission the chat permissions, or null
 * @author TheFaser
 */
public record Chat(
        @Nullable String name,
        Message.Chat.@Nullable Type config,
        Permission.Message.Chat.@Nullable Type permission
) {

    /**
     * The chat's cooldown with the permission that bypasses it.
     *
     * @return the cooldown and its bypass permission
     */
    public Pair<Cooldown, PermissionSetting> cooldown() {
        if (config == null) return Pair.of(null, null);

        return Pair.of(config.cooldown(), permission == null ? null : permission.cooldownBypass());
    }

    /**
     * The chat's sound with the permission needed to hear it.
     *
     * @return the sound and its permission
     */
    public Pair<Sound, PermissionSetting> sound() {
        if (config == null) return Pair.of(null, null);

        return Pair.of(config.sound(), permission == null ? null : permission.sound());
    }


}
