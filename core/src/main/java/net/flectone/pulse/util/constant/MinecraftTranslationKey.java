package net.flectone.pulse.util.constant;

import net.kyori.adventure.text.TranslatableComponent;

import java.util.Arrays;

public enum MinecraftTranslationKey {

    ADV_MODE_NOT_ENABLED("advMode.notEnabled"),
    ADV_MODE_SET_COMMAND_SUCCESS("advMode.setCommand.success"),

    BLOCK_MINECRAFT_BED_NO_SLEEP("block.minecraft.bed.no_sleep"),
    BLOCK_MINECRAFT_BED_NOT_SAFE("block.minecraft.bed.not_safe"),
    BLOCK_MINECRAFT_BED_OBSTRUCTED("block.minecraft.bed.obstructed"),
    BLOCK_MINECRAFT_BED_OCCUPIED("block.minecraft.bed.occupied"),
    BLOCK_MINECRAFT_BED_TOO_FAR_AWAY("block.minecraft.bed.too_far_away"),
    TILE_BED_NO_SLEEP("tile.bed.noSleep"), // legacy
    TILE_BED_NOT_SAFE("tile.bed.notSafe"), // legacy
    TILE_BED_NOT_VALID("tile.bed.notValid"), // legacy
    TILE_BED_OCCUPIED("tile.bed.occupied"), // legacy

    BLOCK_MINECRAFT_SET_SPAWN("block.minecraft.set_spawn"),
    BLOCK_MINECRAFT_SPAWN_NOT_VALID("block.minecraft.spawn.not_valid"),

    CHAT_TYPE_ADVANCEMENT_CHALLENGE("chat.type.advancement.challenge"),
    CHAT_TYPE_ADVANCEMENT_GOAL("chat.type.advancement.goal"),
    CHAT_TYPE_ADVANCEMENT_TASK("chat.type.advancement.task"),
    CHAT_TYPE_ACHIEVEMENT("chat.type.achievement"), // legacy, < 1.12
    CHAT_TYPE_ACHIEVEMENT_TAKEN("chat.type.achievement.taken"), // legacy < 1.12

    COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS("commands.advancement.grant.many.to.one.success"),
    COMMANDS_ADVANCEMENT_GRANT_MANY_TO_MANY_SUCCESS("commands.advancement.grant.many.to.many.success"),
    COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS("commands.advancement.grant.one.to.one.success"),
    COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS("commands.advancement.revoke.many.to.one.success"),
    COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_MANY_SUCCESS("commands.advancement.revoke.many.to.many.success"),
    COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS("commands.advancement.revoke.one.to.one.success"),
    COMMANDS_ADVANCEMENT_GRANT_ONLY_SUCCESS("commands.advancement.grant.only.success"), // legacy, 1.12
    COMMANDS_ADVANCEMENT_GRANT_EVERYTHING_SUCCESS("commands.advancement.grant.everything.success"), // legacy, 1.12
    COMMANDS_ADVANCEMENT_REVOKE_ONLY_SUCCESS("commands.advancement.revoke.only.success"), // legacy, 1.12
    COMMANDS_ADVANCEMENT_REVOKE_EVERYTHING_SUCCESS("commands.advancement.revoke.everything.success"), // legacy, 1.12
    COMMANDS_ACHIEVEMENT_GIVE_ONE("commands.achievement.give.success.one"), // legacy, < 1.12
    COMMANDS_ACHIEVEMENT_GIVE_MANY("commands.achievement.give.success.many"), // legacy, < 1.12
    COMMANDS_ACHIEVEMENT_TAKE_ONE("commands.achievement.take.success.one"), // legacy, < 1.12
    COMMANDS_ACHIEVEMENT_TAKE_MANY("commands.achievement.take.success.many"), // legacy, < 1.12

    COMMANDS_ATTRIBUTE_BASE_VALUE_GET_SUCCESS("commands.attribute.base_value.get.success"),
    COMMANDS_ATTRIBUTE_BASE_VALUE_RESET_SUCCESS("commands.attribute.base_value.reset.success"),
    COMMANDS_ATTRIBUTE_BASE_VALUE_SET_SUCCESS("commands.attribute.base_value.set.success"),
    COMMANDS_ATTRIBUTE_MODIFIER_ADD_SUCCESS("commands.attribute.modifier.add.success"),
    COMMANDS_ATTRIBUTE_MODIFIER_REMOVE_SUCCESS("commands.attribute.modifier.remove.success"),
    COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS("commands.attribute.modifier.value.get.success"),
    COMMANDS_ATTRIBUTE_VALUE_GET_SUCCESS("commands.attribute.value.get.success"),

