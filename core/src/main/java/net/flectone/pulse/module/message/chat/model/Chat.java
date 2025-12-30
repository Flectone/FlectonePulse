package net.flectone.pulse.module.message.chat.model;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Sound;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.Nullable;

public record Chat(@Nullable String name, Message.Chat.@Nullable Type config, Permission.Message.Chat.@Nullable Type permission) {

    public Pair<Cooldown, PermissionSetting> cooldown() {
        if (config == null) return Pair.of(null, null);

        return Pair.of(config.cooldown(), permission == null ? null : permission.cooldownBypass());
    }

    public Pair<Sound, PermissionSetting> sound() {
        if (config == null) return Pair.of(null, null);

        return Pair.of(config.sound(), permission == null ? null : permission.sound());
    }


}
