package net.flectone.pulse.module.message.format.name;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
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
    private final MessageProcessRegistry messageProcessRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public NameModule(FileResolver fileResolver,
                      IntegrationModule integrationModule,
                      PermissionChecker permissionChecker,
                      MessagePipeline messagePipeline,
                      MessageProcessRegistry messageProcessRegistry,
                      PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getFormat().getName_());

        this.message = fileResolver.getMessage().getFormat().getName_();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getName_();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(150, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        messageContext.addTagResolvers(constantTag(sender));

        FEntity receiver = messageContext.getReceiver();
        messageContext.addTagResolvers(displayTag(sender, receiver));
        messageContext.addTagResolvers(vaultSuffixTag(sender, receiver));
        messageContext.addTagResolvers(vaultPrefixTag(sender, receiver));

        if (messageContext.isPlayer()) {
            messageContext.addTagResolvers(playerTag(sender, receiver));
        }
    }

    private TagResolver vaultSuffixTag(FEntity sender, FEntity fReceiver) {
        String tag = "vault_suffix";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);
        if (isInvisible(sender)) return emptyTagResolver(tag);

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
        if (isInvisible(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String prefix = integrationModule.getPrefix(fPlayer);
            if (prefix == null || prefix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = messagePipeline.builder(fPlayer, fReceiver, prefix)
                    .defaultSerializerBuild();

            return Tag.preProcessParsed(text);
        });
    }

    private TagResolver constantTag(FEntity player) {
        String tag = "constant";
        if (checkModulePredicates(player)) return emptyTagResolver(tag);
        if (isInvisible(player)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String constantName = player.getConstantName();
            if (constantName != null && constantName.isEmpty()) {
                return Tag.preProcessParsed(constantName);
            }

            if (constantName == null) {
                constantName = resolveLocalization(player).getConstant();
            }

            if (constantName.isEmpty()) {
                return Tag.selfClosingInserting(Component.empty());
            }

            return Tag.preProcessParsed(messagePipeline.builder(player, constantName).defaultSerializerBuild());
        });
    }

    private TagResolver playerTag(@NotNull FEntity player, FEntity receiver) {
        String tag = "player";
        if (checkModulePredicates(player)) return emptyTagResolver(tag);
        if (isInvisible(player)) return invisibleTag(tag, player, receiver);

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.preProcessParsed(player.getName())
        );
    }

    private TagResolver displayTag(FEntity sender, FEntity fReceiver) {
        String tag = "display_name";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (isInvisible(sender)) return invisibleTag(tag, sender, fReceiver);

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

    private TagResolver invisibleTag(String tag, FEntity sender, FEntity fReceiver) {
        return TagResolver.resolver(tag, (argumentQueue, context) -> {

            String formatInvisible = resolveLocalization(fReceiver).getInvisible();
            Component name = messagePipeline.builder(sender, fReceiver, formatInvisible)
                    .build();

            return Tag.selfClosingInserting(name);
        });
    }

    private boolean isInvisible(FEntity entity) {
        return message.isShouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionTypes.INVISIBILITY);
    }
}
