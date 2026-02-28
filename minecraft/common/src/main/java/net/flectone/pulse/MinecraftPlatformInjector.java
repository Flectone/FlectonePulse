package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.Gson;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.MinecraftDatabase;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.MinecraftChatSettingModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.maintenance.MinecraftMaintenanceModule;
import net.flectone.pulse.module.command.poll.MinecraftPollModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.message.bossbar.BossbarModule;
import net.flectone.pulse.module.message.bossbar.MinecraftBossbarModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.MinecraftBubbleModule;
import net.flectone.pulse.module.message.bubble.render.BubbleRender;
import net.flectone.pulse.module.message.bubble.render.MinecraftBubbleRender;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.MinecraftChatModule;
import net.flectone.pulse.module.message.format.object.MinecraftObjectModule;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.format.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.world.MinecraftWorldModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.join.MinecraftJoinModule;
import net.flectone.pulse.module.message.objective.MinecraftObjectiveModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.rightclick.MinecraftRightClickModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.sidebar.MinecraftSidebarModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.status.MinecraftStatusModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.tab.MinecraftTabModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.module.message.vanilla.MinecraftVanillaModule;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.platform.handler.MinecraftProxyMessageHandler;
import net.flectone.pulse.platform.handler.ProxyMessageHandler;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.MinecraftListenerRegistry;
import net.flectone.pulse.platform.render.*;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.platform.sender.MinecraftSoundPlayer;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.service.MinecraftTranslationService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.service.TranslationService;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.file.Path;

public abstract class MinecraftPlatformInjector extends PlatformInjector {

    protected MinecraftPlatformInjector(Path projectPath,
                                        LibraryResolver libraryResolver,
                                        FLogger fLogger) {
        super(projectPath, libraryResolver, fLogger);
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_14")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_16")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_19_4")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_4));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_6")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_9")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9));

        // database
        bind(Database.class).to(MinecraftDatabase.class);

        // commands
        bind(ChatsettingModule.class).to(MinecraftChatSettingModule.class);
        bind(MaintenanceModule.class).to(MinecraftMaintenanceModule.class);
        bind(PollModule.class).to(MinecraftPollModule.class);

        // integrations
        bind(IntegrationModule.class).to(MinecraftIntegrationModule.class);

        // messages
        bind(BossbarModule.class).to(MinecraftBossbarModule.class);
        bind(BubbleModule.class).to(MinecraftBubbleModule.class);
        bind(ChatModule.class).to(MinecraftChatModule.class);
        bind(ObjectModule.class).to(MinecraftObjectModule.class);
        bind(ScoreboardModule.class).to(MinecraftScoreboardModule.class);
        bind(WorldModule.class).to(MinecraftWorldModule.class);
        bind(JoinModule.class).to(MinecraftJoinModule.class);
        bind(ObjectiveModule.class).to(MinecraftObjectiveModule.class);
        bind(RightclickModule.class).to(MinecraftRightClickModule.class);
        bind(SidebarModule.class).to(MinecraftSidebarModule.class);
        bind(StatusModule.class).to(MinecraftStatusModule.class);
        bind(TabModule.class).to(MinecraftTabModule.class);
        bind(VanillaModule.class).to(MinecraftVanillaModule.class);

        // registers
        bind(ListenerRegistry.class).to(MinecraftListenerRegistry.class);

        // renders
        bind(ActionBarRender.class).to(MinecraftActionBarRender.class);
        bind(BossBarRender.class).to(MinecraftBossBarRender.class);
        bind(BrandRender.class).to(MinecraftBrandRender.class);
        bind(ListFooterRender.class).to(MinecraftListFooterRender.class);
        bind(TextScreenRender.class).to(MinecraftTextScreenRender.class);
        bind(TitleRender.class).to(MinecraftTitleRender.class);
        bind(ToastRender.class).to(MinecraftToastRender.class);
        bind(BubbleRender.class).to(MinecraftBubbleRender.class);

        // senders
        bind(MessageSender.class).to(MinecraftMessageSender.class);
        bind(SoundPlayer.class).to(MinecraftSoundPlayer.class);

        // others
        bind(SkinService.class).to(MinecraftSkinService.class);
        bind(TranslationService.class).to(MinecraftTranslationService.class);
        bind(ProxyMessageHandler.class).to(MinecraftProxyMessageHandler.class);
    }

}
