package net.flectone.pulse.module.message.bed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.bed.listener.BedPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

@Singleton
public class BedModule extends AbstractModuleMessage<Localization.Message.Bed> {

    private final Message.Bed message;
    private final Permission.Message.Bed permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BedModule(FileManager fileManager,
                     FPlayerManager fPlayerManager,
                     ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getBed());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getBed();
        permission = fileManager.getPermission().getMessage().getBed();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(BedPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String key) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        MessageType messageType = MessageType.fromString(key);

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(bed -> switch (messageType) {
                    case NO_SLEEP -> bed.getNoSleep();
                    case NOT_SAFE -> bed.getNotSafe();
                    case OBSTRUCTED -> bed.getObstructed();
                    case OCCUPIED -> bed.getOccupied();
                    case TOO_FAR_AWAY -> bed.getTooFarAway();
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }

    public enum MessageType {
        NO_SLEEP("block.minecraft.bed.no_sleep"),
        NOT_SAFE("block.minecraft.bed.not_safe"),
        OBSTRUCTED("block.minecraft.bed.obstructed"),
        OCCUPIED("block.minecraft.bed.occupied"),
        TOO_FAR_AWAY("block.minecraft.bed.too_far_away"),
        UNKNOWN("unknown");

        private final String key;

        MessageType(String key) {
            this.key = key;
        }

        public static MessageType fromString(String string) {
            return Arrays.stream(MessageType.values())
                    .filter(type -> type.key.equalsIgnoreCase(string))
                    .findAny()
                    .orElse(UNKNOWN);
        }

    }

}

