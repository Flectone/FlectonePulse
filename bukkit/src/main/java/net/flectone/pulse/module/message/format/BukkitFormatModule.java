package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.formatter.MessageFormatter;
import net.flectone.pulse.util.TagType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class BukkitFormatModule extends FormatModule {

    private final MessageFormatter messageFormatter;

    @Inject
    public BukkitFormatModule(FileManager fileManager,
                              MessageFormatter messageFormatter) {
        super(fileManager);

        this.messageFormatter = messageFormatter;
    }

    @Override
    public TagResolver coordsTag(FEntity sender, FEntity fReceiver) {
        String tag = "coords";
        if (!isCorrectTag(TagType.COORDS, sender)) return emptyTagResolver(tag);

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return emptyTagResolver(tag);

        Location location = player.getLocation();

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.COORDS)
                    .replace("<x>", String.valueOf(location.getBlockX()))
                    .replace("<y>", String.valueOf(location.getBlockY()))
                    .replace("<z>", String.valueOf(location.getBlockZ()));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    @Override
    public TagResolver statsTag(FEntity sender, FEntity fReceiver) {
        String tag = "stats";
        if (!isCorrectTag(TagType.STATS, sender)) return emptyTagResolver(tag);

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return emptyTagResolver(tag);

        AttributeInstance armor;
        AttributeInstance damage;

        try {
            armor = player.getAttribute(Attribute.GENERIC_ARMOR);
            damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        } catch (ArrayIndexOutOfBoundsException e) {
            return emptyTagResolver(tag);
        }

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.STATS)
                    .replace("<hp>", String.valueOf(Math.round(player.getHealth() * 10.0)/10.0))
                    .replace("<armor>", String.valueOf(armor != null ? Math.round(armor.getValue() * 10.0)/10.0 : 0.0))
                    .replace("<exp>", player.getLevel() + ".0")
                    .replace("<food>", player.getFoodLevel() + ".0")
                    .replace("<attack>", String.valueOf(damage != null ? Math.round(damage.getValue() * 10.0)/10.0 : 0.0));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

}
