package net.flectone.pulse.platform.controller;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Owns the module tree. It switches modules on and off, keeps the parent-child links, and
 * answers whether a module currently applies to a given entity.
 * @author TheFaser
 */
public interface ModuleController {

    Set<Class<? extends ModuleSimple>> BAN_MODULES = Set.of(BanModule.class, BanlistModule.class, UnbanModule.class);

    Set<Class<? extends ModuleSimple>> MUTE_MODULES = Set.of(MuteModule.class, MutelistModule.class, UnmuteModule.class);

    Set<Class<? extends ModuleSimple>> WARN_MODULES = Set.of(WarnModule.class, WarnlistModule.class, UnwarnModule.class);

    Set<Class<? extends ModuleSimple>> KICK_MODULES = Set.of(KickModule.class);

    /**
     * The on/off state of every module, keyed by name, for the status command.
     *
     * @return the module states
     */
    Map<String, String> collectModuleStatuses();

    /**
     * The on/off state of one module and everything under it.
     *
     * @param clazz the module to start from
     * @return the module states
     */
    Map<String, String> collectModuleStatuses(Class<? extends ModuleSimple> clazz);

    /**
     * Whether a module is switched on.
     *
     * @param abstractModule the module
     * @return true if it is enabled
     */
    boolean isEnable(ModuleSimple abstractModule);

    /**
     * Whether a module is switched on, looked up by name.
     *
     * @param moduleName the module name
     * @return true if it is enabled
     */
    boolean isEnable(ModuleName moduleName);

    /**
     * Whether a module has the named module somewhere beneath it.
     *
     * @param module the parent module
     * @param moduleName the child name to look for
     * @return true if the child is nested under the parent
     */
    boolean containsChild(ModuleSimple module, ModuleName moduleName);

    /**
     * Whether a module does not apply to an entity, taking the config, the permission and the
     * module's own extra condition into account.
     *
     * @param module the module
     * @param fEntity the entity
     * @return true if the module is off for this entity
     */
    boolean isDisabledFor(ModuleSimple module, FEntity fEntity);

    /**
     * Whether a module does not apply to an entity, optionally also requiring its text to be available.
     *
     * @param module the module
     * @param fEntity the entity
     * @param checkLocalizationModule whether a missing localization also counts as disabled
     * @return true if the module is off for this entity
     */
    boolean isDisabledFor(ModuleSimple module, FEntity fEntity, boolean checkLocalizationModule);

    /**
     * Switches every module off, deepest first.
     */
    void terminate();

    /**
     * Switches one module and everything under it off.
     *
     * @param module the module to disable
     */
    void disable(ModuleSimple module);

    /**
     * Builds the module tree and switches on everything the config enables.
     */
    void initialize();

    /**
     * Switches a module on, then walks its children and enables those the test accepts.
     *
     * @param module the module to enable
     * @param enablePredicate decides whether a given child is enabled too
     */
    void enable(ModuleSimple module, Predicate<ModuleSimple> enablePredicate);

    /**
     * Whether a module is one of the given types, used with the grouped constants above.
     *
     * @param module the module
     * @param classes the types to test against
     * @return true if the module matches any of them
     */
    boolean isInstanceOfAny(ModuleSimple module, Set<Class<? extends ModuleSimple>> classes);

}