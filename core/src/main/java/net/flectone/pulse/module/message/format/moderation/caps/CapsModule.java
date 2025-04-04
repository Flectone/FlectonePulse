package net.flectone.pulse.module.message.format.moderation.caps;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;

@Singleton
public class CapsModule extends AbstractModule {

    private final Message.Format.Moderation.Caps message;
    private final Permission.Message.Format.Moderation.Caps permission;

    private final PermissionChecker permissionChecker;

    @Inject
    public CapsModule(FileManager fileManager,
                      PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;

        message = fileManager.getMessage().getFormat().getModeration().getCaps();
        permission = fileManager.getPermission().getMessage().getFormat().getModeration().getCaps();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (permissionChecker.check(sender, permission.getBypass())) return message;
        if (message == null || message.isEmpty()) return message;

        return needApplyAntiCaps(message) ? message.toLowerCase() : message;
    }

    private boolean needApplyAntiCaps(String message) {
        int uppercaseCount = 0;
        int totalLetters = 0;

        for (char symbol : message.toCharArray()) {
            if (!Character.isLetter(symbol)) continue;

            totalLetters++;

            if (!Character.isUpperCase(symbol)) continue;

            uppercaseCount++;
        }

        return totalLetters > 0 && ((double) uppercaseCount / totalLetters) > this.message.getTrigger();
    }
}
