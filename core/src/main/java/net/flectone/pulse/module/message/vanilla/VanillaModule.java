package net.flectone.pulse.module.message.vanilla;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.vanilla.listener.VanillaProxyMessageListener;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public abstract class VanillaModule implements ModuleLocalization<Localization.Message.Vanilla> {

    public static final String ARGUMENT = "argument";

    private final FileFacade fileFacade;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;

    protected VanillaModule(FileFacade fileFacade,
                            ProxyRegistry proxyRegistry,
                            ListenerRegistry listenerRegistry,
                            SocialService socialService) {
        this.fileFacade = fileFacade;
        this.proxyRegistry = proxyRegistry;
        this.listenerRegistry = listenerRegistry;
        this.socialService = socialService;
    }

    @Override
    public void onEnable() {
        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(VanillaProxyMessageListener.class);
        }
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

    public abstract TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent);

}
