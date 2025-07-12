package net.flectone.pulse.module.message.status.motd;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.pipeline.MessagePipeline;

import java.util.List;

@Singleton
public class MOTDModule extends AbstractModuleListMessage<Localization.Message.Status.MOTD> {

    private final Message.Status.MOTD message;
    private final Permission.Message.Status.MOTD permission;
    private final MessagePipeline messagePipeline;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MessagePipeline messagePipeline) {
        super(localization -> localization.getMessage().getStatus().getMotd());

        this.message = fileResolver.getMessage().getStatus().getMotd();
        this.permission = fileResolver.getPermission().getMessage().getStatus().getMotd();
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }


    public JsonElement next(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        String nextMessage = getNextMessage(fPlayer, this.message.isRandom());
        if (nextMessage == null) return null;

        return messagePipeline.builder(fPlayer, nextMessage).jsonSerializerBuild();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return resolveLocalization(fPlayer).getValues();
    }
}
