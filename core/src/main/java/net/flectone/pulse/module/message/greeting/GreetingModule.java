package net.flectone.pulse.module.message.greeting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.format.image.model.FImage;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.util.List;

@Singleton
public class GreetingModule extends AbstractModuleMessage<Localization.Message.Greeting> {

    private final Message.Greeting message;
    private final Permission.Message.Greeting permission;

    private final SkinService skinService;
    private final FLogger fLogger;

    @Inject
    public GreetingModule(FileManager fileManager,
                          SkinService skinService,
                          FLogger fLogger) {
        super(localization -> localization.getMessage().getGreeting());
        this.skinService = skinService;
        this.fLogger = fLogger;

        message = fileManager.getMessage().getGreeting();
        permission = fileManager.getPermission().getMessage().getGreeting();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        FImage fImage = new FImage(skinService.getAvatarUrl(fPlayer));

        try {
            List<String> pixels = fImage.convertImageUrl();

            builder(fPlayer)
                    .destination(message.getDestination())
                    .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.GREETING))
                    .format(s -> {
                        String greetingMessage = String.join("<br>", resolveLocalization(fPlayer).getFormat());

                        for (String pixel : pixels) {
                            greetingMessage = greetingMessage.replaceFirst("\\[#]\\[#]\\[#]\\[#]\\[#]\\[#]\\[#]\\[#]", pixel);
                        }

                        return greetingMessage;
                    })
                    .sound(getSound())
                    .sendBuilt();

        } catch (IOException e) {
            fLogger.warning(e);
        }
    }
}
