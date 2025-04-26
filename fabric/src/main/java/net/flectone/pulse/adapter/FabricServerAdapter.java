package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Singleton
public class FabricServerAdapter implements PlatformServerAdapter {

    private final MinecraftServer minecraftServer;
    private final FLogger fLogger;
    private final Injector injector;

    @Inject
    public FabricServerAdapter(MinecraftServer minecraftServer,
                               FLogger fLogger,
                               Injector injector) {
        this.minecraftServer = minecraftServer;
        this.fLogger = fLogger;
        this.injector = injector;
    }

    @Override
    public void dispatchCommand(String command) {
        try {
            minecraftServer.getCommandManager().getDispatcher().execute(command, minecraftServer.getCommandSource());
        } catch (CommandSyntaxException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public @NotNull String getTPS() {
        return "-1";
    }

    @Override
    public int getMaxPlayers() {
        return minecraftServer.getMaxPlayerCount();
    }

    @Override
    public int getOnlinePlayerCount() {
        return minecraftServer.getCurrentPlayerCount();
    }

    @Override
    public @NotNull String getServerCore() {
        return "Fabric";
    }

    @Override
    public @NotNull JsonElement getMOTD() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", minecraftServer.getServerMotd());
        return jsonObject;
    }

    @Override
    public boolean hasProject(String projectName) {
        return FabricLoader.getInstance().isModLoaded(projectName);
    }

    @Override
    public boolean isOnlineMode() {
        return minecraftServer.isOnlineMode();
    }

    @Override
    public @NotNull String getItemName(Object item) {
        return item instanceof net.minecraft.item.ItemStack itemStack ? itemStack.getItemName().getString() : "";
    }

    @Override
    public @NotNull Component translateItemName(Object item, boolean translatable) {
        return Component.text(getItemName(item));
    }

    @Override
    public @NotNull ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<List<String>> messages, Command.Chatsetting.SettingItem settingItem) {
        return new ItemStack.Builder().type(ItemTypes.ACACIA_BUTTON).build();
    }
}
