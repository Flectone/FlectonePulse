package net.flectone.pulse.module.message.greeting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.greeting.listener.PulseGreetingListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.converter.ImagePixelConverter;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GreetingModuleImpl implements GreetingModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final ListenerRegistry listenerRegistry;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final TaskScheduler taskScheduler;
    private final SkinService skinService;
    private final ImagePixelConverter imagePixelConverter;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseGreetingListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_GREETING;
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
    public Localization.Message.Greeting localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().greeting();
    }

    @Override
    public void send(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        taskScheduler.runAsyncLater(() -> {
            List<String> pixels = imagePixelConverter.convert(skinService.getAvatarUrl(fPlayer));

            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .messageContext(fResolver -> {
                        String format = localization(fResolver).format();

                        if (format.contains("[#][#][#][#][#][#][#][#]")) {
                            for (String pixel : pixels) {
                                format = Strings.CS.replaceOnce(format, "[#][#][#][#][#][#][#][#]", pixel);
                            }
                        }

                        return MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(format)
                                .build();
                    })
                    .build()
            );
        }, 2L); // delay so that message is sent later than join message
    }
}
