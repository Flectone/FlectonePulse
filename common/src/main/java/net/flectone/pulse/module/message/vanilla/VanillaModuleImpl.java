package net.flectone.pulse.module.message.vanilla;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.vanilla.listener.VanillaProxyMessageListener;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaModuleImpl implements VanillaModule {

    private final FileFacade fileFacade;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(VanillaProxyMessageListener.class);
        }
    }

    @Override
    public TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent) {
        // vanilla messages are a platform feature, nothing to parse by default
        return TagResolver.empty();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_VANILLA;
    }

    @Override
    public Message.Vanilla config() {
        return fileFacade.message().vanilla();
    }

    @Override
    public Permission.Message.Vanilla permission() {
        return fileFacade.permission().message().vanilla();
    }

    @Override
    public Localization.Message.Vanilla localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().vanilla();
    }

}
