package net.flectone.pulse.module.message.attribute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
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

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AttributeModule(FileResolver fileResolver,
                           ListenerRegistry listenerRegistry) {
        super(MessageType.ATTRIBUTE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(AttributePulseListener.class);
    }

    @Override
    public Message.Attribute config() {
        return fileResolver.getMessage().getAttribute();
    }

    @Override
    public Permission.Message.Attribute permission() {
        return fileResolver.getPermission().getMessage().getAttribute();
    }

    @Override
    public Localization.Message.Attribute localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getAttribute();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Attribute attribute) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(AttributeMetadata.<Localization.Message.Attribute>builder()
                .sender(fPlayer)
                .range(config().getRange())
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
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, attribute.getTarget())})
                .build()
        );
    }
}