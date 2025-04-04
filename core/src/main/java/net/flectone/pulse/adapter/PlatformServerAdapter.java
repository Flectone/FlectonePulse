package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.model.FPlayer;

import java.util.List;

public abstract class PlatformServerAdapter {

    public abstract void dispatchCommand(String command);

    public abstract ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem);

}
