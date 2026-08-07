package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Sound;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A module that speaks to players and therefore has translatable text, and optionally a
 * cooldown and a sound.
 * @author TheFaser
 */
public interface ModuleLocalization extends ModuleSimple {

    /**
     * The localization section for a player, picked from their chosen language.
     *
     * @param fPlayer the player the text is for
     * @return the localization settings
     */
    LocalizationSetting localization(FPlayer fPlayer);

    @Override
    default Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(ModuleSimple.super.permissions());

        if (permission() instanceof CooldownPermissionSetting cooldownPermission) {
            permissions.add(cooldownPermission.cooldownBypass());
        }

        if (permission() instanceof SoundPermissionSetting soundPermission) {
            permissions.add(soundPermission.sound());
        }

        return Collections.unmodifiableSet(permissions);
    }

    /**
     * The cooldown for this module together with the permission that bypasses it, if it has one.
     *
     * @return the cooldown and its bypass permission, or empty if the module has no cooldown
     */
    default Optional<Pair<Cooldown, PermissionSetting>> cooldown() {
        if (config() instanceof CooldownConfigSetting cooldownSetting
                && permission() instanceof CooldownPermissionSetting cooldownPermission) {
            return Optional.of(Pair.of(cooldownSetting.cooldown(), cooldownPermission.cooldownBypass()));
        }

        return Optional.empty();
    }

    /**
     * The cooldown, for callers that already know the module is configured with one.
     *
     * @return the cooldown and its bypass permission
     * @throws IllegalStateException if the module has no cooldown configured
     */
    default Pair<Cooldown, PermissionSetting> cooldownOrThrow() {
        return cooldown().orElseThrow(() -> new IllegalStateException(
                "Cooldown not configured for module: " + getClass().getSimpleName()
        ));
    }

    /**
     * The sound for this module together with the permission needed to hear it, if it has one.
     *
     * @return the sound and its permission, or empty if the module has no sound
     */
    default Optional<Pair<Sound, PermissionSetting>> sound() {
        if (config() instanceof SoundConfigSetting soundSetting
                && permission() instanceof SoundPermissionSetting soundPermission) {
            return Optional.of(Pair.of(soundSetting.sound(), soundPermission.sound()));
        }

        return Optional.empty();
    }

    /**
     * The sound, for callers that already know the module is configured with one.
     *
     * @return the sound and its permission
     * @throws IllegalStateException if the module has no sound configured
     */
    default Pair<Sound, PermissionSetting> soundOrThrow() {
        return sound().orElseThrow(() -> new IllegalStateException(
                "Sound not configured for module: " + getClass().getSimpleName()
        ));
    }

}
