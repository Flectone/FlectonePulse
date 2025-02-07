package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

@Singleton
public class BukkitFormatModule extends FormatModule {

    private final PermissionUtil permissionUtil;
    private final ComponentUtil componentUtil;

    @Inject
    public BukkitFormatModule(FileManager fileManager,
                              ServerUtil serverUtil,
                              FPlayerManager fPlayerManager,
                              PermissionUtil permissionUtil,
                              ComponentUtil componentUtil,
                              ItemUtil itemUtil) {
        super(fileManager, serverUtil, fPlayerManager, permissionUtil, itemUtil);

        this.permissionUtil = permissionUtil;
        this.componentUtil = componentUtil;
    }

    @Override
    public TagResolver coordsTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.COORDS, sender)) return TagResolver.empty();

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return TagResolver.resolver("coords", ((argumentQueue, context) -> Tag.selfClosingInserting(Component.empty())));

        Location location = player.getLocation();

        return TagResolver.resolver("coords", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.COORDS)
                    .replace("<x>", String.valueOf(location.getBlockX()))
                    .replace("<y>", String.valueOf(location.getBlockY()))
                    .replace("<z>", String.valueOf(location.getBlockZ()));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    @Override
    public TagResolver statsTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.STATS, sender)) return TagResolver.empty();

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return TagResolver.resolver("stats", ((argumentQueue, context) -> Tag.selfClosingInserting(Component.empty())));

        AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
        AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

        return TagResolver.resolver("stats", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.STATS)
                    .replace("<hp>", String.valueOf(Math.round(player.getHealth() * 10.0)/10.0))
                    .replace("<armor>", String.valueOf(armor != null ? Math.round(armor.getValue() * 10.0)/10.0 : 0.0))
                    .replace("<exp>", player.getLevel() + ".0")
                    .replace("<food>", player.getFoodLevel() + ".0")
                    .replace("<attack>", String.valueOf(damage != null ? Math.round(damage.getValue() * 10.0)/10.0 : 0.0));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

}
