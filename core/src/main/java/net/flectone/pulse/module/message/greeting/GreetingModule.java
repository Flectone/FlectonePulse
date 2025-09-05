package net.flectone.pulse.module.message.greeting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.model.util.FImage;
import net.flectone.pulse.module.message.greeting.listener.GreetingPulseListener;
import net.flectone.pulse.module.message.greeting.model.GreetingMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Singleton
public class GreetingModule extends AbstractModuleLocalization<Localization.Message.Greeting> {

    private final Message.Greeting message;
    private final Permission.Message.Greeting permission;
    private final SkinService skinService;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GreetingModule(FileResolver fileResolver,
                          SkinService skinService,
                          FLogger fLogger,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getGreeting(), MessageType.GREETING);

        this.message = fileResolver.getMessage().getGreeting();
        this.permission = fileResolver.getPermission().getMessage().getGreeting();
        this.skinService = skinService;
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        listenerRegistry.register(GreetingPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        FImage fImage = new FImage(skinService.getAvatarUrl(fPlayer));

        try {
            List<String> pixels = fImage.convertImageUrl();

            sendMessage(GreetingMetadata.<Localization.Message.Greeting>builder()
                    .sender(fPlayer)
                    .format(s -> {
                        String greetingMessage = String.join("<br>", resolveLocalization(fPlayer).getFormat());

                        for (String pixel : pixels) {
                            greetingMessage = Strings.CS.replaceOnce(greetingMessage, "[#][#][#][#][#][#][#][#]", pixel);
                        }

                        return greetingMessage;
                    })
                    .pixels(pixels)
                    .destination(message.getDestination())
                    .sound(getModuleSound())
                    .build()
            );

        } catch (IOException | URISyntaxException e) {
            fLogger.warning(e);
        }
    }
}
