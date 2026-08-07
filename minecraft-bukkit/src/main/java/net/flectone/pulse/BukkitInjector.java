package net.flectone.pulse;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.inject.Singleton;
import net.flectone.pulse.scheduler.BukkitTaskScheduler;
import net.flectone.pulse.scheduler.TaskSchedulerImpl;
import net.flectone.pulse.module.command.spy.BukkitSpyModule;
import net.flectone.pulse.module.command.spy.MinecraftSpyModule;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.integration.simplevoice.BukkitSimpleVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.MinecraftSimpleVoiceModule;
import net.flectone.pulse.module.message.afk.AfkModuleImpl;
import net.flectone.pulse.module.message.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModuleImpl;
import net.flectone.pulse.module.message.anvil.BukkitAnvilModule;
import net.flectone.pulse.module.message.book.BookModuleImpl;
import net.flectone.pulse.module.message.book.BukkitBookModule;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.module.message.chat.MinecraftChatModule;
import net.flectone.pulse.module.message.join.BukkitJoinModule;
import net.flectone.pulse.module.message.join.MinecraftJoinModule;
import net.flectone.pulse.module.message.quit.BukkitQuitModule;
import net.flectone.pulse.module.message.quit.MinecraftQuitModule;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import net.flectone.pulse.module.message.sign.SignModuleImpl;
import net.flectone.pulse.platform.adapter.*;
import net.flectone.pulse.platform.provider.*;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.registry.ProxyRegistryImpl;
import net.flectone.pulse.platform.sender.BukkitMessageSender;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.checker.BukkitPermissionChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.logging.FLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitInjector extends MinecraftPlatformInjector {

    private final BukkitFlectonePulse instance;
    private final Plugin plugin;

    public BukkitInjector(BukkitFlectonePulse instance,
                          Plugin plugin,
                          MinecraftPacketEventsAdapter minecraftPacketEventsAdapter,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(plugin.getDataFolder().toPath(), minecraftPacketEventsAdapter, libraryResolver, fLogger);

        this.instance = instance;
        this.plugin = plugin;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        super.setupPlatform(reflectionResolver);

        bind(FlectonePulse.class).toInstance(instance);
        bind(BukkitFlectonePulse.class).toInstance(instance);
        bind(Plugin.class).toInstance(plugin);

        // Adapters
        bind(PlatformPlayerAdapter.class).to(BukkitPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(BukkitServerAdapter.class);

        // Providers
        if (reflectionResolver.hasClass("org.bukkit.attribute.Attribute")) {
            bind(BukkitAttributesProvider.class).to(BukkitModernAttributesProvider.class);
        } else {
            bind(BukkitAttributesProvider.class).to(BukkitLegacyAttributesProvider.class);
        }

        if (reflectionResolver.hasMethod(Player.class, "getPassengers")) {
            bind(BukkitPassengersProvider.class).to(BukkitModernPassengersProvider.class);
        } else {
            bind(BukkitPassengersProvider.class).to(BukkitLegacyPassengersProvider.class);
        }

        // Registries
        bind(PermissionRegistry.class).to(BukkitPermissionRegistry.class);
        bind(MinecraftListenerRegistry.class).to(BukkitListenerRegistry.class);
        bind(ProxyRegistryImpl.class).to(BukkitProxyRegistry.class);

        if (reflectionResolver.hasClass("com.mojang.brigadier.arguments.ArgumentType")) {
            bind(CommandRegistry.class).to(ModernBukkitCommandRegistry.class);
        } else {
            bind(CommandRegistry.class).to(LegacyBukkitCommandRegistry.class);
        }

        // Checkers and utilities
        bind(PermissionChecker.class).to(BukkitPermissionChecker.class);
        bind(TaskSchedulerImpl.class).to(BukkitTaskScheduler.class);

        // Modules
        bind(MinecraftIntegrationModule.class).to(BukkitIntegrationModule.class);

        if (reflectionResolver.hasClass("de.maxhenkel.voicechat.api.VoicechatPlugin")) {
            bind(MinecraftSimpleVoiceModule.class).to(BukkitSimpleVoiceModule.class);
        }

        // sender
        bind(MinecraftMessageSender.class).to(BukkitMessageSender.class);

        bind(AnvilModuleImpl.class).to(BukkitAnvilModule.class);
        bind(BookModuleImpl.class).to(BukkitBookModule.class);
        bind(AfkModuleImpl.class).to(BukkitAfkModule.class);
        bind(MinecraftChatModule.class).to(BukkitChatModule.class);
        bind(SignModuleImpl.class).to(BukkitSignModule.class);
        bind(MinecraftSpyModule.class).to(BukkitSpyModule.class);
        bind(MinecraftJoinModule.class).to(BukkitJoinModule.class);
        bind(MinecraftQuitModule.class).to(BukkitQuitModule.class);

        // scheduler
        bind(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class).toInstance(UniversalScheduler.getScheduler(plugin));
    }
}
