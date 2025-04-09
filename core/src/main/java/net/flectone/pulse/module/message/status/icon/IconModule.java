package net.flectone.pulse.module.message.status.icon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.FileUtil;
import net.flectone.pulse.util.RandomUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class IconModule extends AbstractModule {

    private final Message.Status.Icon message;
    private final Permission.Message.Status.Icon permission;

    private final List<String> iconList = new ArrayList<>();

    private final RandomUtil randomUtil;
    private final FileUtil fileUtil;
    private final Path iconPath;

    private int index;

    @Inject
    public IconModule(FileManager fileManager,
                      @Named("projectPath") Path projectPath,
                      RandomUtil randomUtil,
                      FileUtil fileUtil) {
        this.iconPath = projectPath.resolve("images");
        this.fileUtil = fileUtil;
        this.randomUtil = randomUtil;

        message = fileManager.getMessage().getStatus().getIcon();
        permission = fileManager.getPermission().getMessage().getStatus().getIcon();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        initIcons();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void initIcons() {
        iconList.clear();

        List<String> iconNames = message.getValues();
        if (iconNames.isEmpty()) return;

        iconNames.forEach(iconName -> {
            if (new File(iconPath.toString() + File.separator + iconName).exists()) return;

            fileUtil.saveResource("images" + File.separator + iconName);
        });

        File folder = new File(iconPath.toString());
        if (!folder.isDirectory()) return;

        File[] icons = folder.listFiles();
        if (icons == null) return;

        iconNames.forEach(iconName -> {
            for (File icon : icons) {
                if (!icon.isFile()) continue;
                if (!icon.getName().equals(iconName)) continue;

                String convertedIcon = fileUtil.convertIcon(icon);
                if (convertedIcon == null) continue;
                iconList.add(convertedIcon);
            }
        });
    }

    @Nullable
    public String next(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;
        if (iconList.isEmpty()) return null;

        if (message.isRandom()) {
            index = randomUtil.nextInt(0, iconList.size());
        } else {
            index++;
            index = index % iconList.size();
        }

        return iconList.get(index);
    }
}
