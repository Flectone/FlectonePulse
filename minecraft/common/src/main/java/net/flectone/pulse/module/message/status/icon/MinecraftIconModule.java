package net.flectone.pulse.module.message.status.icon;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.status.icon.listener.MinecraftPacketIconListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.processing.converter.IconConvertor;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.flectone.pulse.platform.formatter.MinecraftServerStatusFormatter;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftIconModule implements ModuleSimple {

    private final List<String> iconList = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final RandomGenerator randomUtil;
    private final IconConvertor iconUtil;
    private final MinecraftServerStatusFormatter statusUtil;
    private final @Named("imagePath") Path iconPath;

    private int index;

    @Override
    public void onEnable() {
        initIcons();

        listenerRegistry.register(MinecraftPacketIconListener.class);
    }

    @Override
    public void onDisable() {
        iconList.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_STATUS_ICON;
    }

    @Override
    public Message.Status.Icon config() {
        return fileFacade.message().status().icon();
    }

    @Override
    public Permission.Message.Status.Icon permission() {
        return fileFacade.permission().message().status().icon();
    }

    public void initIcons() {
        List<String> iconNames = config().values();
        if (iconNames.isEmpty()) return;

        iconNames.forEach(iconName -> {
            if (iconPath.resolve(iconName).toFile().exists()) return;

            platformServerAdapter.saveResource("images/" + iconName);
        });

        File folder = iconPath.toFile();
        if (!folder.isDirectory()) return;

        File[] icons = folder.listFiles();
        if (icons == null) return;

        iconNames.forEach(iconName -> {
            for (File icon : icons) {
                if (!icon.isFile()) continue;
                if (!icon.getName().equals(iconName)) continue;

                String convertedIcon = iconUtil.convert(icon);
                if (convertedIcon == null) continue;
                iconList.add(convertedIcon);
            }
        });
    }

    public void update(PacketSendEvent event) {
        User user = event.getUser();

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        event.markForReEncode(true);

        WrapperPlayServerServerData wrapperPlayServerServerData = new WrapperPlayServerServerData(event);
        wrapperPlayServerServerData.setIcon(statusUtil.formatIcon(next(fPlayer)));
    }

    public @Nullable String next(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;
        if (iconList.isEmpty()) return null;

        if (config().random()) {
            index = randomUtil.nextInt(0, iconList.size());
        } else {
            index++;
            index = index % iconList.size();
        }

        return iconList.get(index);
    }
}
