package net.flectone.pulse.module.message.give;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.give.listener.GivePulseListener;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.module.message.give.model.GiveMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class GiveModule extends AbstractModuleLocalization<Localization.Message.Give> {

    private final Message.Give message;
    private final Permission.Message.Give permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GiveModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getGive(), MessageType.GIVE);

        this.message = fileResolver.getMessage().getGive();
        this.permission = fileResolver.getPermission().getMessage().getGive();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(GivePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Give give) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(GiveMetadata.<Localization.Message.Give>builder()
                .sender(fPlayer)
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_GIVE_SUCCESS_MULTIPLE ? localization.getMultiple() : localization.getSingle(),
                        new String[]{"<items>", "<players>"},
                        new String[]{give.getItems(), StringUtils.defaultString(give.getPlayers())}
                ))
                .give(give)
                .translationKey(translationKey)
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{giveItemTag(give.getItem()), targetTag(fResolver, give.getTarget())})
                .build()
        );
    }

    public TagResolver giveItemTag(Component itemName) {
        String tag = "give_item";
        if (!isEnable() || itemName == null) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.selfClosingInserting(itemName)
        );
    }

}