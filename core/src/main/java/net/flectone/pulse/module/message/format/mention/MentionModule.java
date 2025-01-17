package net.flectone.pulse.module.message.format.mention;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class MentionModule extends AbstractModuleMessage<Localization.Message.Format.Mention> {

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;

    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final IntegrationModule integrationModule;

    @Inject private ComponentUtil componentUtil;

    @Inject
    public MentionModule(FileManager fileManager,
                         FPlayerManager fPlayerManager,
                         PermissionUtil permissionUtil,
                         IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getFormat().getMention());

        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
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

            boolean isMention = !fPlayerManager.getOnline(wordWithoutPrefix).isUnknown()
                    || integrationModule.getGroups().contains(wordWithoutPrefix)
                    && permissionUtil.has(sender, permission.getGroup());

            if (!isMention) continue;

            words[i] = "<mention:" + wordWithoutPrefix + ">";
            break;
        }

        return String.join(" ", words);
    }

    public TagResolver mentionTag(FEntity sender, FEntity receiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();

        return TagResolver.resolver("mention", (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(message.getTrigger() + mention);
            }

            if (integrationModule.getGroups().contains(mention)) {
                for (String group : integrationModule.getGroups()) {
                    if (receiver instanceof FPlayer mentionFPlayer
                            && permissionUtil.has(receiver, "group." + group)) {
                        sendMention(mentionFPlayer);
                        break;
                    }
                }

            } else {
                FPlayer mentionFPlayer = fPlayerManager.getOnline(mention);
                if (mentionFPlayer.isUnknown() || permissionUtil.has(mentionFPlayer, permission.getBypass())) {
                    return Tag.preProcessParsed(message.getTrigger() + mention);
                }

                if (mentionFPlayer.equals(receiver)) {
                    sendMention(mentionFPlayer);
                }
            }

            String format = resolveLocalization(receiver).getFormat()
                    .replace("<player>", mention)
                    .replace("<target>", mention);

            return Tag.selfClosingInserting(componentUtil
                    .builder(receiver, format)
                    .build()
            );
        });
    }

    private void sendMention(FPlayer fPlayer) {
        playSound(fPlayer);

        builder(fPlayer)
                .destination(message.getDestination())
                .format(Localization.Message.Format.Mention::getPerson)
                .sound(null)
                .sendBuilt();
    }
}
