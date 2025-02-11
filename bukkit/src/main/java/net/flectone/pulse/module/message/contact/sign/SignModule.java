package net.flectone.pulse.module.message.contact.sign;

import com.github.retrooper.packetevents.util.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.sign.listener.SignListener;
import net.flectone.pulse.platform.SoundPlayer;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Singleton
public class SignModule extends AbstractModuleMessage<Localization.Message.Contact.Sign> {

    private final Message.Contact.Sign message;
    private final Permission.Message.Contact.Sign permission;

    private final BukkitListenerRegistry bukkitListenerManager;
    private final DyeUtil dyeUtil;
    private final ItemUtil itemUtil;
    private final ComponentUtil componentUtil;
    private final SoundPlayer soundPlayer;
    private final PermissionUtil permissionUtil;

    @Inject
    public SignModule(FileManager fileManager,
                      BukkitListenerRegistry bukkitListenerManager,
                      ItemUtil itemUtil,
                      DyeUtil dyeUtil,
                      ComponentUtil componentUtil,
                      SoundPlayer soundPlayer,
                      PermissionUtil permissionUtil) {
        super(localization -> localization.getMessage().getContact().getSign());
        this.bukkitListenerManager = bukkitListenerManager;
        this.itemUtil = itemUtil;
        this.dyeUtil = dyeUtil;
        this.componentUtil = componentUtil;
        this.soundPlayer = soundPlayer;
        this.permissionUtil = permissionUtil;

        message = fileManager.getMessage().getContact().getSign();
        permission = fileManager.getPermission().getMessage().getContact().getSign();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        bukkitListenerManager.register(SignListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public TagResolver dyeTag(@Nullable FPlayer fPlayer, @NotNull TextColor color) {
        if (!isEnable()) return TagResolver.empty();
        if (!permissionUtil.has(fPlayer, getModulePermission())) return TagResolver.empty();

        return TagResolver.resolver("dye", (argumentQueue, context) ->
                Tag.inserting(Component.empty().color(color)));
    }

    @Async
    public void sign(@NotNull FPlayer fPlayer, @NotNull Block clickedBlock) {
        if (checkModulePredicates(fPlayer)) return;
        if (clickedBlock.getLocation().getWorld() == null) return;
        if (!clickedBlock.getType().toString().equalsIgnoreCase(message.getBlock())) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack itemToDrop = inventory.getItemInMainHand().clone();
        ItemStack itemDye = inventory.getItemInOffHand();
        itemToDrop.setAmount(1);

        ItemMeta itemMeta = itemToDrop.getItemMeta();
        if (itemMeta == null) return;

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) itemLore = new ArrayList<>();

        Pair<Integer, int[]> indexAndArray = itemUtil.findSignIndex(itemMeta, itemLore, fPlayer);

        int index = indexAndArray.getKey();
        int[] signIndexes = indexAndArray.getValue();

        TextColor color = dyeUtil.dyeToHex(itemDye.getType().name());
        if (color == null) return;

        String sign = resolveLocalization(fPlayer).getFormat();
        Component component = componentUtil.builder(fPlayer, sign)
                .tagResolvers(dyeTag(fPlayer, color))
                .build();
        sign = LegacyComponentSerializer.legacySection().serialize(component);

        String previousSign = null;

        if (index == -1) {
            signIndexes = Arrays.copyOf(signIndexes, signIndexes.length + 1);
            signIndexes[signIndexes.length - 1] = itemLore.size();
            itemLore.add(sign);
        } else {
            previousSign = itemLore.get(index);
            itemLore.set(index, sign);
        }

        itemUtil.setSignIndex(itemMeta, signIndexes);
        itemMeta.setLore(itemLore);
        itemToDrop.setItemMeta(itemMeta);

        itemUtil.decreaseItemAmount(inventory.getItemInMainHand(), () -> inventory.setItemInMainHand(null));
        itemUtil.decreaseItemAmount(inventory.getItemInOffHand(), () -> inventory.setItemInOffHand(null));

        Location dropLocation = clickedBlock.getLocation().add(0.5, 1, 0.5);
        itemUtil.dropItem(dropLocation, itemToDrop);

        if (index != -1 && message.isDropDye()) {
            Component previousComponent = LegacyComponentSerializer.legacySection().deserialize(previousSign);
            TextColor textColor = previousComponent.color();
            if (textColor != null) {
                String itemStack = dyeUtil.hexToDye(textColor);
                if (itemStack != null) {
                    itemUtil.dropItem(dropLocation, new ItemStack(Material.valueOf(itemStack)));
                }
            }
        }

        Location location = clickedBlock.getLocation();
        soundPlayer.play(getSound(), fPlayer, new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
