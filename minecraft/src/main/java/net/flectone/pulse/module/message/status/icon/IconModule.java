package net.flectone.pulse.module.message.status.icon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IconModule extends AbstractModule {

    private final List<String> iconList = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final PlatformServerAdapter platformServerAdapter;
    private final RandomUtil randomUtil;
    private final IconUtil iconUtil;
    private final @Named("imagePath") Path iconPath;

    private int index;

    @Override
    public void onEnable() {
        super.onEnable();

        initIcons();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        iconList.clear();
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

                String convertedIcon = iconUtil.convertIcon(icon);
                if (convertedIcon == null) continue;
                iconList.add(convertedIcon);
            }
        });
    }

    public @Nullable String next(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;
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
