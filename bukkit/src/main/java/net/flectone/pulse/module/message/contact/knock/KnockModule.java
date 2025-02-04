package net.flectone.pulse.module.message.contact.knock;

import com.github.retrooper.packetevents.util.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.knock.listener.KnockListener;
import net.flectone.pulse.platform.SoundPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class KnockModule extends AbstractModuleMessage<Localization.Message.Contact> {

    private final Message.Contact.Knock message;
    private final Permission.Message.Contact.Knock permission;

    private final Map<String, Sound> BLOCK_SOUND = new HashMap<>();

    private final BukkitListenerManager bukkitListenerManager;
    private final SoundPlayer soundPlayer;

    @Inject
    public KnockModule(FileManager fileManager,
                       BukkitListenerManager bukkitListenerManager,
                       SoundPlayer soundPlayer) {
        super(localization -> localization.getMessage().getContact());
        this.bukkitListenerManager = bukkitListenerManager;
        this.soundPlayer = soundPlayer;

        message = fileManager.getMessage().getContact().getKnock();
        permission = fileManager.getPermission().getMessage().getContact().getKnock();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        BLOCK_SOUND.clear();
        message.getVariants().forEach(value -> {
            Permission.PermissionEntry soundPermission = permission.getTypes().get(value.getType());

            BLOCK_SOUND.put(value.getType(), createSound(value.getSound(), soundPermission));
        });

        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        bukkitListenerManager.register(KnockListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void knock(@NotNull FPlayer fPlayer, @NotNull Location location, @NotNull Block clickedBlock) {
        if (checkModulePredicates(fPlayer)) return;

        String blockType = clickedBlock.getType().toString().toUpperCase();
        Optional<Map.Entry<String, Sound>> blockEntry = BLOCK_SOUND.entrySet()
                .stream()
                .filter(entry -> blockType.contains(entry.getKey()))
                .findAny();

        if (blockEntry.isEmpty()) return;

        soundPlayer.play(blockEntry.get().getValue(), fPlayer, new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
