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
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.player.FabricPlayerLoginListener;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.processing.resolver.FabricLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.constant.HookType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
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
    private Injector injector;

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
        LibraryResolver libraryResolver = new FabricLibraryResolver(logger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        WrapperPacketEvents.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new FabricInjector(this, libraryResolver, fLogger));
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

        injector.getInstance(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);
            return;
        }

        get(FlectonePulseAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).reload();
    }

    @Override
    public @NonNull FabricFlectonePulseLoader getLoader() {
        return loader.get();
    }

    @Override
    public void hook(HookType type, Object... args) {
        FabricPlayerLoginListener fabricPlayerLoginListener = injector.getInstance(FabricPlayerLoginListener.class);
        try {
            switch (type) {
                case CONFIGURE_SERIALIZATION -> WrapperPacketEvents.configureSerialization((ChannelPipeline) args[0], (PacketFlow) args[1]);
                case PRE_NEW_PLAYER_PLACE -> WrapperPacketEvents.preNewPlayerPlace((Connection) args[0], (Channel) args[1], (ServerPlayer) args[2]);
                case ON_PLAYER_PRE_LOGIN -> fabricPlayerLoginListener.onPreLogin((ServerLoginPacketListenerImpl) args[0], (GameProfile) args[1]);
                case ON_PLAYER_LOGIN -> WrapperPacketEvents.onPlayerLogin((Connection) args[0], (ServerPlayer) args[1]);
                case POST_RESPAWN -> WrapperPacketEvents.postRespawn((Connection) args[0], (Channel) args[1], (ServerPlayer) args[2]);
                case INIT_PACKET_ADAPTER -> WrapperPacketEvents.init();
                case TERMINATE_FAILED_PACKET_ADAPTER -> WrapperPacketEvents.terminateFailed();
                case TERMINATE_PACKET_ADAPTER -> WrapperPacketEvents.terminate();
                case CLOSE_UIS -> {
                    injector.getInstance(MinecraftInventoryController.class).closeAll();
                    injector.getInstance(MinecraftDialogController.class).closeAll();
                }
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            fLogger.warning("Hook %s type called with invalid arguments: %s", type, e.getMessage());
        }
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