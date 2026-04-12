package net.flectone.pulse.module.message.format.names;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.names.listener.NamesPulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamesModule implements ModuleLocalization<Localization.Message.Format.Names> {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(NamesPulseListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_NAMES;
    }

    @Override
    public Message.Format.Names config() {
        return fileFacade.message().format().names();
    }

    @Override
    public Permission.Message.Format.Names permission() {
        return fileFacade.permission().message().format().names();
    }

    @Override
    public Localization.Message.Format.Names localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().names();
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer fReceiver = messageContext.receiver();

        if (!(sender instanceof FPlayer fPlayer)) {
            return messageContext.addTagResolver(MessagePipeline.ReplacementTag.DISPLAY_NAME, (_, _) -> {
                Localization.Message.Format.Names localizationName = localization(fReceiver);

                Component showEntityName = sender.showEntityName();
                if (showEntityName == null) {
                    MessageContext displayContext = messagePipeline.createContext(sender, fReceiver,
                                    StringUtils.replaceEach(
                                            sender.type().equals(FEntity.UNKNOWN_TYPE) ? localizationName.unknown() : localizationName.entity(),
                                            new String[]{"<name>", "<type>", "<uuid>"},
                                            new String[]{"<lang:'" + sender.type() + "'>", sender.type(), sender.uuid().toString()}
                                    )
                            )
                            .withFlags(messageContext.flags())
                            .addFlags(
                                    new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE},
                                    new boolean[]{false, false}
                            );

                    Component displayName = messagePipeline.build(displayContext);

                    return Tag.selfClosingInserting(displayName);
                }

                MessageContext displayContext = messagePipeline.createContext(sender, fReceiver,
                                sender.type().equals(FEntity.UNKNOWN_TYPE)
                                        ? localizationName.unknown()
                                        : StringUtils.replaceEach(
                                        localizationName.entity(),
                                        new String[]{"<type>", "<uuid>"},
                                        new String[]{sender.type(), sender.uuid().toString()}
                                )
                        )
                        .addTagResolver(TagResolver.resolver("name", (_, _) -> Tag.selfClosingInserting(showEntityName)))
                        .withFlags(messageContext.flags())
                        .addFlags(
                                new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE},
                                new boolean[]{false, false}
                        );

                Component displayName = messagePipeline.build(displayContext);
                return Tag.selfClosingInserting(displayName);
            });
        }

        // Nickname module can be disabled in config, but its placeholder is used, so we need to add it
        Set<String> playerNameTags = !messageContext.tagResolver().has(MessagePipeline.ReplacementTag.NICKNAME.getTagName()) && messageContext.isFlag(MessageFlag.NICKNAME_MODULE)
                ? Set.of(MessagePipeline.ReplacementTag.PLAYER.getTagName(), MessagePipeline.ReplacementTag.NICKNAME.getTagName())
                : Set.of(MessagePipeline.ReplacementTag.PLAYER.getTagName());

        return messageContext
                .addTagResolvers(
                        TagResolver.resolver(MessagePipeline.ReplacementTag.CONSTANT.getTagName(), (argumentQueue, _) -> {
                            List<Component> constants = fPlayer.constants();
                            if (constants.isEmpty()) {
                                List<String> stringConstants = localization(fPlayer).constant();
                                if (stringConstants.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

                                constants = stringConstants.stream()
                                        .map(string -> messagePipeline.build(messagePipeline.createContext(fPlayer, string)))
                                        .toList();
                            }

                            int constantIndex = 0;
                            if (argumentQueue.hasNext()) {
                                constantIndex = argumentQueue.pop().asInt().orElse(0);
                                if (constantIndex >= constants.size()) {
                                    constantIndex = 0;
                                }
                            }

                            return Tag.inserting(constants.get(constantIndex));
                        }),
                        TagResolver.resolver(MessagePipeline.ReplacementTag.DISPLAY_NAME.getTagName(), (argumentQueue, _) -> {
                            int displayNameIndex = 0;
                            if (argumentQueue.hasNext()) {
                                displayNameIndex = argumentQueue.pop().asInt().orElse(0);
                                if (displayNameIndex > localization().display().size()) {
                                    displayNameIndex = 0;
                                }
                            }

                            Localization.Message.Format.Names localization = localization(fReceiver);
                            String displayName = fPlayer.isUnknown() || localization.display().isEmpty()
                                    ? Strings.CS.replace(localization.unknown(), "<name>", fPlayer.name())
                                    : localization.display().get(displayNameIndex);

                            MessageContext displayContext = messagePipeline.createContext(sender, fReceiver, displayName)
                                    .withFlags(messageContext.flags())
                                    .addFlags(
                                            new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE},
                                            new boolean[]{false, false}
                                    );

                            Component displayNameComponent = messagePipeline.build(displayContext);

                            return Tag.selfClosingInserting(displayNameComponent);
                        }),
                        TagResolver.resolver(Set.of(MessagePipeline.ReplacementTag.PREFIX.getTagName(), "vault_prefix"), (_, _) -> {
                            String prefix = integrationModule.getPrefix(fPlayer);
                            return buildVaultTag(fPlayer, fReceiver, prefix, messageContext);
                        }),
                        TagResolver.resolver(Set.of(MessagePipeline.ReplacementTag.SUFFIX.getTagName(), "vault_suffix"), (_, _) -> {
                            String suffix = integrationModule.getSuffix(fPlayer);
                            return buildVaultTag(fPlayer, fReceiver, suffix, messageContext);
                        }),
                        TagResolver.resolver(playerNameTags, (_, _) ->
                                Tag.preProcessParsed(fPlayer.name())
                        )
                );
    }

    public MessageContext addInvisibleTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        return messageContext.addTagResolver(Set.of(MessagePipeline.ReplacementTag.DISPLAY_NAME, MessagePipeline.ReplacementTag.PLAYER),
                (_, _) -> {
                    String formatInvisible = localization(receiver).invisible();
                    MessageContext invisibleContext = messagePipeline.createContext(sender, receiver, formatInvisible);
                    Component name = messagePipeline.build(invisibleContext);

                    return Tag.selfClosingInserting(name);
                }
        );
    }

    public boolean isInvisible(FEntity entity) {
        return config().shouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionUtil.INVISIBILITY_POTION_NAME);
    }

    private Tag buildVaultTag(FPlayer fPlayer, FPlayer fReceiver, String vaultTag, MessageContext messageContext) {
        if (StringUtils.isEmpty(vaultTag)) return MessagePipeline.ReplacementTag.emptyTag();

        MessageContext tagContext = messagePipeline.createContext(fPlayer, fReceiver, vaultTag)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE},
                        new boolean[]{false, false}
                );

        // <texture> and <player_head> can't be deserialized, so it returns a Component, instead of a string
        // because of this, color won't be able to be applied to next word,
        // but it's better that way than displaying the tags incorrectly
        if (vaultTag.contains("<texture") || vaultTag.contains("<player_head")) {
            return Tag.selfClosingInserting(messagePipeline.build(tagContext));
        }

        return Tag.preProcessParsed(messagePipeline.buildDefault(tagContext));
    }

}