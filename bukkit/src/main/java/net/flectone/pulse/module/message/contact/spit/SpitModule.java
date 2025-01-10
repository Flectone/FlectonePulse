package net.flectone.pulse.module.message.contact.spit;

import com.github.retrooper.packetevents.util.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.spit.listener.SpitListener;
import net.flectone.pulse.platform.SoundPlayer;
import net.flectone.pulse.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;


@Singleton
public class SpitModule extends AbstractModuleMessage<Localization.Message.Contact.Spit> {

    @Getter private final Message.Contact.Spit message;
    private final Permission.Message.Contact.Spit permission;

    @Getter private final String SPIT_NAME = "SPIT_NAME";

    private final BukkitListenerManager bukkitListenerManager;
    private final SoundPlayer soundPlayer;
    private final PermissionUtil permissionUtil;

    @Inject
    public SpitModule(FileManager fileManager,
                      BukkitListenerManager bukkitListenerManager,
                      PermissionUtil permissionUtil,
                      SoundPlayer soundPlayer) {
        super(localization -> localization.getMessage().getContact().getSpit());

        this.bukkitListenerManager = bukkitListenerManager;
        this.permissionUtil = permissionUtil;
        this.soundPlayer = soundPlayer;

        message = fileManager.getMessage().getContact().getSpit();
        permission = fileManager.getPermission().getMessage().getContact().getSpit();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        bukkitListenerManager.register(SpitListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(@NotNull FPlayer fPlayer, Location location) {
        if (checkModulePredicates(fPlayer)) return;

        spit(fPlayer);
    }

    @Sync
    public void spit(FPlayer fPlayer) {
        if (!isEnable()) return;
        if (!permissionUtil.has(fPlayer, getModulePermission())) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        Location location = player.getEyeLocation();
        location.setY(location.getY() - 0.3);

        LlamaSpit spit = (LlamaSpit) player.getWorld().spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.setVelocity(location.getDirection());
        spit.setShooter(player);
        spit.setCustomNameVisible(false);
        spit.setCustomName(SPIT_NAME);

        soundPlayer.play(getSound(), fPlayer, new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Async
    public void send(FPlayer fPlayer, FPlayer fReceiver) {
        if (!isEnable()) return;
        if (!permissionUtil.has(fPlayer, getModulePermission())) return;
        if (!message.isMessage()) return;
        if (!fReceiver.is(FPlayer.Setting.SPIT)) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .destination(message.getDestination())
                .format(Localization.Message.Contact.Spit::getFormat)
                .sound(null)
                .sendBuilt();
    }
}
