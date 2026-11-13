package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.Gson;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModuleImpl;
import net.flectone.pulse.module.command.chatsetting.MinecraftChatSettingModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModuleImpl;
import net.flectone.pulse.module.command.maintenance.MinecraftMaintenanceModule;
import net.flectone.pulse.module.command.poll.MinecraftPollModule;
import net.flectone.pulse.module.command.poll.PollModuleImpl;
import net.flectone.pulse.module.command.spy.MinecraftSpyModule;
import net.flectone.pulse.module.command.spy.SpyModuleImpl;
import net.flectone.pulse.module.integration.IntegrationModuleImpl;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.message.bossbar.BossbarModuleImpl;
import net.flectone.pulse.module.message.bossbar.MinecraftBossbarModule;
import net.flectone.pulse.module.message.bubble.BubbleModuleImpl;
import net.flectone.pulse.module.message.bubble.MinecraftBubbleModule;
import net.flectone.pulse.module.message.chat.ChatModuleImpl;
import net.flectone.pulse.module.message.chat.MinecraftChatModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModuleImpl;
import net.flectone.pulse.module.message.format.moderation.delete.MinecraftDeleteModule;
import net.flectone.pulse.module.message.format.object.MinecraftObjectModule;
import net.flectone.pulse.module.message.format.object.ObjectModuleImpl;
import net.flectone.pulse.module.message.join.JoinModuleImpl;
import net.flectone.pulse.module.message.join.MinecraftJoinModule;
import net.flectone.pulse.module.message.quit.MinecraftQuitModule;
import net.flectone.pulse.module.message.quit.QuitModuleImpl;
import net.flectone.pulse.module.message.rightclick.MinecraftRightClickModule;
import net.flectone.pulse.module.message.rightclick.RightclickModuleImpl;
import net.flectone.pulse.module.message.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModuleImpl;
import net.flectone.pulse.module.message.scoreboard.objective.MinecraftObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModuleImpl;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.BelownameModuleImpl;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.MinecraftBelownameModule;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.MinecraftTabnameModule;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.TabnameModuleImpl;
import net.flectone.pulse.module.message.serverlink.MinecraftServerlinkModule;
import net.flectone.pulse.module.message.serverlink.ServerlinkModuleImpl;
import net.flectone.pulse.module.message.sidebar.MinecraftSidebarModule;
import net.flectone.pulse.module.message.sidebar.SidebarModuleImpl;
import net.flectone.pulse.module.message.status.MinecraftStatusModule;
import net.flectone.pulse.module.message.status.StatusModuleImpl;
import net.flectone.pulse.module.message.status.icon.IconModuleImpl;
import net.flectone.pulse.module.message.status.icon.MinecraftIconModule;
import net.flectone.pulse.module.message.status.motd.MinecraftMOTDModule;
import net.flectone.pulse.module.message.status.motd.MotdModuleImpl;
import net.flectone.pulse.module.message.status.players.MinecraftPlayersModule;
import net.flectone.pulse.module.message.status.players.PlayersModuleImpl;
import net.flectone.pulse.module.message.tab.TabModuleImpl;
import net.flectone.pulse.module.message.tab.footer.FooterModuleImpl;
import net.flectone.pulse.module.message.tab.footer.MinecraftFooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModuleImpl;
import net.flectone.pulse.module.message.tab.header.MinecraftHeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModuleImpl;
import net.flectone.pulse.module.message.vanilla.MinecraftVanillaModule;
import net.flectone.pulse.module.message.vanilla.VanillaModuleImpl;
import net.flectone.pulse.module.message.vanilla.extractor.ComponentExtractor;
import net.flectone.pulse.module.message.vanilla.extractor.MinecraftComponentExtractor;
import net.flectone.pulse.platform.adapter.MinecraftPacketEventsAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.MinecraftListenerRegistry;
import net.flectone.pulse.platform.render.*;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.platform.sender.MinecraftSoundPlayer;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.MinecraftProfileResolver;
import net.flectone.pulse.resolver.ProfileResolver;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.flectone.pulse.serializer.MinecraftComponentSerializer;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.service.MinecraftTranslationService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.service.TranslationService;

import java.nio.file.Path;

public abstract class MinecraftPlatformInjector extends PlatformInjector {

    private final MinecraftPacketEventsAdapter minecraftPacketEventsAdapter;

