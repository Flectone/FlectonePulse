package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.advancedban.BukkitAdvancedBanModule;
import net.flectone.pulse.module.integration.blazeandcave.BukkitBlazeandCaveModule;
import net.flectone.pulse.module.integration.cmi.BukkitCMIModule;
import net.flectone.pulse.module.integration.interactivechat.BukkitInteractiveChatModule;
import net.flectone.pulse.module.integration.itemsadder.BukkitItemsAdderModule;
import net.flectone.pulse.module.integration.libertybans.BukkitLibertyBansModule;
import net.flectone.pulse.module.integration.litebans.BukkitLiteBansModule;
import net.flectone.pulse.module.integration.maintenance.BukkitMaintenanceModule;
import net.flectone.pulse.module.integration.miniplaceholders.BukkitMiniPlaceholdersModule;
import net.flectone.pulse.module.integration.motd.BukkitMOTDModule;
import net.flectone.pulse.module.integration.placeholderapi.BukkitPlaceholderAPIModule;
import net.flectone.pulse.module.integration.supervanish.BukkitSuperVanishModule;
import net.flectone.pulse.module.integration.tab.BukkitTABModule;
import net.flectone.pulse.module.integration.triton.BukkitTritonModule;
import net.flectone.pulse.module.integration.vault.BukkitVaultModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.checker.BukkitDatapackChecker;
import net.flectone.pulse.util.checker.PaperDatapackChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;

