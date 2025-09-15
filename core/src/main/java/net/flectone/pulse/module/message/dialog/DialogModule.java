package net.flectone.pulse.module.message.dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.dialog.listener.DialogPulseListener;
import net.flectone.pulse.module.message.dialog.model.Dialog;
import net.flectone.pulse.module.message.dialog.model.DialogMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@Singleton
public class DialogModule extends AbstractModuleLocalization<Localization.Message.Dialog> {

    private final Message.Dialog message;
    private final Permission.Message.Dialog permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DialogModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDialog(), MessageType.DIALOG);

        this.message = fileResolver.getMessage().getDialog();
        this.permission = fileResolver.getPermission().getMessage().getDialog();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DialogPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Dialog dialog) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DialogMetadata.<Localization.Message.Dialog>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> switch (translationKey) {
                    case COMMANDS_DIALOG_CLEAR_MULTIPLE -> Strings.CS.replace(localization.getClear().getMultiple(), "<players>", StringUtils.defaultString(dialog.getPlayers()));
                    case COMMANDS_DIALOG_CLEAR_SINGLE -> localization.getClear().getSingle();
                    case COMMANDS_DIALOG_SHOW_MULTIPLE -> Strings.CS.replace(localization.getShow().getMultiple(), "<players>", StringUtils.defaultString(dialog.getPlayers()));
                    case COMMANDS_DIALOG_SHOW_SINGLE -> localization.getShow().getSingle();
                    default -> "";
                })
                .dialog(dialog)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, dialog.getTarget())})
                .build()
        );
    }

}