package net.flectone.pulse.module.message.damage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.damage.listener.DamagePulseListener;
import net.flectone.pulse.module.message.damage.model.Damage;
import net.flectone.pulse.module.message.damage.model.DamageMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DamageModule extends AbstractModuleLocalization<Localization.Message.Damage> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(DamagePulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.DAMAGE;
    }

    @Override
    public Message.Damage config() {
        return fileResolver.getMessage().getDamage();
    }

    @Override
    public Permission.Message.Damage permission() {
        return fileResolver.getPermission().getMessage().getDamage();
    }

    @Override
    public Localization.Message.Damage localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getDamage();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Damage damage) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DamageMetadata.<Localization.Message.Damage>builder()
                .sender(fPlayer)
                .format(localization -> Strings.CS.replace(localization.getFormat(), "<amount>", damage.amount()))
                .damage(damage)
                .translationKey(translationKey)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, damage.target())})
                .build()
        );
    }
}