    COMMANDS_CLEAR_SUCCESS_MULTIPLE("commands.clear.success.multiple"),
    COMMANDS_CLEAR_SUCCESS_SINGLE("commands.clear.success.single"),
    COMMANDS_CLEAR_SUCCESS("commands.clear.success"), // legacy

    COMMANDS_CLONE_SUCCESS("commands.clone.success"),

    COMMANDS_DAMAGE_SUCCESS("commands.damage.success"),

    COMMANDS_DEOP_SUCCESS("commands.deop.success"),

    COMMANDS_DIFFICULTY_QUERY("commands.difficulty.query"),
    COMMANDS_DIFFICULTY_SUCCESS("commands.difficulty.success"),

    COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE("commands.effect.clear.everything.success.multiple"),
    COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE("commands.effect.clear.everything.success.single"),
    COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE("commands.effect.clear.specific.success.multiple"),
    COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE("commands.effect.clear.specific.success.single"),
    COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE("commands.effect.give.success.multiple"),
    COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE("commands.effect.give.success.single"),

    COMMANDS_ENCHANT_SUCCESS_MULTIPLE("commands.enchant.success.multiple"),
    COMMANDS_ENCHANT_SUCCESS_SINGLE("commands.enchant.success.single"),
    COMMANDS_ENCHANT_SUCCESS("commands.enchant.success"), // legacy

    COMMANDS_EXECUTE_CONDITIONAL_PASS("commands.execute.conditional.pass"),
    COMMANDS_EXECUTE_CONDITIONAL_PASS_COUNT("commands.execute.conditional.pass_count"),

    COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE("commands.experience.add.levels.success.multiple"),
    COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE("commands.experience.add.levels.success.single"),
    COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE("commands.experience.add.points.success.multiple"),
    COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE("commands.experience.add.points.success.single"),
    COMMANDS_EXPERIENCE_QUERY_LEVELS("commands.experience.query.levels"),
    COMMANDS_EXPERIENCE_QUERY_POINTS("commands.experience.query.points"),
    COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE("commands.experience.set.levels.success.multiple"),
    COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE("commands.experience.set.levels.success.single"),
    COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE("commands.experience.set.points.success.multiple"),
    COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE("commands.experience.set.points.success.single"),

    COMMANDS_FILL_SUCCESS("commands.fill.success"),

    COMMANDS_FILLBIOME_SUCCESS("commands.fillbiome.success"),
    COMMANDS_FILLBIOME_SUCCESS_COUNT("commands.fillbiome.success.count"),

    COMMANDS_DEFAULTGAMEMODE_SUCCESS("commands.defaultgamemode.success"),
    COMMANDS_GAMEMODE_SUCCESS_OTHER("commands.gamemode.success.other"),
    COMMANDS_GAMEMODE_SUCCESS_SELF("commands.gamemode.success.self"),
    GAMEMODE_CHANGED("gameMode.changed"),

    COMMANDS_GAMERULE_QUERY("commands.gamerule.query"),
    COMMANDS_GAMERULE_SET("commands.gamerule.set"),

    COMMANDS_GIVE_SUCCESS_MULTIPLE("commands.give.success.multiple"),
    COMMANDS_GIVE_SUCCESS_SINGLE("commands.give.success.single"),

    COMMANDS_KILL_SUCCESS_MULTIPLE("commands.kill.success.multiple"),
    COMMANDS_KILL_SUCCESS_SINGLE("commands.kill.success.single"),
    COMMANDS_KILL_SUCCESS("commands.kill.successful"), // legacy

    COMMANDS_OP_SUCCESS("commands.op.success"),

    COMMANDS_PARTICLE_SUCCESS("commands.particle.success"),

    COMMANDS_RELOAD_SUCCESS("commands.reload.success"),

