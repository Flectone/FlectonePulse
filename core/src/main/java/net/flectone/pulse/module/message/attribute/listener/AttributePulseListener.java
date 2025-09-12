package net.flectone.pulse.module.message.attribute.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.attribute.AttributeModule;
import net.flectone.pulse.module.message.attribute.extractor.AttributeExtractor;
import net.flectone.pulse.module.message.attribute.model.Attribute;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class AttributePulseListener implements PulseListener {

    private final AttributeModule attributeModule;
    private final AttributeExtractor attributeExtractor;

    @Inject
    public AttributePulseListener(AttributeModule attributeModule,
                                  AttributeExtractor attributeExtractor) {
        this.attributeModule = attributeModule;
        this.attributeExtractor = attributeExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_ATTRIBUTE_BASE_VALUE_GET_SUCCESS, COMMANDS_ATTRIBUTE_BASE_VALUE_RESET_SUCCESS,
                 COMMANDS_ATTRIBUTE_BASE_VALUE_SET_SUCCESS, COMMANDS_ATTRIBUTE_VALUE_GET_SUCCESS -> {
                Optional<Attribute> attribute = attributeExtractor.extractBaseValue(event.getTranslatableComponent());
                if (attribute.isEmpty()) return;

                event.setCancelled(true);
                attributeModule.send(event.getFPlayer(), translationKey, attribute.get());
            }
            case COMMANDS_ATTRIBUTE_MODIFIER_ADD_SUCCESS, COMMANDS_ATTRIBUTE_MODIFIER_REMOVE_SUCCESS,
                 COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS -> {
                Optional<Attribute> attribute = attributeExtractor.extractModifier(translationKey, event.getTranslatableComponent());
                if (attribute.isEmpty()) return;

                event.setCancelled(true);
                attributeModule.send(event.getFPlayer(), translationKey, attribute.get());
            }
        }
    }

}