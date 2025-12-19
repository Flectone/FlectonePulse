package net.flectone.pulse.module.message.greeting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.FImage;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.greeting.listener.GreetingPulseListener;
import net.flectone.pulse.module.message.greeting.model.GreetingMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GreetingModule extends AbstractModuleLocalization<Localization.Message.Greeting> {

    private final FileFacade fileFacade;
    private final SkinService skinService;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().sound(), permission().sound());

        listenerRegistry.register(GreetingPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.GREETING;
    }

    @Override
    public Message.Greeting config() {
        return fileFacade.message().greeting();
    }

    @Override
    public Permission.Message.Greeting permission() {
        return fileFacade.permission().message().greeting();
    }

    @Override
    public Localization.Message.Greeting localization(FEntity sender) {
        return fileFacade.localization(sender).message().greeting();
    }

    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        FImage fImage = new FImage(skinService.getAvatarUrl(fPlayer));

        try {
            List<String> pixels = fImage.convertImageUrl();

            sendMessage(GreetingMetadata.<Localization.Message.Greeting>builder()
                    .sender(fPlayer)
                    .format(s -> {
                        String greetingMessage = String.join("<br>", localization(fPlayer).format());

                        for (String pixel : pixels) {
                            greetingMessage = Strings.CS.replaceOnce(greetingMessage, "[#][#][#][#][#][#][#][#]", pixel);
                        }

                        return greetingMessage;
                    })
                    .pixels(pixels)
                    .destination(config().destination())
                    .sound(getModuleSound())
                    .build()
            );

        } catch (Exception e) {
            fLogger.warning(e);
        }
    }
}
