package net.flectone.pulse.module.message.enchant;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.enchant.listener.EnchantPulseListener;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.module.message.enchant.model.EnchantMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class EnchantModule extends AbstractModuleLocalization<Localization.Message.Enchant> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EnchantModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(MessageType.ENCHANT);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(EnchantPulseListener.class);
    }

    @Override
    public Message.Enchant config() {
        return fileResolver.getMessage().getEnchant();
    }

    @Override
    public Permission.Message.Enchant permission() {
        return fileResolver.getPermission().getMessage().getEnchant();
    }

    @Override
    public Localization.Message.Enchant localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getEnchant();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Enchant enchant) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(EnchantMetadata.<Localization.Message.Enchant>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> switch (translationKey) {
                    case COMMANDS_ENCHANT_SUCCESS_SINGLE, COMMANDS_ENCHANT_SUCCESS -> localization.getSingle();
                    case COMMANDS_ENCHANT_SUCCESS_MULTIPLE -> Strings.CS.replace(localization.getMultiple(), "<players>", StringUtils.defaultString(enchant.getPlayers()));
                    default -> "";
                })
                .enchant(enchant)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{enchantmentRag(enchant.getName()), targetTag(fResolver, enchant.getTarget())})
                .build()
        );
    }

    public TagResolver enchantmentRag(Component name) {
        String tag = "enchantment";
        if (!isEnable() || name == null) return empty(tag);

        Component enchant = NamedTextColor.GRAY.equals(name.color()) ? name.color(null) : name;

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.selfClosingInserting(enchant)
        );
    }
}
