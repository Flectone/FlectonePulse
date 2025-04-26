package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;

import java.util.Set;

@Singleton
public class FabricIntegrationModule extends IntegrationModule {

    @Inject
    public FabricIntegrationModule(FileManager fileManager,
                                   Injector injector) {
        super(fileManager, injector);
    }

    @Override
    public String checkMention(FEntity fPlayer, String message) {
        return "";
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        return false;
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
    public Set<String> getGroups() {
        return Set.of();
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
    public boolean hasMessenger() {
        return false;
    }

    @Override
    public boolean isVanished(FEntity sender) {
        return false;
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        return "";
    }
}