    COMMANDS_RIDE_DISMOUNT_SUCCESS("commands.ride.dismount.success"),
    COMMANDS_RIDE_MOUNT_SUCCESS("commands.ride.mount.success"),

    COMMANDS_ROTATE_SUCCESS("commands.rotate.success"),

    COMMANDS_SAVE_DISABLED("commands.save.disabled"),
    COMMANDS_SAVE_ENABLED("commands.save.enabled"),
    COMMANDS_SAVE_SAVING("commands.save.saving"),
    COMMANDS_SAVE_SUCCESS("commands.save.success"),

    COMMANDS_SEED_SUCCESS("commands.seed.success"),
    COMMANDS_SETBLOCK_SUCCESS("commands.setblock.success"),

    COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE("commands.spawnpoint.success.multiple"),
    COMMANDS_SPAWNPOINT_SUCCESS_SINGLE("commands.spawnpoint.success.single"),
    COMMANDS_SPAWNPOINT_SUCCESS("commands.spawnpoint.success"), // legacy
    COMMANDS_SETWORLDSPAWN_SUCCESS("commands.setworldspawn.success"),

    COMMANDS_STOP_SUCCESS("commands.stop.stopping"),

    COMMANDS_SUMMON_SUCCESS("commands.summon.success"),

    COMMANDS_TELEPORT_SUCCESS_ENTITY_MULTIPLE("commands.teleport.success.entity.multiple"),
    COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE("commands.teleport.success.entity.single"),
    COMMANDS_TELEPORT_SUCCESS_LOCATION_MULTIPLE("commands.teleport.success.location.multiple"),
    COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE("commands.teleport.success.location.single"),

    COMMANDS_TIME_QUERY("commands.time.query"),
    COMMANDS_TIME_SET("commands.time.set"),

