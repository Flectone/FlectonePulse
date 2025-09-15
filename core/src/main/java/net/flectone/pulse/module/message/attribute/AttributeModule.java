package net.flectone.pulse.module.message.attribute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.attribute.listener.AttributePulseListener;
import net.flectone.pulse.module.message.attribute.model.Attribute;
import net.flectone.pulse.module.message.attribute.model.AttributeMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class AttributeModule extends AbstractModuleLocalization<Localization.Message.Attribute> {

    private final Message.Attribute message;
    private final Permission.Message.Attribute permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AttributeModule(FileResolver fileResolver,
                           ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getAttribute(), MessageType.ATTRIBUTE);

        this.message = fileResolver.getMessage().getAttribute();
        this.permission = fileResolver.getPermission().getMessage().getAttribute();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(AttributePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Attribute attribute) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(AttributeMetadata.<Localization.Message.Attribute>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_ATTRIBUTE_BASE_VALUE_GET_SUCCESS -> localization.getBaseValue().getGet();
                            case COMMANDS_ATTRIBUTE_BASE_VALUE_RESET_SUCCESS -> localization.getBaseValue().getReset();
                            case COMMANDS_ATTRIBUTE_BASE_VALUE_SET_SUCCESS -> localization.getBaseValue().getSet();
                            case COMMANDS_ATTRIBUTE_MODIFIER_ADD_SUCCESS -> localization.getModifier().getAdd();
                            case COMMANDS_ATTRIBUTE_MODIFIER_REMOVE_SUCCESS -> localization.getModifier().getRemove();
                            case COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS -> localization.getModifier().getValueGet();
                            case COMMANDS_ATTRIBUTE_VALUE_GET_SUCCESS -> localization.getValueGet();
                            default -> "";
                        },
                        new String[]{"<modifier>", "<attribute>", "<value>"},
                        new String[]{StringUtils.defaultString(attribute.getModifier()), attribute.getName(), StringUtils.defaultString(attribute.getValue())}
                ))
                .attribute(attribute)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, attribute.getTarget())})
                .build()
        );
    }

}