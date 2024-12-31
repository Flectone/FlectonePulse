package net.flectone.pulse.module.message.brand;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.brand.ticker.BrandTicker;
import net.flectone.pulse.module.message.brand.util.BrandPacketSerializer;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
public class BrandModule extends AbstractModuleListMessage<Localization.Message.Brand> {

    private final String BRAND = "minecraft:brand";

    private final Message.Brand message;
    private final Permission.Message.Brand permission;

    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final BrandPacketSerializer brandPacketSerializer;

    @Inject
    private BrandTicker brandTicker;

    @Inject
    public BrandModule(FileManager fileManager,
                       ComponentUtil componentUtil,
                       PacketEventsUtil packetEventsUtil,
                       BrandPacketSerializer brandPacketSerializer) {
        super(localization -> localization.getMessage().getBrand());
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.brandPacketSerializer = brandPacketSerializer;

        message = fileManager.getMessage().getBrand();
        permission = fileManager.getPermission().getMessage().getBrand();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Config.Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            brandTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String message = nextMessage(fPlayer, this.message.isRandom(), resolveLocalization(fPlayer).getValues());
        if (message == null) return;

        Component component = componentUtil.builder(fPlayer, message).build();
        message = LegacyComponentSerializer.legacySection().serialize(component);

        byte[] data = brandPacketSerializer.serialize(message + "§r");
        if (data == null) return;

        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerPluginMessage(BRAND, data));
    }
}
