package net.flectone.pulse.module.message.format.name;

import com.google.inject.Inject;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class NameModule extends AbstractModuleMessage<Localization.Message.Format.Name> {

    private final Message.Format.Name message;
    private final Permission.Message.Format.Name permission;

    private final IntegrationModule integrationModule;

    @Inject
    private ComponentUtil componentUtil;

    public NameModule(FileManager fileManager,
                      IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getFormat().getName_());

        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getFormat().getName_();
        permission = fileManager.getPermission().getMessage().getFormat().getName_();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void add(FPlayer fPlayer);
    public abstract void remove(FPlayer fPlayer);

    public TagResolver vaultSuffixTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();

        return TagResolver.resolver(Set.of("vault_suffix"), (argumentQueue, context) -> {
            String suffix = integrationModule.getSuffix(fPlayer);
            if (suffix == null || suffix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = componentUtil.builder(fPlayer, fReceiver, suffix)
                    .serialize();


            return Tag.preProcessParsed(text);
        });
    }

    public TagResolver vaultPrefixTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();

        return TagResolver.resolver(Set.of("vault_prefix"), (argumentQueue, context) -> {
            String prefix = integrationModule.getPrefix(fPlayer);
            if (prefix == null || prefix.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            String text = componentUtil.builder(fPlayer, fReceiver, prefix)
                    .serialize();

            return Tag.preProcessParsed(text);
        });
    }

    public TagResolver playerTag(@NotNull FEntity player) {
        if (checkModulePredicates(player)) return TagResolver.empty();

        return TagResolver.resolver("player", (argumentQueue, context) -> Tag.preProcessParsed(player.getName()));
    }

    public TagResolver displayTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();

        return TagResolver.resolver("display_name", (argumentQueue, context) -> {
            if (sender instanceof FPlayer fPlayer) {
                if (fPlayer.isUnknown()) {
                    return Tag.preProcessParsed(resolveLocalization(fReceiver).getUnknown()
                            .replace("<name>", fPlayer.getName())
                    );
                }

                String displayName = resolveLocalization(fReceiver).getDisplay();
                Component name = componentUtil.builder(sender, fReceiver, displayName)
                        .build();

                return Tag.inserting(name);
            }

            return Tag.preProcessParsed(resolveLocalization(fReceiver).getEntity()
                    .replace("<name>", sender.getName())
                    .replace("<type>", sender.getType())
                    .replace("<uuid>", sender.getUuid().toString())
            );
        });
    }
}
