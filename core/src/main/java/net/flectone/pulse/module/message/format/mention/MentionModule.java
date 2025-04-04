package net.flectone.pulse.module.message.format.mention;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.UUID;
import java.util.WeakHashMap;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class MentionModule extends AbstractModuleMessage<Localization.Message.Format.Mention> {

    private final WeakHashMap<UUID, Boolean> processedMentions = new WeakHashMap<>();

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;

    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;

    @Inject private MessageFormatter messageFormatter;

    @Inject
    public MentionModule(FileManager fileManager,
                         FPlayerService fPlayerService,
                         PermissionChecker permissionChecker,
                         IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getFormat().getMention());

        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getFormat().getMention();
        permission = fileManager.getPermission().getMessage().getFormat().getMention();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        registerPermission(permission.getGroup());
        registerPermission(permission.getBypass());
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (!word.startsWith(this.message.getTrigger())) continue;

            String wordWithoutPrefix = word.replaceFirst(this.message.getTrigger(), "");

            boolean isMention = !fPlayerService.getFPlayer(wordWithoutPrefix).isUnknown()
                    || integrationModule.getGroups().contains(wordWithoutPrefix)
                    && permissionChecker.check(sender, permission.getGroup());

            if (!isMention) continue;

            words[i] = "<mention:" + wordWithoutPrefix + ">";
            break;
        }

        return String.join(" ", words);
    }

    public TagResolver mentionTag(UUID processId, FEntity sender, FEntity receiver) {
        String tag = "mention";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(message.getTrigger() + mention);
            }

            if (integrationModule.getGroups().contains(mention)) {
                for (String group : integrationModule.getGroups()) {
                    if (!(receiver instanceof FPlayer mentionFPlayer)) break;
                    if (permissionChecker.check(mentionFPlayer, permission.getBypass())) break;
                    if (!permissionChecker.check(receiver, "group." + group)) continue;

                    sendMention(processId, mentionFPlayer);
                    break;
                }

            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (mentionFPlayer.equals(receiver) && !permissionChecker.check(mentionFPlayer, permission.getBypass())) {
                    sendMention(processId, mentionFPlayer);
                }
            }

            String format = resolveLocalization(receiver).getFormat()
                    .replace("<player>", mention)
                    .replace("<target>", mention);

            return Tag.selfClosingInserting(messageFormatter.builder(receiver, format).build());
        });
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