@Singleton
public class BukkitIntegrationModule extends MinecraftIntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final Injector injector;
    private final FLogger fLogger;

    @Inject
    public BukkitIntegrationModule(FileFacade fileFacade,
                                   FLogger fLogger,
                                   PlatformServerAdapter platformServerAdapter,
                                   ReflectionResolver reflectionResolver,
                                   ModuleController moduleController,
                                   Injector injector) {
        super(fileFacade, fLogger, platformServerAdapter, reflectionResolver, moduleController, injector);
        
        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.injector = injector;
        this.fLogger = fLogger;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("AdvancedBan")) {
            builder.add(BukkitAdvancedBanModule.class);
        }

        if (isDatapackEnabled("Blazeandcave")) {
            builder.add(BukkitBlazeandCaveModule.class);
        }

        if (platformServerAdapter.hasProject("CMI")) {
            builder.add(BukkitCMIModule.class);
        }

        if (platformServerAdapter.hasProject("PlaceholderAPI")) {
            builder.add(BukkitPlaceholderAPIModule.class);
        }

        if (platformServerAdapter.hasProject("Vault")) {
            builder.add(BukkitVaultModule.class);
        }

        if (platformServerAdapter.hasProject("InteractiveChat")) {
            if (reflectionResolver.hasClass("com.loohp.interactivechat.registry.Registry")) {
                builder.add(BukkitInteractiveChatModule.class);
            } else {
                fLogger.warning("Update InteractiveChat to the latest version");
            }
        }

        if (platformServerAdapter.hasProject("ItemsAdder")) {
            Class<?> fontImageWrapper = reflectionResolver.resolveClass("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
            if (fontImageWrapper != null && reflectionResolver.hasMethod(fontImageWrapper, "replaceFontImages", Permissible.class, String.class)) {
                builder.add(BukkitItemsAdderModule.class);
            } else {
                fLogger.warning("Update ItemsAdder to the latest version");
            }
        }

        if (platformServerAdapter.hasProject("LibertyBans")) {
            builder.add(BukkitLibertyBansModule.class);
        }

        if (platformServerAdapter.hasProject("LiteBans")) {
            builder.add(BukkitLiteBansModule.class);
        }

        if (platformServerAdapter.hasProject("Maintenance")) {
            builder.add(BukkitMaintenanceModule.class);
        }

        if (platformServerAdapter.hasProject("MiniPlaceholders")) {
            builder.add(BukkitMiniPlaceholdersModule.class);
        }

        if (platformServerAdapter.hasProject("MOTD")) {
            builder.add(BukkitMOTDModule.class);
        }

        if (platformServerAdapter.hasProject("SuperVanish") || platformServerAdapter.hasProject("PremiumVanish")) {
            if (reflectionResolver.hasClass("de.myzelyam.api.vanish.VanishAPI")) {
                builder.add(BukkitSuperVanishModule.class);
            } else {
                fLogger.warning("Integration with SuperVanish is not possible. Are you using another plugin with the same name? It is only supported https://www.spigotmc.org/resources/supervanish-be-invisible.1331/");
            }
        }

        if (platformServerAdapter.hasProject("TAB")) {
            builder.add(BukkitTABModule.class);
        }

        if (platformServerAdapter.hasProject("Triton")) {
            builder.add(BukkitTritonModule.class);
        }

        return builder;
    }

    @Override
    public String checkMention(FEntity fSender, String message) {
        if (moduleController.isDisabledFor(this, fSender)) return message;

        if (containsEnabledChild(BukkitInteractiveChatModule.class)) {
            return getInstance(BukkitInteractiveChatModule.class).checkMention(fSender, message);
        }

        return message;
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        boolean value = super.hasFPlayerPermission(fPlayer, permission);

        if (containsEnabledChild(BukkitVaultModule.class)) {
            return getInstance(BukkitVaultModule.class).hasVaultPermission(fPlayer, permission);
        }

        return value;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        String prefix = super.getPrefix(fPlayer);
        if (prefix != null) return prefix;

        if (containsEnabledChild(BukkitVaultModule.class)) {
            return getInstance(BukkitVaultModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        String suffix = super.getSuffix(fPlayer);
        if (suffix != null) return suffix;

        if (containsEnabledChild(BukkitVaultModule.class)) {
            return getInstance(BukkitVaultModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> groups = super.getGroups();
        if (!groups.isEmpty()) return groups;

        if (containsEnabledChild(BukkitVaultModule.class)) {
            return getInstance(BukkitVaultModule.class).getGroups();
        }

        return Collections.emptySet();
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (containsEnabledChild(BukkitSuperVanishModule.class)) {
            return getInstance(BukkitSuperVanishModule.class).isVanished(sender);
        }

        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.getMetadata("vanished")
                .stream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.hasPermission("sv.see") || player.hasPermission("cmi.seevanished");
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        if (containsEnabledChild(BukkitLiteBansModule.class)) {
            return getInstance(BukkitLiteBansModule.class).isMuted(fPlayer);
        }

        if (containsEnabledChild(BukkitAdvancedBanModule.class)) {
            return getInstance(BukkitAdvancedBanModule.class).isMuted(fPlayer);
        }

        if (containsEnabledChild(BukkitCMIModule.class)) {
            return getInstance(BukkitCMIModule.class).isMuted(fPlayer);
        }

        if (containsEnabledChild(BukkitLibertyBansModule.class)) {
            return getInstance(BukkitLibertyBansModule.class).isMuted(fPlayer);
        }

        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        if (containsEnabledChild(BukkitLiteBansModule.class)) {
            return getInstance(BukkitLiteBansModule.class).getMute(fPlayer);
        }

        if (containsEnabledChild(BukkitAdvancedBanModule.class)) {
            return getInstance(BukkitAdvancedBanModule.class).getMute(fPlayer);
        }

        if (containsEnabledChild(BukkitCMIModule.class)) {
            return getInstance(BukkitCMIModule.class).getMute(fPlayer);
        }

        if (containsEnabledChild(BukkitLibertyBansModule.class)) {
            return getInstance(BukkitLibertyBansModule.class).getMute(fPlayer);
        }

        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(BukkitTritonModule.class)) return null;

        return getInstance(BukkitTritonModule.class).getLocale(fPlayer);
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        if (moduleController.isDisabledFor(this, fReceiver)) return false;

        if (containsEnabledChild(BukkitInteractiveChatModule.class)) {
            return getInstance(BukkitInteractiveChatModule.class).sendMessage(fReceiver, message);
        }

        return false;
    }

    public boolean isDatapackEnabled(@NonNull String name) {
        if (!reflectionResolver.hasClass("org.bukkit.packs.DataPack")) return false;

        return reflectionResolver.isPaper()
                ? injector.getInstance(PaperDatapackChecker.class).isEnabled(name)
                : injector.getInstance(BukkitDatapackChecker.class).isEnabled(name);
    }

}
