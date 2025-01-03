package net.flectone.pulse.module.message.greeting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.format.image.model.FImage;

import java.io.IOException;
import java.util.List;

@Singleton
public class GreetingModule extends AbstractModuleMessage<Localization.Message.Greeting> {

    private final Message.Greeting message;
    private final Permission.Message.Greeting permission;

    private final FPlayerManager fPlayerManager;
    private final FLogger fLogger;

    @Inject
    public GreetingModule(FileManager fileManager,
                          FPlayerManager fPlayerManager,
                          FLogger fLogger) {
        super(localization -> localization.getMessage().getGreeting());
        this.fPlayerManager = fPlayerManager;
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
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        FImage fImage = new FImage(fPlayerManager.getAvatarURL(fPlayer));

        try {
            List<String> pixels = fImage.convertImageUrl();

            builder(fPlayer)
                    .destination(message.getDestination())
                    .filter(fReceiver -> fReceiver.is(FPlayer.Setting.GREETING))
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