    DEATH_ATTACK_ANVIL("death.attack.anvil"),
    DEATH_ATTACK_ANVIL_PLAYER("death.attack.anvil.player"),
    DEATH_ATTACK_ARROW("death.attack.arrow"),
    DEATH_ATTACK_ARROW_ITEM("death.attack.arrow.item"),
    DEATH_ATTACK_BAD_RESPAWN_POINT_MESSAGE("death.attack.badRespawnPoint.message"),
    DEATH_ATTACK_CACTUS("death.attack.cactus"),
    DEATH_ATTACK_CACTUS_PLAYER("death.attack.cactus.player"),
    DEATH_ATTACK_CRAMMING("death.attack.cramming"),
    DEATH_ATTACK_CRAMMING_PLAYER("death.attack.cramming.player"),
    DEATH_ATTACK_DRAGON_BREATH("death.attack.dragonBreath"),
    DEATH_ATTACK_DRAGON_BREATH_PLAYER("death.attack.dragonBreath.player"),
    DEATH_ATTACK_DROWN("death.attack.drown"),
    DEATH_ATTACK_DROWN_PLAYER("death.attack.drown.player"),
    DEATH_ATTACK_DRYOUT("death.attack.dryout"),
    DEATH_ATTACK_DRYOUT_PLAYER("death.attack.dryout.player"),
    DEATH_ATTACK_EVEN_MORE_MAGIC("death.attack.even_more_magic"),
    DEATH_ATTACK_EXPLOSION("death.attack.explosion"),
    DEATH_ATTACK_EXPLOSION_PLAYER("death.attack.explosion.player"),
    DEATH_ATTACK_EXPLOSION_ITEM("death.attack.explosion.item"),
    DEATH_ATTACK_EXPLOSION_PLAYER_ITEM("death.attack.explosion.player.item"),
    DEATH_ATTACK_FALL("death.attack.fall"),
    DEATH_ATTACK_FALL_PLAYER("death.attack.fall.player"),
    DEATH_ATTACK_FALLING_BLOCK("death.attack.fallingBlock"),
    DEATH_ATTACK_FALLING_BLOCK_PLAYER("death.attack.fallingBlock.player"),
    DEATH_ATTACK_FALLING_STALACTITE("death.attack.fallingStalactite"),
    DEATH_ATTACK_FALLING_STALACTITE_PLAYER("death.attack.fallingStalactite.player"),
    DEATH_ATTACK_FIREBALL("death.attack.fireball"),
    DEATH_ATTACK_FIREBALL_ITEM("death.attack.fireball.item"),
    DEATH_ATTACK_FIREWORKS("death.attack.fireworks"),
    DEATH_ATTACK_FIREWORKS_ITEM("death.attack.fireworks.item"),
    DEATH_ATTACK_FIREWORKS_PLAYER("death.attack.fireworks.player"),
    DEATH_ATTACK_FLY_INTO_WALL("death.attack.flyIntoWall"),
    DEATH_ATTACK_FLY_INTO_WALL_PLAYER("death.attack.flyIntoWall.player"),
    DEATH_ATTACK_FREEZE("death.attack.freeze"),
    DEATH_ATTACK_FREEZE_PLAYER("death.attack.freeze.player"),
    DEATH_ATTACK_GENERIC("death.attack.generic"),
    DEATH_ATTACK_GENERIC_PLAYER("death.attack.generic.player"),
    DEATH_ATTACK_GENERIC_KILL("death.attack.genericKill"),
    DEATH_ATTACK_GENERIC_KILL_PLAYER("death.attack.genericKill.player"),
    DEATH_ATTACK_HOT_FLOOR("death.attack.hotFloor"),
    DEATH_ATTACK_HOT_FLOOR_PLAYER("death.attack.hotFloor.player"),
    DEATH_ATTACK_INDIRECT_MAGIC("death.attack.indirectMagic"),
    DEATH_ATTACK_INDIRECT_MAGIC_ITEM("death.attack.indirectMagic.item"),
    DEATH_ATTACK_IN_FIRE("death.attack.inFire"),
    DEATH_ATTACK_IN_FIRE_PLAYER("death.attack.inFire.player"),
    DEATH_ATTACK_IN_WALL("death.attack.inWall"),
    DEATH_ATTACK_IN_WALL_PLAYER("death.attack.inWall.player"),
    DEATH_ATTACK_LAVA("death.attack.lava"),
    DEATH_ATTACK_LAVA_PLAYER("death.attack.lava.player"),
    DEATH_ATTACK_LIGHTNING_BOLT("death.attack.lightningBolt"),
    DEATH_ATTACK_LIGHTNING_BOLT_PLAYER("death.attack.lightningBolt.player"),
    DEATH_ATTACK_MACE_SMASH("death.attack.mace_smash"),
    DEATH_ATTACK_MACE_SMASH_ITEM("death.attack.mace_smash.item"),
    DEATH_ATTACK_MAGIC("death.attack.magic"),
    DEATH_ATTACK_MAGIC_PLAYER("death.attack.magic.player"),
    DEATH_ATTACK_MOB("death.attack.mob"),
    DEATH_ATTACK_MOB_ITEM("death.attack.mob.item"),
    DEATH_ATTACK_ON_FIRE("death.attack.onFire"),
    DEATH_ATTACK_ON_FIRE_ITEM("death.attack.onFire.item"),
    DEATH_ATTACK_ON_FIRE_PLAYER("death.attack.onFire.player"),
    DEATH_ATTACK_OUT_OF_WORLD("death.attack.outOfWorld"),
    DEATH_ATTACK_OUT_OF_WORLD_PLAYER("death.attack.outOfWorld.player"),
    DEATH_ATTACK_OUTSIDE_BORDER("death.attack.outsideBorder"),
    DEATH_ATTACK_OUTSIDE_BORDER_PLAYER("death.attack.outsideBorder.player"),
    DEATH_ATTACK_PLAYER("death.attack.player"),
    DEATH_ATTACK_PLAYER_ITEM("death.attack.player.item"),
    DEATH_ATTACK_SONIC_BOOM("death.attack.sonic_boom"),
    DEATH_ATTACK_SONIC_BOOM_ITEM("death.attack.sonic_boom.item"),
    DEATH_ATTACK_SONIC_BOOM_PLAYER("death.attack.sonic_boom.player"),
    DEATH_ATTACK_STALAGMITE("death.attack.stalagmite"),
    DEATH_ATTACK_STALAGMITE_PLAYER("death.attack.stalagmite.player"),
    DEATH_ATTACK_STARVE("death.attack.starve"),
    DEATH_ATTACK_STARVE_PLAYER("death.attack.starve.player"),
    DEATH_ATTACK_STING("death.attack.sting"),
    DEATH_ATTACK_STING_ITEM("death.attack.sting.item"),
    DEATH_ATTACK_STING_PLAYER("death.attack.sting.player"),
    DEATH_ATTACK_SWEET_BERRY_BUSH("death.attack.sweetBerryBush"),
    DEATH_ATTACK_SWEET_BERRY_BUSH_PLAYER("death.attack.sweetBerryBush.player"),
    DEATH_ATTACK_THORNS("death.attack.thorns"),
    DEATH_ATTACK_THORNS_ITEM("death.attack.thorns.item"),
    DEATH_ATTACK_THROWN("death.attack.thrown"),
    DEATH_ATTACK_THROWN_ITEM("death.attack.thrown.item"),
    DEATH_ATTACK_TRIDENT("death.attack.trident"),
    DEATH_ATTACK_TRIDENT_ITEM("death.attack.trident.item"),
    DEATH_ATTACK_WITHER("death.attack.wither"),
    DEATH_ATTACK_WITHER_PLAYER("death.attack.wither.player"),
    DEATH_ATTACK_WITHER_SKULL("death.attack.witherSkull"),
    DEATH_ATTACK_WITHER_SKULL_ITEM("death.attack.witherSkull.item"),
    DEATH_FELL_ACCIDENT_GENERIC("death.fell.accident.generic"),
    DEATH_FELL_ACCIDENT_LADDER("death.fell.accident.ladder"),
    DEATH_FELL_ACCIDENT_OTHER_CLIMBABLE("death.fell.accident.other_climbable"),
    DEATH_FELL_ACCIDENT_SCAFFOLDING("death.fell.accident.scaffolding"),
    DEATH_FELL_ACCIDENT_TWISTING_VINES("death.fell.accident.twisting_vines"),
    DEATH_FELL_ACCIDENT_VINES("death.fell.accident.vines"),
    DEATH_FELL_ACCIDENT_WEEPING_VINES("death.fell.accident.weeping_vines"),
    DEATH_FELL_ASSIST("death.fell.assist"),
    DEATH_FELL_ASSIST_ITEM("death.fell.assist.item"),
    DEATH_FELL_FINISH("death.fell.finish"),
    DEATH_FELL_FINISH_ITEM("death.fell.finish.item"),
    DEATH_FELL_KILLER("death.fell.killer"),

