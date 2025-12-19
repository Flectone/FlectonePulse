package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPulseListener;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitModule extends AbstractModuleLocalization<Localization.Message.Quit> {

    private final FileFacade fileFacade;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().sound(), permission().sound());

        listenerRegistry.register(QuitPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.QUIT;
    }

    @Override
    public Message.Quit config() {
        return fileFacade.message().quit();
    }

    @Override
    public Permission.Message.Quit permission() {
        return fileFacade.permission().message().quit();
    }

    @Override
    public Localization.Message.Quit localization(FEntity sender) {
        return fileFacade.localization(sender).message().quit();
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(QuitMetadata.<Localization.Message.Quit>builder()
                .sender(fPlayer)
                .format(Localization.Message.Quit::format)
                .ignoreVanish(ignoreVanish)
                .destination(config().destination())
                .range(config().range())
                .sound(getModuleSound())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .integration()
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(ignoreVanish))
                .build()
        );
    }
}
