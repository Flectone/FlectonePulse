package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.tree.CommandNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.exception.InjectorNotInitializedException;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.player.FabricPlayerLoginListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.integration.simplevoice.MinecraftSimpleVoiceModule;
import net.flectone.pulse.platform.adapter.FabricPacketEventsAdapter;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.resolver.FabricLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Getter
@Singleton
public class FabricFlectonePulse implements FlectonePulse {

    private final Supplier<FabricFlectonePulseLoader> loader;

    @Setter
    private MinecraftServer minecraftServer;

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private FabricPacketEventsAdapter packetEventsAdapter;
    private Injector injector;

    private MinecraftSimpleVoiceModule simpleVoiceModule;
    private FabricPlayerLoginListener fabricPlayerLoginListener;

    public FabricFlectonePulse(Supplier<FabricFlectonePulseLoader> loader) {
        this.loader = loader;
    }

    @Override
    public void onLoad() {
        // initialize custom logger
        Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
        fLogger = new FLogger(
                logRecord -> logger.info(logRecord.getMessage()),
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new FabricLibraryResolver(logger);
        libraryResolver.resolveRepositories();

        try {
            libraryResolver.loadLibraries();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Failed to download library")) {
                logger.error("\n\n====================\n A problem occurred while downloading the libraries, perhaps you do not have access to repository. \n Try downloading the libraries manually from https://flectone.net/files/r/FlectonePulse-libraries.zip and extract them into FlectonePulse folder \n====================\n");
            }

            throw e;
        }

        // load PacketEvents
        packetEventsAdapter = new FabricPacketEventsAdapter();
        packetEventsAdapter.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new FabricInjector(this, packetEventsAdapter, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        removeDefaultFabricCommands();

        // get scheduler
        TaskScheduler taskScheduler = get(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        ServerTickEvents.START_SERVER_TICK.register(_ -> taskScheduler.onTick());

        injector.getInstance(FlectonePulseAPIImpl.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);
            return;
        }

        get(FlectonePulseAPIImpl.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPIImpl.class).reload();
    }

    @Override
    public @NonNull FabricFlectonePulseLoader getLoader() {
        return loader.get();
    }

    @Override
    public <T> T get(Class<T> type) {
        if (!isReady()) {
            throw new InjectorNotInitializedException();
        }

        return injector.getInstance(type);
    }

    @Override
    public boolean isReady() {
        return injector != null;
    }

    @Override
    public void hook(HookType type, Object... args) {
        try {
            switch (type) {
                case CONFIGURE_SERIALIZATION -> packetEventsAdapter.configureSerialization((ChannelPipeline) args[0], (PacketFlow) args[1]);
                case PRE_NEW_PLAYER_PLACE -> packetEventsAdapter.preNewPlayerPlace((Connection) args[0], (Channel) args[1], (ServerPlayer) args[2]);
                case ON_PLAYER_PRE_LOGIN -> getPlayerLoginListener().onPreLogin((ServerLoginPacketListenerImpl) args[0], (GameProfile) args[1]);
                case ON_PLAYER_LOGIN -> packetEventsAdapter.onPlayerLogin((Connection) args[0], (ServerPlayer) args[1]);
                case POST_RESPAWN -> packetEventsAdapter.postRespawn((Connection) args[0], (Channel) args[1], (ServerPlayer) args[2]);
                case INIT_PACKET_ADAPTER -> packetEventsAdapter.init();
                case TERMINATE_FAILED_PACKET_ADAPTER -> packetEventsAdapter.terminateFailed();
                case TERMINATE_PACKET_ADAPTER -> packetEventsAdapter.terminate();
                case CLOSE_UIS -> {
                    injector.getInstance(MinecraftInventoryController.class).closeAll();
                    injector.getInstance(MinecraftDialogController.class).closeAll();
                }
                case SIMPLEVOICE_ENTITY_SOUND_PACKET -> getSimpleVoiceModule().onEntitySoundPacketEvent(args[0]);
                case SIMPLEVOICE_MICROPHONE_PACKET -> getSimpleVoiceModule().onMicrophonePacketEvent(args[0]);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            fLogger.warning("Hook %s type called with invalid arguments: %s", type, e.getMessage());
        }
    }

    private FabricPlayerLoginListener getPlayerLoginListener() {
        if (fabricPlayerLoginListener == null) {
            fabricPlayerLoginListener = injector.getInstance(FabricPlayerLoginListener.class);
        }

        return fabricPlayerLoginListener;
    }

    private MinecraftSimpleVoiceModule getSimpleVoiceModule() {
        if (simpleVoiceModule == null) {
            simpleVoiceModule = injector.getInstance(MinecraftSimpleVoiceModule.class);
        }

        return simpleVoiceModule;
    }

    private void removeDefaultFabricCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            CommandNode<CommandSourceStack> root = dispatcher.getRoot();

            for (String command : injector.getInstance(FileFacade.class).config().internal().vanillaCommandsToRemove()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }
}