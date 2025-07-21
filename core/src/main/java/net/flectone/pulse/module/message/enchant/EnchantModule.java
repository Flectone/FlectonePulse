package net.flectone.pulse.module.message.enchant;

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
public class EnchantModule extends AbstractModuleMessage<Localization.Message.Enchant> {

    private final Message.Enchant message;
    private final Permission.Message.Enchant permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public EnchantModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getEnchant());

        this.message = fileResolver.getMessage().getEnchant();
        this.permission = fileResolver.getPermission().getMessage().getEnchant();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            if (!event.getKey().startsWith("commands.enchant.success")) return;
            if (event.getKey() == MinecraftTranslationKeys.COMMANDS_ENCHANT_SUCCESS) {
                event.cancel();
                send(event, "", "", "");
                return;
            }

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().size() < 2) return;
            if (!(translatableComponent.args().get(0) instanceof TranslatableComponent enchantComponent)) return;

            String enchantKey = enchantComponent.key();

            if (enchantComponent.children().size() < 2) return;
            if (!(enchantComponent.children().get(1) instanceof TranslatableComponent levelComponent)) return;

            String levelKey = levelComponent.key();

            if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return;
            String value = targetComponent.content();

            event.cancel();
            send(event, enchantKey, levelKey, value);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(TranslatableMessageEvent event, String enchant, String level, String value) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = event.getKey() == MinecraftTranslationKeys.COMMANDS_ENCHANT_SUCCESS_SINGLE
                || event.getKey() == MinecraftTranslationKeys.COMMANDS_ENCHANT_SUCCESS;

        if (isSingle && !value.isEmpty()) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSingle ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<enchant>", enchant)
                        .replace("<level>", level)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