    protected MinecraftPlatformInjector(Path projectPath,
                                        MinecraftPacketEventsAdapter minecraftPacketEventsAdapter,
                                        LibraryResolver libraryResolver,
                                        FLogger fLogger) {
        super(projectPath, libraryResolver, fLogger);

        this.minecraftPacketEventsAdapter = minecraftPacketEventsAdapter;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_14")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_16")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_17")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_18")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_19_3")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_6")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_9")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_26_2")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_26_2));

        // adapters
        bind(MinecraftPacketEventsAdapter.class).toInstance(minecraftPacketEventsAdapter);

        // commands
        bind(ChatsettingModuleImpl.class).to(MinecraftChatSettingModule.class);
        bind(MaintenanceModuleImpl.class).to(MinecraftMaintenanceModule.class);
        bind(PollModuleImpl.class).to(MinecraftPollModule.class);
        bind(SpyModuleImpl.class).to(MinecraftSpyModule.class);

        // integrations
        bind(IntegrationModuleImpl.class).to(MinecraftIntegrationModule.class);

        // messages
        bind(BossbarModuleImpl.class).to(MinecraftBossbarModule.class);
        bind(BubbleModuleImpl.class).to(MinecraftBubbleModule.class);
        bind(ChatModuleImpl.class).to(MinecraftChatModule.class);
        bind(DeleteModuleImpl.class).to(MinecraftDeleteModule.class);
        bind(ObjectModuleImpl.class).to(MinecraftObjectModule.class);
        bind(ScoreboardModuleImpl.class).to(MinecraftScoreboardModule.class);
        bind(ServerlinkModuleImpl.class).to(MinecraftServerlinkModule.class);
        bind(JoinModuleImpl.class).to(MinecraftJoinModule.class);
        bind(ObjectiveModuleImpl.class).to(MinecraftObjectiveModule.class);
        bind(BelownameModuleImpl.class).to(MinecraftBelownameModule.class);
        bind(TabnameModuleImpl.class).to(MinecraftTabnameModule.class);
        bind(QuitModuleImpl.class).to(MinecraftQuitModule.class);
        bind(RightclickModuleImpl.class).to(MinecraftRightClickModule.class);
        bind(SidebarModuleImpl.class).to(MinecraftSidebarModule.class);
        bind(StatusModuleImpl.class).to(MinecraftStatusModule.class);
        bind(IconModuleImpl.class).to(MinecraftIconModule.class);
        bind(MotdModuleImpl.class).to(MinecraftMOTDModule.class);
        bind(PlayersModuleImpl.class).to(MinecraftPlayersModule.class);
        bind(FooterModuleImpl.class).to(MinecraftFooterModule.class);
        bind(HeaderModuleImpl.class).to(MinecraftHeaderModule.class);
        bind(PlayerlistnameModuleImpl.class).to(MinecraftPlayerlistnameModule.class);
        bind(VanillaModuleImpl.class).to(MinecraftVanillaModule.class);

        // registers
        bind(ListenerRegistry.class).to(MinecraftListenerRegistry.class);

        // renders
        bind(ActionBarRender.class).to(MinecraftActionBarRender.class);
        bind(BossBarRender.class).to(MinecraftBossBarRender.class);
        bind(BrandRender.class).to(MinecraftBrandRender.class);
        bind(ListFooterRender.class).to(MinecraftListFooterRender.class);
        bind(HeadTextRender.class).to(MinecraftHeadTextRender.class);
        bind(TitleRender.class).to(MinecraftTitleRender.class);
        bind(ToastRender.class).to(MinecraftToastRender.class);

        // senders
        bind(MessageSender.class).to(MinecraftMessageSender.class);
        bind(SoundPlayer.class).to(MinecraftSoundPlayer.class);

        // others
        bind(ComponentSerializer.class).to(MinecraftComponentSerializer.class);
        bind(ComponentExtractor.class).to(MinecraftComponentExtractor.class);
        bind(new TypeLiteral<ComponentExtractor<?>>() {}).to(MinecraftComponentExtractor.class);
        bind(SkinService.class).to(MinecraftSkinService.class);
        bind(ProfileResolver.class).to(MinecraftProfileResolver.class);
        bind(TranslationService.class).to(MinecraftTranslationService.class);
    }

    protected <T> void bindVersioned(Class<T> type) {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

        for (ServerVersion version : ServerVersion.reversedValues()) {
            if (version == ServerVersion.ERROR || serverVersion.isOlderThan(version)) continue;

            try {
                Class<?> implementation = Class.forName(type.getName() + version.name(), false, type.getClassLoader());
                bind(type).to(implementation.asSubclass(type));
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new IllegalStateException("No implementation of " + type.getSimpleName() + " for " + serverVersion.getReleaseName());
    }

}
