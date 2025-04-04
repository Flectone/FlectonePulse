package net.flectone.pulse.module.message.status.motd;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.formatter.MessageFormatter;

import java.util.List;

@Singleton
public class MOTDModule extends AbstractModuleListMessage<Localization.Message.Status.MOTD> {

    private final Message.Status.MOTD message;
    private final Permission.Message.Status.MOTD permission;

    private final MessageFormatter messageFormatter;

    @Inject
    public MOTDModule(FileManager fileManager,
                      MessageFormatter messageFormatter) {
        super(localization -> localization.getMessage().getStatus().getMotd());

        this.messageFormatter = messageFormatter;

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

        return messageFormatter.builder(fPlayer, message).serializeToTree();
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
