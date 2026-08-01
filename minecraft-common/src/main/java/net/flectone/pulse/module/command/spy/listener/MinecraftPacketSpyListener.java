package net.flectone.pulse.module.command.spy.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.command.spy.SpyModule;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketSpyListener implements PacketListener {

    private static final int ANVIL_TYPE = 8;

    private final Map<UUID, String> anvilPlayers = new ConcurrentHashMap<>();

    private final SpyModule spyModule;

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        anvilPlayers.remove(uuid);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow wrapperPlayServerOpenWindow = new WrapperPlayServerOpenWindow(event);

            int windowType = wrapperPlayServerOpenWindow.getType();
            if (windowType != 8 && windowType != 0) return;

            if (wrapperPlayServerOpenWindow.getType() == ANVIL_TYPE
                    || (wrapperPlayServerOpenWindow.getLegacyType() != null && wrapperPlayServerOpenWindow.getLegacyType().toLowerCase().contains("anvil"))) {
                anvilPlayers.put(event.getUser().getUUID(), "");
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
            anvilPlayers.remove(event.getUser().getUUID());
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Client.CHAT_COMMAND -> {
                WrapperPlayClientChatCommand wrapperPlayClientChatCommand = new WrapperPlayClientChatCommand(event);
                spyModule.checkCommand(event.getUser().getUUID(), wrapperPlayClientChatCommand.getCommand());
            }
            case PacketType.Play.Client.CHAT_COMMAND_UNSIGNED -> {
                WrapperPlayClientChatCommandUnsigned wrapperPlayClientChatCommandUnsigned = new WrapperPlayClientChatCommandUnsigned(event);
                spyModule.checkCommand(event.getUser().getUUID(), wrapperPlayClientChatCommandUnsigned.getCommand());
            }
            case PacketType.Play.Client.CHAT_MESSAGE -> {
                WrapperPlayClientChatMessage wrapperPlayClientChatMessage = new WrapperPlayClientChatMessage(event);
                spyModule.checkChat(event.getUser().getUUID(), wrapperPlayClientChatMessage.getMessage(), List.of());
            }
            case PacketType.Play.Client.EDIT_BOOK -> {
                WrapperPlayClientEditBook wrapperPlayClientEditBook = new WrapperPlayClientEditBook(event);

                ItemStack itemStack = wrapperPlayClientEditBook.getItemStack();
                if (itemStack == null) {
                    spyModule.checkBook(event.getUser().getUUID(), wrapperPlayClientEditBook.getTitle(), wrapperPlayClientEditBook.getPages());
                } else {
                    NBTCompound nbtCompound = itemStack.getNBT();
                    if (nbtCompound != null) {
                        String title = nbtCompound.getStringTagValueOrNull("title");

                        List<String> pages = new ArrayList<>();
                        NBTList<NBTString> pagesTag = nbtCompound.getStringListTagOrNull("pages");
                        if (pagesTag != null) {
                            for (NBTString page : pagesTag.getTags()) {
                                pages.add(page.getValue());
                            }
                        }

                        spyModule.checkBook(event.getUser().getUUID(), title, pages);
                    }
                }
            }
            case PacketType.Play.Client.UPDATE_SIGN -> {
                WrapperPlayClientUpdateSign wrapperPlayClientUpdateSign = new WrapperPlayClientUpdateSign(event);
                spyModule.checkSign(event.getUser().getUUID(), wrapperPlayClientUpdateSign.getTextLines());
            }
            case PacketType.Play.Client.NAME_ITEM -> {
                WrapperPlayClientNameItem wrapperPlayClientNameItem = new WrapperPlayClientNameItem(event);

                UUID player = event.getUser().getUUID();
                if (anvilPlayers.containsKey(player)) {
                    anvilPlayers.put(player, wrapperPlayClientNameItem.getItemName());
                }
            }
            case PacketType.Play.Client.CLICK_WINDOW -> {
                WrapperPlayClientClickWindow wrapperPlayClientClickWindow = new WrapperPlayClientClickWindow(event);
                if (wrapperPlayClientClickWindow.getSlot() != 2) return;

                UUID player = event.getUser().getUUID();
                String itemName = anvilPlayers.remove(player);
                if (StringUtils.isEmpty(itemName)) return;

                spyModule.checkAnvil(event.getUser().getUUID(), itemName);
            }
            case PacketType.Play.Client.CLOSE_WINDOW -> anvilPlayers.remove(event.getUser().getUUID());
            default -> {
                // nothing to check
            }
        }

    }
}
