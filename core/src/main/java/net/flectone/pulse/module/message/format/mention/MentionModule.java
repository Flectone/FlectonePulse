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

    @Inject private ComponentUtil componentUtil;

    @Inject
    public MentionModule(FileManager fileManager,
                         FPlayerManager fPlayerManager,
                         PermissionUtil permissionUtil) {
        super(localization -> localization.getMessage().getFormat().getMention());
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;

        message = fileManager.getMessage().getFormat().getMention();
        permission = fileManager.getPermission().getMessage().getFormat().getMention();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

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
            if (fPlayerManager.getOnline(wordWithoutPrefix).isUnknown()) continue;
            words[i] = "<mention:" + wordWithoutPrefix + ">";
            break;
        }

        return String.join(" ", words);
    }

    public TagResolver mentionTag(FEntity sender, FEntity receiver) {;
        if (checkModulePredicates(sender)) return TagResolver.empty();

        return TagResolver.resolver("mention", (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String playerName = mentionTag.value();
            if (playerName.isEmpty()) {
                return Tag.preProcessParsed(message.getTrigger() + playerName);
            }

            FPlayer mentionFPlayer = fPlayerManager.getOnline(playerName);
            if (mentionFPlayer.isUnknown() || permissionUtil.has(mentionFPlayer, permission.getBypass())) {
                return Tag.preProcessParsed(message.getTrigger() + playerName);
            }

            if (mentionFPlayer.equals(receiver)) {
                playSound(mentionFPlayer);
            }

            return Tag.selfClosingInserting(componentUtil
                    .builder(mentionFPlayer, receiver, resolveLocalization(receiver).getFormat())
                    .build()
            );
        });
    }
}
