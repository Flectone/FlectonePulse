package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SleepModule extends AbstractModuleMessage<Localization.Message.Sleep> {

    private final Message.Sleep message;
    private final Permission.Message.Sleep permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public SleepModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getSleep());

        this.message = fileResolver.getMessage().getSleep();
        this.permission = fileResolver.getPermission().getMessage().getSleep();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            if (!event.getKey().startsWith("sleep.")) return;

            String sleepCount = "";
            String allCount = "";

            TranslatableComponent translatableComponent = event.getComponent();
            if (event.getKey() == MinecraftTranslationKeys.SLEEP_PLAYERS_SLEEPING && translatableComponent.args().size() == 2) {
                if ((translatableComponent.args().get(0) instanceof TextComponent sleepComponent)) {
                    sleepCount = sleepComponent.content();
                }
                if ((translatableComponent.args().get(1) instanceof TextComponent allComponent)) {
                    allCount = allComponent.content();
                }
            }

            event.cancel();
            send(event, sleepCount, allCount);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(TranslatableMessageEvent event, String sleepCount, String allCount) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(bed -> switch (event.getKey()) {
                    case SLEEP_NOT_POSSIBLE -> bed.getNotPossible();
                    case SLEEP_PLAYERS_SLEEPING -> bed.getPlayersSleeping()
                            .replace("<sleep_count>", sleepCount)
                            .replace("<all_count>", allCount);
                    case SLEEP_SKIPPING_NIGHT -> bed.getSkippingNight();
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }
}
