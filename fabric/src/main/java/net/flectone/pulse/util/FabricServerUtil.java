package net.flectone.pulse.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.minecraft.server.MinecraftServer;

@Singleton
public class FabricServerUtil implements ServerUtil {

    @Inject
    private MinecraftServer minecraftServer;
    @Inject
    private FPlayerManager fPlayerManager;
    @Inject
    private IntegrationModule integrationModule;

    @Inject
    public FabricServerUtil() {

    }

    @Override
    public String getMinecraftName(Object item) {
        return item.toString();
    }

    @Override
    public String getTPS() {
        return "";
    }

    @Override
    public int getMax() {
        return minecraftServer.getMaxPlayerCount();
    }

    @Override
    public int getOnlineCount() {
        return (int) fPlayerManager.getFPlayers().stream()
                .filter(FPlayer::isOnline)
                .filter(fPlayer -> !fPlayer.isUnknown())
                .filter(fPlayer -> !integrationModule.isVanished(fPlayer))
                .count();
    }

    @Override
    public boolean hasProject(String projectName) {
        return false;
    }

    @Override
    public JsonElement getMOTD() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", minecraftServer.getServerMotd());
        return jsonObject;
    }
}
