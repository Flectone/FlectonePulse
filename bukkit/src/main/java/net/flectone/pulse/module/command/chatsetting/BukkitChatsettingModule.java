package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.InventoryManager;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class BukkitChatsettingModule extends ChatsettingModule {

    private final ComponentUtil componentUtil;

    @Inject
    public BukkitChatsettingModule(FileManager fileManager,
                                   ThreadManager threadManager,
                                   InventoryManager inventoryManager,
                                   ComponentUtil componentUtil,
                                   PermissionUtil permissionUtil) {
        super(fileManager, threadManager, inventoryManager, componentUtil, permissionUtil);

        this.componentUtil = componentUtil;
    }

    @Override
    public ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem) {
        Component name = itemMessages.isEmpty() ? Component.empty() : componentUtil.builder(fPlayer, itemMessages.get(0)).build();
        List<Component> lore = new ArrayList<>();
        if (itemMessages.size() > 1) {
            itemMessages.stream()
                    .skip(1)
                    .forEach(string -> lore.add(
                                    componentUtil
                                            .builder(fPlayer, string.replace("<chat>", String.valueOf(fPlayer.getChat())))
                                            .build()
                            )
                    );
        }

        return new ItemStack.Builder()
                .type(SpigotConversionUtil.fromBukkitItemMaterial(Material.valueOf(settingItem.getMaterials().get(settingIndex))))
                .component(ComponentTypes.ITEM_NAME, name)
                .component(ComponentTypes.LORE, new ItemLore(lore))
                .build();
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executesPlayer(this::executesFPlayer)
                .override();
    }
}
