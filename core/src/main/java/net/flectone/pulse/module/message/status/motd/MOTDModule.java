package net.flectone.pulse.module.message.status.motd;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.util.ComponentUtil;

import java.util.List;

@Singleton
public class MOTDModule extends AbstractModuleListMessage<Localization.Message.Status.MOTD> {

    private final Message.Status.MOTD message;
    private final Permission.Message.Status.MOTD permission;

    private final ComponentUtil componentUtil;

    @Inject
    public MOTDModule(FileManager fileManager,
                      ComponentUtil componentUtil) {
        super(localization -> localization.getMessage().getStatus().getMotd());

        this.componentUtil = componentUtil;

        message = fileManager.getMessage().getStatus().getMotd();
        permission = fileManager.getPermission().getMessage().getStatus().getMotd();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }


    public JsonElement next(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        String message = getNextMessage(fPlayer, this.message.isRandom());
        if (message == null) return null;

        return componentUtil.builder(fPlayer, message).serializeToTree();
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return resolveLocalization(fPlayer).getValues();
    }
}
