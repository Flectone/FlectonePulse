package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class BukkitChatsettingModule extends ChatsettingModule {

    private final ComponentUtil componentUtil;

    @Inject
    public BukkitChatsettingModule(FileManager fileManager,
                                   SettingDAO settingDAO,
                                   ComponentUtil componentUtil,
                                   CommandUtil commandUtil,
                                   PermissionUtil permissionUtil,
                                   InventoryController inventoryController) {
        super(fileManager, settingDAO, componentUtil, commandUtil, permissionUtil, inventoryController);

        this.componentUtil = componentUtil;
    }

    @Override
    public ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem) {

        Component name = itemMessages.isEmpty()
                ? Component.empty()
                : componentUtil.builder(fPlayer, itemMessages.get(0)).build();

        List<Component> lore = new ArrayList<>();
        if (itemMessages.size() > 1) {
            itemMessages.stream()
                    .skip(1)
                    .forEach(string -> lore.add(
                                    componentUtil
                                            .builder(fPlayer, string.replace("<chat>", String.valueOf(fPlayer.getSettingValue(FPlayer.Setting.CHAT))))
                                            .build()
                            )
                    );
        }

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            return new ItemStack.Builder()
                    .type(SpigotConversionUtil.fromBukkitItemMaterial(Material.valueOf(settingItem.getMaterials().get(settingIndex))))
                    .component(ComponentTypes.ITEM_NAME, name)
                    .component(ComponentTypes.LORE, new ItemLore(lore))
                    .build();
        }

        org.bukkit.inventory.ItemStack legacyItemStack = new org.bukkit.inventory.ItemStack(Material.valueOf(settingItem.getMaterials().get(settingIndex)));

        ItemMeta itemMeta = legacyItemStack.getItemMeta();
        itemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
        itemMeta.setLore(lore.stream()
                .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                .toList()
        );

        legacyItemStack.setItemMeta(itemMeta);

        return SpigotConversionUtil.fromBukkitItemStack(legacyItemStack);
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executesPlayer(this::executesFPlayer)
                .override();
    }
}
