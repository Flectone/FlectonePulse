package net.flectone.pulse.module.message.ride;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.ride.listener.RidePulseListener;
import net.flectone.pulse.module.message.ride.model.Ride;
import net.flectone.pulse.module.message.ride.model.RideMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class RideModule extends AbstractModuleLocalization<Localization.Message.Ride> {

    private final Message.Ride message;
    private final Permission.Message.Ride permission;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Inject
    public RideModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry,
                      MessagePipeline messagePipeline) {
        super(localization -> localization.getMessage().getRide(), MessageType.RIDE);

        this.message = fileResolver.getMessage().getRide();
        this.permission = fileResolver.getPermission().getMessage().getRide();
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
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
                .sender(ride.target())
                .filterPlayer(fPlayer)
                .format(string -> translationKey == MinecraftTranslationKey.COMMANDS_RIDE_DISMOUNT_SUCCESS
                        ? string.getDismount()
                        : string.getMount()
                )
                .ride(ride)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fReceiver -> new TagResolver[]{destinationTag(fReceiver, ride.destination())})
                .build()
        );
    }

    public TagResolver destinationTag(FPlayer receiver, FEntity destination) {
        String tag = "destination";
        if (!isEnable()) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Component component = messagePipeline.builder(destination, receiver, "<display_name>").build();

            return Tag.selfClosingInserting(component);
        });
    }

}