    ITEM_MINECRAFT_DEBUG_STICK_EMPTY("item.minecraft.debug_stick.empty"),
    ITEM_MINECRAFT_DEBUG_STICK_SELECT("item.minecraft.debug_stick.select"),
    ITEM_MINECRAFT_DEBUG_STICK_UPDATE("item.minecraft.debug_stick.update"),

    MULTIPLAYER_PLAYER_JOINED("multiplayer.player.joined"),
    MULTIPLAYER_PLAYER_JOINED_RENAMED("multiplayer.player.joined.renamed"),
    MULTIPLAYER_PLAYER_LEFT("multiplayer.player.left"),
    MULTIPLAYER_MESSAGE_NOT_DELIVERED("multiplayer.message_not_delivered"),

    SLEEP_NOT_POSSIBLE("sleep.not_possible"),
    SLEEP_PLAYERS_SLEEPING("sleep.players_sleeping"),
    SLEEP_SKIPPING_NIGHT("sleep.skipping_night"),

    COMMANDS_WEATHER_SET_CLEAR("commands.weather.set.clear"),
    COMMANDS_WEATHER_SET_RAIN("commands.weather.set.rain"),
    COMMANDS_WEATHER_SET_THUNDER("commands.weather.set.thunder"),

    UNKNOWN("unknown");

    private final String key;

    MinecraftTranslationKey(String key) {
        this.key = key;
    }

    public static MinecraftTranslationKey fromString(TranslatableComponent component) {
        return fromString(component.key());
    }

    public static MinecraftTranslationKey fromString(String string) {
        return Arrays.stream(MinecraftTranslationKey.values())
                .filter(type -> type.key.equalsIgnoreCase(string))
                .findAny()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return key;
    }

    public boolean equals(String string) {
        return this.key.equals(string);
    }

    public boolean startsWith(String string) {
        return this.key.startsWith(string);
    }

}
