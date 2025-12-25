package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.libertybans.LibertyBansModule;
import net.flectone.pulse.module.integration.maintenance.MaintenanceModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.advancedban.AdvancedBanModule;
import net.flectone.pulse.module.integration.interactivechat.InteractiveChatModule;
import net.flectone.pulse.module.integration.itemsadder.ItemsAdderModule;
import net.flectone.pulse.module.integration.litebans.LiteBansModule;
import net.flectone.pulse.module.integration.minimotd.MiniMOTDModule;
import net.flectone.pulse.module.integration.miniplaceholders.MiniPlaceholdersModule;
import net.flectone.pulse.module.integration.motd.MOTDModule;
import net.flectone.pulse.module.integration.placeholderapi.PlaceholderAPIModule;
import net.flectone.pulse.module.integration.supervanish.SuperVanishModule;
import net.flectone.pulse.module.integration.tab.TABModule;
import net.flectone.pulse.module.integration.triton.TritonModule;
import net.flectone.pulse.module.integration.vault.VaultModule;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Collections;
import java.util.Set;

@Singleton
public class BukkitIntegrationModule extends IntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;

    @Inject
    public BukkitIntegrationModule(FileFacade fileFacade,
                                   FLogger fLogger,
                                   PlatformServerAdapter platformServerAdapter,
                                   ReflectionResolver reflectionResolver,
                                   Injector injector) {
        super(fileFacade, fLogger, platformServerAdapter, reflectionResolver, injector);
        
        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.fLogger = fLogger;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("AdvancedBan")) {
            builder.add(AdvancedBanModule.class);
        }

        if (platformServerAdapter.hasProject("PlaceholderAPI")) {
            builder.add(PlaceholderAPIModule.class);
        }

        if (platformServerAdapter.hasProject("Vault")) {
            builder.add(VaultModule.class);
        }

        if (platformServerAdapter.hasProject("InteractiveChat")) {
            if (reflectionResolver.hasClass("com.loohp.interactivechat.registry.Registry")) {
                builder.add(InteractiveChatModule.class);
            } else {
                fLogger.warning("Update InteractiveChat to the latest version");
            }
        }

        if (platformServerAdapter.hasProject("ItemsAdder")) {
            builder.add(ItemsAdderModule.class);
        }

        if (platformServerAdapter.hasProject("LibertyBans")) {
            builder.add(LibertyBansModule.class);
        }

        if (platformServerAdapter.hasProject("LiteBans")) {
            builder.add(LiteBansModule.class);
        }

        if (platformServerAdapter.hasProject("Maintenance")) {
            builder.add(MaintenanceModule.class);
        }

        if (platformServerAdapter.hasProject("MiniMOTD")) {
            builder.add(MiniMOTDModule.class);
        }

        if (platformServerAdapter.hasProject("MiniPlaceholders")) {
            builder.add(MiniPlaceholdersModule.class);
        }

        if (platformServerAdapter.hasProject("MOTD")) {
            builder.add(MOTDModule.class);
        }

        if (platformServerAdapter.hasProject("SuperVanish") || platformServerAdapter.hasProject("PremiumVanish")) {
            if (reflectionResolver.hasClass("de.myzelyam.api.vanish.VanishAPI")) {
                builder.add(SuperVanishModule.class);
            } else {
                fLogger.warning("Integration with SuperVanish is not possible. Are you using another plugin with the same name? It is only supported https://www.spigotmc.org/resources/supervanish-be-invisible.1331/");
            }
        }

        if (platformServerAdapter.hasProject("TAB")) {
            builder.add(TABModule.class);
        }

        if (platformServerAdapter.hasProject("Triton")) {
            builder.add(TritonModule.class);
        }

        return builder;
    }

    @Override
    public String checkMention(FEntity fSender, String message) {
        if (isModuleDisabledFor(fSender)) return message;

        if (containsChild(InteractiveChatModule.class)) {
            return getInstance(InteractiveChatModule.class).checkMention(fSender, message);
        }

        return message;
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        boolean value = super.hasFPlayerPermission(fPlayer, permission);

        if (containsChild(VaultModule.class)) {
            value = value && getInstance(VaultModule.class).hasVaultPermission(fPlayer, permission);
        }

        return value;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        String prefix = super.getPrefix(fPlayer);
        if (prefix != null) return prefix;

        if (containsChild(VaultModule.class)) {
            return getInstance(VaultModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        String suffix = super.getSuffix(fPlayer);
        if (suffix != null) return suffix;

        if (containsChild(VaultModule.class)) {
            return getInstance(VaultModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> groups = super.getGroups();
        if (!groups.isEmpty()) return groups;

        if (containsChild(VaultModule.class)) {
            return getInstance(VaultModule.class).getGroups();
        }

        return Collections.emptySet();
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (containsChild(SuperVanishModule.class)) {
            return getInstance(SuperVanishModule.class).isVanished(sender);
        }

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return false;

        return player.getMetadata("vanished")
                .stream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return false;

        return player.hasPermission("sv.see") || player.hasPermission("cmi.seevanished");
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        if (containsChild(LiteBansModule.class)) {
            return getInstance(LiteBansModule.class).isMuted(fPlayer);
        }

        if (containsChild(AdvancedBanModule.class)) {
            return getInstance(AdvancedBanModule.class).isMuted(fPlayer);
        }

        if (containsChild(LibertyBansModule.class)) {
            return getInstance(LibertyBansModule.class).isMuted(fPlayer);
        }

        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        if (containsChild(LiteBansModule.class)) {
            return getInstance(LiteBansModule.class).getMute(fPlayer);
        }

        if (containsChild(AdvancedBanModule.class)) {
            return getInstance(AdvancedBanModule.class).getMute(fPlayer);
        }

        if (containsChild(LibertyBansModule.class)) {
            return getInstance(LibertyBansModule.class).getMute(fPlayer);
        }

        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        if (!isEnable()) return null;
        if (!containsChild(TritonModule.class)) return null;

        return getInstance(TritonModule.class).getLocale(fPlayer);
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        if (isModuleDisabledFor(fReceiver)) return false;

        if (containsChild(InteractiveChatModule.class)) {
            return getInstance(InteractiveChatModule.class).sendMessage(fReceiver, message);
        }

        return false;
    }
}
