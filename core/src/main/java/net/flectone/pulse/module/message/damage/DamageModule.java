package net.flectone.pulse.module.message.damage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.damage.listener.DamagePulseListener;
import net.flectone.pulse.module.message.damage.model.Damage;
import net.flectone.pulse.module.message.damage.model.DamageMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;

@Singleton
public class DamageModule extends AbstractModuleLocalization<Localization.Message.Damage> {

    private final Message.Damage message;
    private final Permission.Message.Damage permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DamageModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDamage(), MessageType.DAMAGE);

        this.message = fileResolver.getMessage().getDamage();
        this.permission = fileResolver.getPermission().getMessage().getDamage();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DamagePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, Damage damage) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DamageMetadata.<Localization.Message.Damage>builder()
                .sender(damage.entity())
                .filterPlayer(fPlayer)
                .format(s -> Strings.CS.replace(
                        s.getFormat(),
                        "<amount>",
                        damage.amount()
                ))
                .damage(damage)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}