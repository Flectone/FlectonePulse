package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.MessageTag;

import java.util.function.UnaryOperator;

@Singleton
public class FabricIntegrationModule extends IntegrationModule {

    @Inject
    public FabricIntegrationModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String checkMention(FPlayer fPlayer, Object event) {
        return null;
    }

    @Override
    public String mark(FEntity sender, String message) {
        return message;
    }

    @Override
    public String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission) {
        return "";
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        return true;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        return "";
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        return "";
    }

    @Override
    public int getGroupWeight(FPlayer fPlayer) {
        return 0;
    }

    @Override
    public String getTextureUrl(FEntity sender) {
        return "";
    }

    @Override
    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString) {

    }

    @Override
    public boolean isVanished(FEntity sender) {
        return false;
    }
}
