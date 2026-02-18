package net.flectone.pulse;

import com.google.gson.Gson;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.execution.pipeline.HytaleMessagePipeline;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.HytaleTaskScheduler;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.HytaleChatsettingModule;
import net.flectone.pulse.module.integration.HytaleIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.HytaleBubbleModule;
import net.flectone.pulse.module.message.bubble.render.BubbleRender;
import net.flectone.pulse.module.message.bubble.render.HytaleBubbleRender;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.HytaleChatModule;
import net.flectone.pulse.module.message.format.scoreboard.HytaleScoreboardModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.world.HytaleWorldModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.join.HytaleJoinModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.sidebar.HytaleSidebarModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.vanilla.HytaleVanillaModule;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.platform.adapter.HytalePlayerAdapter;
import net.flectone.pulse.platform.adapter.HytaleServerAdapter;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.render.*;
import net.flectone.pulse.platform.sender.HytaleMessageSender;
import net.flectone.pulse.platform.sender.HytaleSoundPlayer;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.HytaleSkinService;
import net.flectone.pulse.service.HytaleTranslationService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.service.TranslationService;
import net.flectone.pulse.util.checker.HytalePermissionChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.nio.file.Path;

public class HytaleInjector extends PlatformInjector {

    private final HytaleFlectonePulse flectonePulse;

    public HytaleInjector(HytaleFlectonePulse flectonePulse,
                          Path projectPath,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(projectPath, libraryResolver, fLogger);

        this.flectonePulse = flectonePulse;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(HytaleFlectonePulse.class).toInstance(flectonePulse);
        bind(JavaPlugin.class).toInstance(flectonePulse);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        // adapters
        bind(PlatformPlayerAdapter.class).to(HytalePlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(HytaleServerAdapter.class);

        // registries
        bind(PermissionRegistry.class).to(HytalePermissionRegistry.class);
        bind(ListenerRegistry.class).to(HytaleListenerRegistry.class);
        bind(CommandRegistry.class).to(HytaleCommandRegistry.class);

        // checkers and utilities
        bind(PermissionChecker.class).to(HytalePermissionChecker.class);
        bind(TaskScheduler.class).to(HytaleTaskScheduler.class);
        bind(MessagePipeline.class).to(HytaleMessagePipeline.class);

        // integrations
        bind(IntegrationModule.class).to(HytaleIntegrationModule.class);

        // commands
        bind(ChatsettingModule.class).to(HytaleChatsettingModule.class);
//        bind(PollModule.class).to(MinecraftPollModule.class);

        // messages
        bind(BubbleModule.class).to(HytaleBubbleModule.class);
        bind(ChatModule.class).to(HytaleChatModule.class);
//        bind(ObjectModule.class).to(MinecraftObjectModule.class);
        bind(ScoreboardModule.class).to(HytaleScoreboardModule.class);
        bind(WorldModule.class).to(HytaleWorldModule.class);
        bind(JoinModule.class).to(HytaleJoinModule.class);
//        bind(ObjectModule.class).to(MinecraftObjectModule.class);
//        bind(RightclickModule.class).to(MinecraftRightClickModule.class);
        bind(SidebarModule.class).to(HytaleSidebarModule.class);
//        bind(StatusModule.class).to(MinecraftStatusModule.class);
//        bind(TabModule.class).to(MinecraftTabModule.class);
        bind(VanillaModule.class).to(HytaleVanillaModule.class);

        // renders
        bind(ActionBarRender.class).to(HytalyActionBarRender.class);
        bind(BossBarRender.class).to(HytaleBossBarRender.class);
        bind(BrandRender.class).to(HytaleBrandRender.class);
        bind(ListFooterRender.class).to(HytaleListFooterRender.class);
        bind(TextScreenRender.class).to(HytaleTextScreenRender.class);
        bind(TitleRender.class).to(HytaleTitleRender.class);
        bind(ToastRender.class).to(HytaleToastRender.class);
        bind(BubbleRender.class).to(HytaleBubbleRender.class);

        // senders
        bind(MessageSender.class).to(HytaleMessageSender.class);
        bind(SoundPlayer.class).to(HytaleSoundPlayer.class);

        // others
        bind(SkinService.class).to(HytaleSkinService.class);
        bind(TranslationService.class).to(HytaleTranslationService.class);
    }

}
