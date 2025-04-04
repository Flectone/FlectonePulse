package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.model.FPlayer;

import java.util.List;

public abstract class PlatformServerAdapter {

    public abstract void dispatchCommand(String command);

    public abstract String getMinecraftName(Object item);

    public abstract String getTPS();

    public abstract int getMax();

    public abstract int getOnlineCount();

    public abstract boolean hasProject(String projectName);

    public abstract JsonElement getMOTD();

    public abstract ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem);

}
