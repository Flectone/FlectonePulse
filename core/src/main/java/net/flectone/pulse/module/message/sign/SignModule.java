package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public abstract class SignModule extends AbstractModule {

    private final Message.Sign message;
    private final Permission.Message.Sign permission;

    private final ComponentUtil componentUtil;

    @Inject
    public SignModule(FileManager fileManager,
                      ComponentUtil componentUtil) {
        this.componentUtil = componentUtil;

        message = fileManager.getMessage().getSign();
        permission = fileManager.getPermission().getMessage().getSign();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String format(FPlayer fPlayer, String string) {
        if (checkModulePredicates(fPlayer)) return null;

        try {
            Component component = componentUtil.builder(fPlayer, string)
                    .userMessage(true)
                    .build()
                    .mergeStyle(LegacyComponentSerializer.legacySection().deserialize(string));

            return LegacyComponentSerializer.legacySection().serialize(component);

        } catch (ParsingException ignored) {}

        return string;
    }
}
