package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPacketListener;
import net.flectone.pulse.util.MessageTag;

@Singleton
public class QuitModule extends AbstractModuleMessage<Localization.Message.Quit> {

    private final Message.Quit message;
    private final Permission.Message.Quit permission;

    private final ListenerManager listenerManager;

    @Inject
    public QuitModule(FileManager fileManager,
                      ListenerManager listenerManager,
                      IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getQuit());

        this.listenerManager = listenerManager;

        message = fileManager.getMessage().getQuit();
        permission = fileManager.getPermission().getMessage().getQuit();

        addPredicate(integrationModule::isVanished);
    }

    @Override
    public void reload() {

        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(QuitPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .tag(MessageTag.QUIT)
                .range(message.getRange())
                .filter(fReceiver -> fReceiver.is(FPlayer.Setting.QUIT))
                .format(Localization.Message.Quit::getFormat)
                .integration()
                .proxy()
                .sound(getSound())
                .sendBuilt();
    }
}
