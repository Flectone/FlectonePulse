package net.flectone.pulse.module.integration;

import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.MessageTag;

import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule extends AbstractModule {

    private final Integration integration;
    private final Permission.Integration permission;

    public IntegrationModule(FileManager fileManager) {
        integration = fileManager.getIntegration();
        permission = fileManager.getPermission().getIntegration();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public abstract String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission);

    public abstract boolean hasFPlayerPermission(FPlayer fPlayer, String permission);

    public abstract String getPrefix(FPlayer fPlayer);

    public abstract String getSuffix(FPlayer fPlayer);

    public abstract Set<String> getGroups();

    public abstract int getGroupWeight(FPlayer fPlayer);

    public abstract String getTextureUrl(FEntity sender);

    public abstract void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString);

    public abstract boolean isVanished(FEntity sender);
}
