package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import net.flectone.pulse.config.setting.*;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Sound;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Optional;

public interface ModuleLocalization<L extends LocalizationSetting> extends ModuleSimple {

    L localization(FEntity sender);

    @Override
    default ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        ImmutableList.Builder<PermissionSetting> builder = ModuleSimple.super.permissionBuilder();

        if (permission() instanceof CooldownPermissionSetting cooldownPermission) {
            builder.add(cooldownPermission.cooldownBypass());
        }

        if (permission() instanceof SoundPermissionSetting soundPermission) {
            builder.add(soundPermission.sound());
        }

        return builder;
    }

    default L localization() {
        return localization(FPlayer.UNKNOWN);
    }

    default Optional<Pair<Cooldown, PermissionSetting>> cooldown() {
        if (config() instanceof CooldownConfigSetting cooldownSetting
                && permission() instanceof CooldownPermissionSetting cooldownPermission) {
            return Optional.of(Pair.of(cooldownSetting.cooldown(), cooldownPermission.cooldownBypass()));
        }

        return Optional.empty();
    }

    default Pair<Cooldown, PermissionSetting> cooldownOrThrow() {
        return cooldown().orElseThrow(() -> new IllegalStateException(
                "Cooldown not configured for module: " + getClass().getSimpleName()
        ));
    }

    default Optional<Pair<Sound, PermissionSetting>> sound() {
        if (config() instanceof SoundConfigSetting soundSetting
                && permission() instanceof SoundPermissionSetting soundPermission) {
            return Optional.of(Pair.of(soundSetting.sound(), soundPermission.sound()));
        }

        return Optional.empty();
    }

    default Pair<Sound, PermissionSetting> soundOrThrow() {
        return sound().orElseThrow(() -> new IllegalStateException(
                "Sound not configured for module: " + getClass().getSimpleName()
        ));
    }

}
