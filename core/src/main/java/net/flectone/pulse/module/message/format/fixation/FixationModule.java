package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import org.jetbrains.annotations.Nullable;

@Singleton
public class FixationModule extends AbstractModule {

    private final Message.Format.Fixation message;
    private final Permission.Message.Format.Fixation permission;

    @Inject
    public FixationModule(FileManager fileManager) {
        message = fileManager.getMessage().getFormat().getFixation();
        permission = fileManager.getPermission().getMessage().getFormat().getFixation();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (message.isBlank()) return message;

        if (this.message.isEndDot()) {
            if (this.message.getNonDotSymbols().stream().noneMatch(message::endsWith)) {
                message = message + ".";
            }
        }

        if (this.message.isFirstLetterUppercase()) {
            message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        }

        return message;
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
