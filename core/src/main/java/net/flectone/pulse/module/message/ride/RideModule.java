package net.flectone.pulse.module.message.ride;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.ride.listener.RidePulseListener;
import net.flectone.pulse.module.message.ride.model.Ride;
import net.flectone.pulse.module.message.ride.model.RideMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class RideModule extends AbstractModuleLocalization<Localization.Message.Ride> {

    private final Message.Ride message;
    private final Permission.Message.Ride permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public RideModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getRide(), MessageType.RIDE);

        this.message = fileResolver.getMessage().getRide();
        this.permission = fileResolver.getPermission().getMessage().getRide();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(RidePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Ride ride) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(RideMetadata.<Localization.Message.Ride>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> translationKey == MinecraftTranslationKey.COMMANDS_RIDE_DISMOUNT_SUCCESS
                        ? localization.getDismount()
                        : localization.getMount()
                )
                .ride(ride)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{
                        targetTag(fResolver, ride.target()),
                        targetTag("second_target", fResolver, ride.secondTarget())
                })
                .build()
        );
    }

}