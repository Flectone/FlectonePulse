package net.flectone.pulse.module.message.format.mention;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.listener.MentionPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class MentionModule extends AbstractModuleLocalization<Localization.Message.Format.Mention> {

    private final WeakHashMap<UUID, Boolean> processedMentions = new WeakHashMap<>();

    private final Cache<String, String> messageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Inject
    public MentionModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry,
                         FPlayerService fPlayerService,
                         IntegrationModule integrationModule,
                         PermissionChecker permissionChecker,
                         MessagePipeline messagePipeline,
                         FLogger fLogger) {
        super(localization -> localization.getMessage().getFormat().getMention());

        this.message = fileResolver.getMessage().getFormat().getMention();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getMention();
        this.listenerRegistry = listenerRegistry;
        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        registerPermission(permission.getGroup());
        registerPermission(permission.getBypass());

        listenerRegistry.register(MentionPulseListener.class);
    }

    @Override
    public void onDisable() {
        processedMentions.clear();
        messageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void format(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        String formattedMessage;
        try {
             formattedMessage = messageCache.get(contextMessage, () -> replace(sender, contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = replace(sender, contextMessage);
        }

        messageContext.setMessage(formattedMessage);
    }

    public void addTags(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        UUID processId = messageContext.getMessageUUID();
        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.MENTION, (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(message.getTrigger() + mention);
            }

            Optional<String> group = integrationModule.getGroups().stream()
                    .filter(name -> name.equalsIgnoreCase(mention))
                    .findFirst();

            if (group.isPresent()) {
                if (receiver instanceof FPlayer mentionFPlayer
                        && !permissionChecker.check(mentionFPlayer, permission.getBypass())
                        && permissionChecker.check(mentionFPlayer, permission.getGroup() + "." + group.get())) {
                    sendMention(processId, mentionFPlayer);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (mentionFPlayer.equals(receiver) && !permissionChecker.check(mentionFPlayer, permission.getBypass())) {
                    sendMention(processId, mentionFPlayer);
                }
            }

            String format = StringUtils.replaceEach(resolveLocalization(receiver).getFormat(),
                    new String[]{"<player>", "<target>"},
                    new String[]{mention, mention}
            );

            return Tag.selfClosingInserting(messagePipeline.builder(receiver, format).build());
        });
    }

    private String replace(FEntity sender, String message) {
        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.startsWith(this.message.getTrigger())) continue;

            String wordWithoutPrefix = Strings.CS.replaceOnce(word, this.message.getTrigger(), "");

            FPlayer mentionFPlayer = fPlayerService.getFPlayer(wordWithoutPrefix);
            boolean isMention = !mentionFPlayer.isUnknown() && integrationModule.canSeeVanished(mentionFPlayer, sender)
                    || integrationModule.getGroups().contains(wordWithoutPrefix) && permissionChecker.check(sender, permission.getGroup());
            if (isMention) {
                words[i] = "<mention:" + wordWithoutPrefix + ">";
            }
        }

        return String.join(" ", words);
    }

    private void sendMention(UUID processId, FPlayer fPlayer) {
        if (processedMentions.containsKey(processId)) return;

        processedMentions.put(processId, true);

        playSound(fPlayer);

        builder(fPlayer)
                .destination(message.getDestination())
                .format(Localization.Message.Format.Mention::getPerson)
                .sound(null)
                .sendBuilt();
    }
}
