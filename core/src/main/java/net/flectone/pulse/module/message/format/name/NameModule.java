package net.flectone.pulse.module.message.format.name;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class NameModule extends AbstractModuleMessage<Localization.Message.Format.Name> implements MessageProcessor {

    private final Message.Format.Name message;
    private final Permission.Message.Format.Name permission;
    private final Permission.Message.Format formatPermission;

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public NameModule(FileManager fileManager,
                      IntegrationModule integrationModule,
                      PermissionChecker permissionChecker,
                      MessagePipeline messagePipeline,
                      MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getName_());

        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;

        message = fileManager.getMessage().getFormat().getName_();
        permission = fileManager.getPermission().getMessage().getFormat().getName_();
        formatPermission = fileManager.getPermission().getMessage().getFormat();

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        if (messageContext.isPlayer()) {
            messageContext.addTagResolvers(playerTag(sender));
        }

        FEntity receiver = messageContext.getReceiver();
        messageContext.addTagResolvers(displayTag(sender, receiver));
        messageContext.addTagResolvers(vaultSuffixTag(sender, receiver));
        messageContext.addTagResolvers(vaultPrefixTag(sender, receiver));
    }

    private TagResolver vaultSuffixTag(FEntity sender, FEntity fReceiver) {
        String tag = "vault_suffix";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String suffix = integrationModule.getSuffix(fPlayer);
            if (suffix == null || suffix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, fReceiver, suffix)
                    .defaultSerializerBuild();


            return Tag.preProcessParsed(text);
        });
    }

    private TagResolver vaultPrefixTag(FEntity sender, FEntity fReceiver) {
        String tag = "vault_prefix";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String prefix = integrationModule.getPrefix(fPlayer);
            if (prefix == null || prefix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, fReceiver, prefix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });
    }

    private TagResolver playerTag(@NotNull FEntity player) {
        String tag = "player";
        if (checkModulePredicates(player)) return emptyTagResolver(tag);;

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.preProcessParsed(player.getName())
        );
    }

    private TagResolver displayTag(FEntity sender, FEntity fReceiver) {
        String tag = "display_name";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            if (sender instanceof FPlayer fPlayer) {
                if (fPlayer.isUnknown()) {
                    return Tag.preProcessParsed(resolveLocalization(fReceiver).getUnknown()
                            .replace("<name>", fPlayer.getName())
                    );
                }

                String displayName = resolveLocalization(fReceiver).getDisplay();
                Component name = messagePipeline.builder(sender, fReceiver, displayName)
                        .build();

                return Tag.selfClosingInserting(name);
            }

            return Tag.preProcessParsed(resolveLocalization(fReceiver).getEntity()
                    .replace("<name>", sender.getName())
                    .replace("<type>", sender.getType())
                    .replace("<uuid>", sender.getUuid().toString())
            );
        });
    }
}
