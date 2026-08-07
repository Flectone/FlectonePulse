package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.BukkitDatapackChecker;
import net.flectone.pulse.checker.PaperDatapackChecker;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.advancedban.BukkitAdvancedBanModule;
import net.flectone.pulse.module.integration.blazeandcave.BukkitBlazeandCaveModule;
import net.flectone.pulse.module.integration.cmi.BukkitCMIModule;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.floodgate.MinecraftFloodgateModule;
import net.flectone.pulse.module.integration.geyser.MinecraftGeyserModule;
import net.flectone.pulse.module.integration.interactivechat.BukkitInteractiveChatModule;
import net.flectone.pulse.module.integration.itemsadder.BukkitItemsAdderModule;
import net.flectone.pulse.module.integration.libertybans.BukkitLibertyBansModule;
import net.flectone.pulse.module.integration.litebans.BukkitLiteBansModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.maintenance.BukkitMaintenanceModule;
import net.flectone.pulse.module.integration.miniplaceholders.BukkitMiniPlaceholdersModule;
import net.flectone.pulse.module.integration.motd.BukkitMOTDModule;
import net.flectone.pulse.module.integration.placeholderapi.BukkitPlaceholderAPIModule;
import net.flectone.pulse.module.integration.skinsrestorer.MinecraftSkinsRestorerModule;
import net.flectone.pulse.module.integration.supervanish.BukkitSuperVanishModule;
import net.flectone.pulse.module.integration.tab.BukkitTABModule;
import net.flectone.pulse.module.integration.triton.BukkitTritonModule;
import net.flectone.pulse.module.integration.vault.BukkitVaultModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class BukkitIntegrationModule extends MinecraftIntegrationModule {

    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final FLogger fLogger;
    private final LazyInstance<BukkitAdvancedBanModule> advancedBanModule;
    private final LazyInstance<BukkitCMIModule> cmiModule;
    private final LazyInstance<BukkitInteractiveChatModule> interactiveChatModule;
    private final LazyInstance<BukkitLibertyBansModule> libertyBansModule;
    private final LazyInstance<BukkitLiteBansModule> liteBansModule;
    private final LazyInstance<BukkitSuperVanishModule> superVanishModule;
    private final LazyInstance<BukkitTritonModule> tritonModule;
    private final LazyInstance<BukkitVaultModule> vaultModule;
    private final LazyInstance<BukkitDatapackChecker> bukkitDatapackChecker;
    private final LazyInstance<PaperDatapackChecker> paperDatapackChecker;

    @Inject
    public BukkitIntegrationModule(FileFacade fileFacade,
                                   FLogger fLogger,
                                   LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                   ReflectionResolver reflectionResolver,
                                   ListenerRegistry listenerRegistry,
                                   ModuleController moduleController,
                                   LazyInstance<LuckPermsModule> luckPermsModule,
                                   LazyInstance<DeeplModule> deeplModule,
                                   LazyInstance<YandexModule> yandexModule,
                                   LazyInstance<MinecraftFloodgateModule> floodgateModule,
                                   LazyInstance<MinecraftGeyserModule> geyserModule,
                                   LazyInstance<MinecraftSkinsRestorerModule> skinsRestorerModule,
                                   LazyInstance<BukkitAdvancedBanModule> advancedBanModule,
                                   LazyInstance<BukkitCMIModule> cmiModule,
                                   LazyInstance<BukkitInteractiveChatModule> interactiveChatModule,
                                   LazyInstance<BukkitLibertyBansModule> libertyBansModule,
                                   LazyInstance<BukkitLiteBansModule> liteBansModule,
                                   LazyInstance<BukkitSuperVanishModule> superVanishModule,
                                   LazyInstance<BukkitTritonModule> tritonModule,
                                   LazyInstance<BukkitVaultModule> vaultModule,
                                   LazyInstance<BukkitDatapackChecker> bukkitDatapackChecker,
                                   LazyInstance<PaperDatapackChecker> paperDatapackChecker) {
        super(fileFacade, fLogger, platformServerAdapter, reflectionResolver, listenerRegistry, moduleController,
                luckPermsModule, deeplModule, yandexModule, floodgateModule, geyserModule, skinsRestorerModule);

        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
        this.advancedBanModule = advancedBanModule;
        this.cmiModule = cmiModule;
        this.interactiveChatModule = interactiveChatModule;
        this.libertyBansModule = libertyBansModule;
        this.liteBansModule = liteBansModule;
        this.superVanishModule = superVanishModule;
        this.tritonModule = tritonModule;
        this.vaultModule = vaultModule;
        this.bukkitDatapackChecker = bukkitDatapackChecker;
        this.paperDatapackChecker = paperDatapackChecker;
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> builder = new LinkedHashSet<>(super.children());

        PlatformServerAdapter platformServerAdapterInstance = platformServerAdapter.get();
        if (platformServerAdapterInstance.hasProject("AdvancedBan")) {
            builder.add(BukkitAdvancedBanModule.class);
        }

        if (isDatapackEnabled("Blazeandcave")) {
            builder.add(BukkitBlazeandCaveModule.class);
        }

        if (platformServerAdapterInstance.hasProject("CMI")) {
            builder.add(BukkitCMIModule.class);
        }

        if (platformServerAdapterInstance.hasProject("PlaceholderAPI")) {
            builder.add(BukkitPlaceholderAPIModule.class);
        }

        if (platformServerAdapterInstance.hasProject("Vault")) {
            builder.add(BukkitVaultModule.class);
        }

        if (platformServerAdapterInstance.hasProject("InteractiveChat")) {
            if (reflectionResolver.hasClass("com.loohp.interactivechat.registry.Registry")) {
                builder.add(BukkitInteractiveChatModule.class);
            } else {
                fLogger.warning("Update InteractiveChat to the latest version");
            }
        }

        if (platformServerAdapterInstance.hasProject("ItemsAdder")) {
            Class<?> fontImageWrapper = reflectionResolver.resolveClass("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
            if (fontImageWrapper != null && reflectionResolver.hasMethod(fontImageWrapper, "replaceFontImages", Permissible.class, String.class)) {
                builder.add(BukkitItemsAdderModule.class);
            } else {
                fLogger.warning("Update ItemsAdder to the latest version");
            }
        }

        if (platformServerAdapterInstance.hasProject("LibertyBans")) {
            builder.add(BukkitLibertyBansModule.class);
        }

        if (platformServerAdapterInstance.hasProject("LiteBans")) {
            builder.add(BukkitLiteBansModule.class);
        }

        if (platformServerAdapterInstance.hasProject("Maintenance")) {
            builder.add(BukkitMaintenanceModule.class);
        }

        if (platformServerAdapterInstance.hasProject("MiniPlaceholders")) {
            builder.add(BukkitMiniPlaceholdersModule.class);
        }

        if (platformServerAdapterInstance.hasProject("MOTD")) {
            builder.add(BukkitMOTDModule.class);
        }

        if (platformServerAdapterInstance.hasProject("SuperVanish") || platformServerAdapterInstance.hasProject("PremiumVanish")) {
            if (reflectionResolver.hasClass("de.myzelyam.api.vanish.VanishAPI")) {
                builder.add(BukkitSuperVanishModule.class);
            } else {
                fLogger.warning("Integration with SuperVanish is not possible. Are you using another plugin with the same name? It is only supported https://www.spigotmc.org/resources/supervanish-be-invisible.1331/");
            }
        }

        if (platformServerAdapterInstance.hasProject("TAB")) {
            builder.add(BukkitTABModule.class);
        }

        if (platformServerAdapterInstance.hasProject("Triton")) {
            builder.add(BukkitTritonModule.class);
        }

        return Collections.unmodifiableSet(builder);
    }

    @Override
    public String checkMention(FEntity fSender, String message) {
        if (moduleController.isDisabledFor(this, fSender)) return message;

        if (containsEnabledChild(ModuleName.INTEGRATION_INTERACTIVECHAT)) {
            return interactiveChatModule.get().checkMention(fSender, message);
        }

        return message;
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        boolean value = super.hasFPlayerPermission(fPlayer, permission);

        if (containsEnabledChild(ModuleName.INTEGRATION_VAULT)) {
            return vaultModule.get().hasVaultPermission(fPlayer, permission);
        }

        return value;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        String prefix = super.getPrefix(fPlayer);
        if (prefix != null) return prefix;

        if (containsEnabledChild(ModuleName.INTEGRATION_VAULT)) {
            return vaultModule.get().getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        String suffix = super.getSuffix(fPlayer);
        if (suffix != null) return suffix;

        if (containsEnabledChild(ModuleName.INTEGRATION_VAULT)) {
            return vaultModule.get().getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> groups = super.getGroups();
        if (!groups.isEmpty()) return groups;

        if (containsEnabledChild(ModuleName.INTEGRATION_VAULT)) {
            return vaultModule.get().getGroups();
        }

        return Set.of();
    }

    @Override
    public boolean isVanished(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.getMetadata("vanished")
                .stream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @Override
    public boolean hasVanishIntegration() {
        if (containsEnabledChild(ModuleName.INTEGRATION_SUPERVANISH)) {
            return superVanishModule.get().isHooked();
        }

        return false;
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.hasPermission("sv.see") || player.hasPermission("cmi.seevanished");
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        if (containsEnabledChild(ModuleName.INTEGRATION_LITEBANS)) {
            return liteBansModule.get().isMuted(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_ADVANCEDBAN)) {
            return advancedBanModule.get().isMuted(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_CMI)) {
            return cmiModule.get().isMuted(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_LIBERTYBANS)) {
            return libertyBansModule.get().isMuted(fPlayer);
        }

        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        if (containsEnabledChild(ModuleName.INTEGRATION_LITEBANS)) {
            return liteBansModule.get().getMute(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_ADVANCEDBAN)) {
            return advancedBanModule.get().getMute(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_CMI)) {
            return cmiModule.get().getMute(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_LIBERTYBANS)) {
            return libertyBansModule.get().getMute(fPlayer);
        }

        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(ModuleName.INTEGRATION_TRITON)) return null;

        return tritonModule.get().getLocale(fPlayer);
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        if (moduleController.isDisabledFor(this, fReceiver)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_INTERACTIVECHAT)) {
            return interactiveChatModule.get().sendMessage(fReceiver, message);
        }

        return false;
    }

    public boolean isDatapackEnabled(@NonNull String name) {
        if (!reflectionResolver.hasClass("org.bukkit.packs.DataPack")) return false;

        return reflectionResolver.isPaper()
                ? paperDatapackChecker.get().isEnabled(name)
                : bukkitDatapackChecker.get().isEnabled(name);
    }

}