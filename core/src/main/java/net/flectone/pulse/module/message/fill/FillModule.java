package net.flectone.pulse.module.message.fill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.fill.listener.FillPulseListener;
import net.flectone.pulse.module.message.fill.model.FillMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;

@Singleton
public class FillModule extends AbstractModuleLocalization<Localization.Message.Fill> {

    private final Message.Fill message;
    private final Permission.Message.Fill permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FillModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFill(), MessageType.FILL);

        this.message = fileResolver.getMessage().getFill();
        this.permission = fileResolver.getPermission().getMessage().getFill();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(FillPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String amount) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(FillMetadata.<Localization.Message.Fill>builder()
                .sender(fPlayer)
                .format(s -> Strings.CS.replace(
                        s.getFormat(),
                        "<amount>",
                        amount
                ))
                .amount(amount)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
