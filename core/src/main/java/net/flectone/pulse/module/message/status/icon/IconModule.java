package net.flectone.pulse.module.message.status.icon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.RandomUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class IconModule extends AbstractModule {

    private final List<String> iconList = new ArrayList<>();

    private final FileResolver fileResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final RandomUtil randomUtil;
    private final IconUtil iconUtil;
    private final Path iconPath;

    private int index;

    @Inject
    public IconModule(FileResolver fileResolver,
                      @Named("projectPath") Path projectPath,
                      PlatformServerAdapter platformServerAdapter,
                      RandomUtil randomUtil,
                      IconUtil iconUtil) {
        this.fileResolver = fileResolver;
        this.platformServerAdapter = platformServerAdapter;
        this.iconUtil = iconUtil;
        this.randomUtil = randomUtil;
        this.iconPath = projectPath.resolve("images");
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        initIcons();
    }

    @Override
    public void onDisable() {
        iconList.clear();
    }

    @Override
    public Message.Status.Icon config() {
        return fileResolver.getMessage().getStatus().getIcon();
    }

    @Override
    public Permission.Message.Status.Icon permission() {
        return fileResolver.getPermission().getMessage().getStatus().getIcon();
    }

    public void initIcons() {
        List<String> iconNames = config().getValues();
        if (iconNames.isEmpty()) return;

        iconNames.forEach(iconName -> {
            if (new File(iconPath.toString() + File.separator + iconName).exists()) return;

            platformServerAdapter.saveResource("images" + File.separator + iconName);
        });

        File folder = new File(iconPath.toString());
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

    @Nullable
    public String next(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;
        if (iconList.isEmpty()) return null;

        if (config().isRandom()) {
            index = randomUtil.nextInt(0, iconList.size());
        } else {
            index++;
            index = index % iconList.size();
        }

        return iconList.get(index);
    }
}
