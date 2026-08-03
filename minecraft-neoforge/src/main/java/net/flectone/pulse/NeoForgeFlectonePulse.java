package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.mojang.brigadier.tree.CommandNode;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.NeoForgeLibraryResolver;
import net.flectone.pulse.util.constant.HookType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Getter
@Singleton
public class NeoForgeFlectonePulse implements FlectonePulse {

    @Setter
    private MinecraftServer minecraftServer;

    private final Supplier<NeoForgeFlectonePulseLoader> loader;

    private FLogger fLogger;
    private Injector injector;

    public NeoForgeFlectonePulse(Supplier<NeoForgeFlectonePulseLoader> loader) {
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
        LibraryResolver libraryResolver = new NeoForgeLibraryResolver(logger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        WrapperPacketEvents.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new NeoForgeInjector(this, libraryResolver, fLogger));
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
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Pre.class, _ -> taskScheduler.onTick());

        injector.getInstance(FlectonePulseAPI.class).onEnable();
    }

    private void removeDefaultFabricCommands() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            CommandNode<CommandSourceStack> root = event.getDispatcher().getRoot();

            for (String command : injector.getInstance(FileFacade.class).config().internal().vanillaCommandsToRemove()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            terminateFailedPacketAdapter();
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
    public NeoForgeFlectonePulseLoader getLoader() {
        return loader.get();
    }

    @Override
    public void initPacketAdapter() {
        WrapperPacketEvents.init();
    }

    @Override
    public void terminateFailedPacketAdapter() {
        WrapperPacketEvents.terminateFailed();
    }

    @Override
    public void terminatePacketAdapter() {
        WrapperPacketEvents.terminate();
    }

    @Override
    public void closeUIs() {
        // close all open inventories
        injector.getInstance(MinecraftInventoryController.class).closeAll();
        injector.getInstance(MinecraftDialogController.class).closeAll();
    }

    @Override
    public void hook(HookType type, Object... args) {
        try {
            switch (type) {
                case CONFIGURE_SERIALIZATION -> WrapperPacketEvents.configureSerialization((ChannelPipeline) args[0], (PacketFlow) args[1]);
                case PRE_NEW_PLAYER_PLACE -> WrapperPacketEvents.preNewPlayerPlace((Connection) args[0], (ServerPlayer) args[1]);
                case ON_PLAYER_LOGIN -> WrapperPacketEvents.onPlayerLogin((Connection) args[0], (ServerPlayer) args[1]);
                case POST_RESPAWN -> WrapperPacketEvents.postRespawn((CallbackInfoReturnable<ServerPlayer>) args[0]);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            fLogger.warning("Hook % type called with invalid arguments: %s", type, e.getMessage());
        }
    }

}
