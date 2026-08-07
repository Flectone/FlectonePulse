package net.flectone.pulse.module.integration;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

import java.util.Set;

/**
 * The single point every other module asks about third-party plugins, so nothing else has to know
 * which of them are installed. Each question is answered by whichever hook is available, or by a
 * harmless default when none is.
 * @author TheFaser
 */
public interface IntegrationModule extends ModuleSimple {

    @Override
    void onEnable();

    @Override
    ModuleName name();

    @Override
    Integration config();

    @Override
    Permission.Integration permission();

    /**
     * Lets the integrations rewrite mentions in a message, so a Discord ping renders as one.
     *
     * @param fPlayer who wrote it
     * @param message the text
     * @return the rewritten text
     */
    String checkMention(FEntity fPlayer, String message);

    /**
     * Whether a vanish plugin is hiding this entity.
     *
     * @param sender the entity
     * @return true if it is hidden
     */
    boolean isVanished(FEntity sender);

    /**
     * Whether any vanish plugin is installed at all.
     *
     * @return true if one is hooked
     */
    boolean hasVanishIntegration();

    /**
     * Whether this entity may see hidden players.
     *
     * @param sender the entity
     * @return true if it can see through vanish
     */
    boolean hasSeeVanishPermission(FEntity sender);

    /**
     * Hands a message to InteractiveChat so its own placeholders are expanded.
     *
     * @param fReceiver the reader
     * @param message the message
     * @return true if InteractiveChat took it
     */
    boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message);

    /**
     * Whether another moderation plugin has this player muted.
     *
     * @param fPlayer the player
     * @return true if an external mute applies
     */
    boolean isMuted(FPlayer fPlayer);

    /**
     * Whether the player joined through Geyser or Floodgate, which changes what their client can render.
     *
     * @param fPlayer the player
     * @return true if they are on Bedrock
     */
    boolean isBedrockPlayer(FEntity fPlayer);

    /**
     * The external mute applying to a player.
     *
     * @param fPlayer the player
     * @return the mute, or null if none applies
     */
    ExternalModeration getMute(FPlayer fPlayer);

    /**
     * The language Triton has selected for a player.
     *
     * @param fPlayer the player
     * @return the locale, or an empty string if Triton is not installed
     */
    String getTritonLocale(FPlayer fPlayer);

    /**
     * Whether a named integration is hooked and switched on.
     *
     * @param moduleName the integration
     * @return true if it is usable
     */
    boolean containsEnabledChild(ModuleName moduleName);

    /**
     * Whether a permission plugin grants this node, consulted when the platform cannot answer.
     *
     * @param fPlayer the player
     * @param permission the node
     * @return true if it is granted
     */
    boolean hasFPlayerPermission(FPlayer fPlayer, String permission);

    /**
     * Whether the permission plugin answers yes to everything, which means its answers carry no information.
     *
     * @return true if every node is granted
     */
    boolean isAlwaysHaveTruePermission();

    /**
     * The prefix a permission plugin gives a player.
     *
     * @param fPlayer the player
     * @return the prefix, or an empty string if none is set
     */
    String getPrefix(FPlayer fPlayer);

    /**
     * The suffix a permission plugin gives a player.
     *
     * @param fPlayer the player
     * @return the suffix, or an empty string if none is set
     */
    String getSuffix(FPlayer fPlayer);

    /**
     * Every permission group known to the server.
     *
     * @return the group names
     */
    Set<String> getGroups();

    /**
     * The weight of a player's highest group, used to decide who outranks whom.
     *
     * @param fPlayer the player
     * @return the weight
     */
    int getGroupWeight(FPlayer fPlayer);

    /**
     * Translates text through DeepL, if that integration is enabled.
     *
     * @param sender who asked
     * @param source the language to translate from
     * @param target the language to translate into
     * @param text the text
     * @return the translation, or the original text if DeepL is unavailable
     */
    String deeplTranslate(FPlayer sender, String source, String target, String text);

    /**
     * Translates text through Yandex, if that integration is enabled.
     *
     * @param sender who asked
     * @param source the language to translate from
     * @param target the language to translate into
     * @param text the text
     * @return the translation, or the original text if Yandex is unavailable
     */
    String yandexTranslate(FPlayer sender, String source, String target, String text);

}
