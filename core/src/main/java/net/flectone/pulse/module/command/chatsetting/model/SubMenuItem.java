package net.flectone.pulse.module.command.chatsetting.model;

import net.flectone.pulse.config.Permission;

import java.util.Map;

public record SubMenuItem(String name, String material, Map<Integer, String> colors, Permission.IPermission perm) {}
