package net.flectone.pulse.module.message.contact.unsign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.unsign.listener.UnsignListener;
import net.flectone.pulse.util.DyeUtil;
import net.flectone.pulse.util.ItemUtil;
import net.flectone.pulse.util.Pair;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@Singleton
public class UnsignModule extends AbstractModuleMessage<Localization.Message.Contact> {

    private final Message.Contact.Unsign message;
    private final Permission.Message.Contact.Unsign permission;

    private final BukkitListenerManager bukkitListenerManager;
    private final ItemUtil itemUtil;
    private final DyeUtil dyeUtil;
    private final FPlayerManager fPlayerManager;

    @Inject
    public UnsignModule(FileManager fileManager,
                        BukkitListenerManager bukkitListenerManager,
                        ItemUtil itemUtil,
                        DyeUtil dyeUtil,
                        FPlayerManager fPlayerManager) {
        super(localization -> localization.getMessage().getContact());
        this.bukkitListenerManager = bukkitListenerManager;
        this.itemUtil = itemUtil;
        this.dyeUtil = dyeUtil;
        this.fPlayerManager = fPlayerManager;

        message = fileManager.getMessage().getContact().getUnsign();
        permission = fileManager.getPermission().getMessage().getContact().getUnsign();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        bukkitListenerManager.register(UnsignListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void unSign(@NotNull FPlayer fPlayer, @NotNull ItemStack itemStack, @NotNull Block clickedBlock) {
        if (checkModulePredicates(fPlayer)) return;
        if (clickedBlock.getLocation().getWorld() == null) return;
        if (!clickedBlock.getType().toString().equalsIgnoreCase(message.getBlock())) return;

        ItemStack itemToDrop = itemStack.clone();
        itemToDrop.setAmount(1);

        ItemMeta itemMeta = itemToDrop.getItemMeta();
        if (itemMeta == null) return;

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) return;

        Pair<Integer, int[]> indexAndArray = itemUtil.findSignIndex(itemMeta, itemLore, fPlayer);

        int index = indexAndArray.getKey();
        int[] signIndexes = indexAndArray.getValue();

        if (index == -1) return;

        int[] newSignIndexes = new int[signIndexes.length - 1];

        int k = 0;
        for (int signIndex : signIndexes) {
            if (signIndex == index) continue;
            newSignIndexes[k++] = signIndex;
        }

        String sign = itemLore.get(index);

        TextColor textColor = LegacyComponentSerializer.legacySection().deserialize(sign).color();
        if (textColor == null) return;

        String itemDye = dyeUtil.hexToDye(textColor);
        if (itemDye == null) return;

        itemLore.remove(index);
        itemMeta.setLore(itemLore);

        if (newSignIndexes.length == 0) {
            itemUtil.removeSignIndex(itemMeta);
        } else {
            itemUtil.setSignIndex(itemMeta, newSignIndexes);
        }

        itemToDrop.setItemMeta(itemMeta);

        itemUtil.decreaseItemAmount(itemStack, () -> {
            Player player = Bukkit.getPlayer(fPlayer.getUuid());
            if (player == null) return;
            player.getInventory().setItemInMainHand(null);
        });

        Location dropLocation = clickedBlock.getLocation().add(0.5, 1, 0.5);

        itemUtil.dropItem(dropLocation, itemToDrop);

        if (message.isDropDye()) {
            itemUtil.dropItem(dropLocation, new ItemStack(Material.valueOf(itemDye)));
        }

        fPlayerManager.playSound(getSound(), fPlayer, clickedBlock.getLocation());
    }
}
