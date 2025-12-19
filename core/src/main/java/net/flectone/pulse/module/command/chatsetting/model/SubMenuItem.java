package net.flectone.pulse.module.command.chatsetting.model;

import net.flectone.pulse.config.setting.PermissionSetting;

import java.util.Map;

public record SubMenuItem(String name, String material, Map<Integer, String> colors, PermissionSetting perm) {}
