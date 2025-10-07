package net.flectone.pulse.util.constant;

import net.kyori.adventure.text.TranslatableComponent;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MinecraftTranslationKey {

    ADV_MODE_NOT_ENABLED("advMode.notEnabled"), // 1.8+
    ADV_MODE_SET_COMMAND_SUCCESS("advMode.setCommand.success"), // 1.8+

    BLOCK_MINECRAFT_BED_NO_SLEEP("block.minecraft.bed.no_sleep"), // 1.13+
    BLOCK_MINECRAFT_BED_NOT_SAFE("block.minecraft.bed.not_safe"), // 1.13+
    BLOCK_MINECRAFT_BED_OBSTRUCTED("block.minecraft.bed.obstructed"), // 1.13+
    BLOCK_MINECRAFT_BED_OCCUPIED("block.minecraft.bed.occupied"), // 1.13+
    BLOCK_MINECRAFT_BED_TOO_FAR_AWAY("block.minecraft.bed.too_far_away"), // 1.13+
    TILE_BED_NO_SLEEP("tile.bed.noSleep"), // legacy 1.8-1.12
    TILE_BED_NOT_SAFE("tile.bed.notSafe"), // legacy 1.8-1.12
    TILE_BED_NOT_VALID("tile.bed.notValid"), // legacy 1.8-1.12
    TILE_BED_OCCUPIED("tile.bed.occupied"), // legacy 1.8-1.12

    BLOCK_MINECRAFT_SET_SPAWN("block.minecraft.set_spawn"), // 1.17+
    BLOCK_MINECRAFT_SPAWN_NOT_VALID("block.minecraft.spawn.not_valid"), // 1.17+
    BLOCK_MINECRAFT_BED_SET_SPAWN("block.minecraft.bed.set_spawn"), // 1.13-1.16
    BLOCK_MINECRAFT_BED_NOT_VALID("block.minecraft.bed.not_valid"), // 1.13-1.16

    CHAT_TYPE_ADVANCEMENT_CHALLENGE("chat.type.advancement.challenge"), // 1.12+
    CHAT_TYPE_ADVANCEMENT_GOAL("chat.type.advancement.goal"), // 1.12+
    CHAT_TYPE_ADVANCEMENT_TASK("chat.type.advancement.task"), // 1.12+
    CHAT_TYPE_ACHIEVEMENT("chat.type.achievement"), // legacy 1.8-1.11
    CHAT_TYPE_ACHIEVEMENT_TAKEN("chat.type.achievement.taken"), // legacy 1.8-1.11

    COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_MANY_SUCCESS("commands.advancement.grant.criterion.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_ONE_SUCCESS("commands.advancement.grant.criterion.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS("commands.advancement.grant.many.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_ONE_TO_MANY_SUCCESS("commands.advancement.grant.one.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_MANY_TO_MANY_SUCCESS("commands.advancement.grant.many.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS("commands.advancement.grant.one.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS("commands.advancement.revoke.many.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_MANY_SUCCESS("commands.advancement.revoke.one.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_MANY_SUCCESS("commands.advancement.revoke.many.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS("commands.advancement.revoke.one.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_MANY_SUCCESS("commands.advancement.revoke.criterion.to.many.success"), // 1.13+
    COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_ONE_SUCCESS("commands.advancement.revoke.criterion.to.one.success"), // 1.13+
    COMMANDS_ADVANCEMENT_GRANT_ONLY_SUCCESS("commands.advancement.grant.only.success"), // 1.12-only
    COMMANDS_ADVANCEMENT_GRANT_EVERYTHING_SUCCESS("commands.advancement.grant.everything.success"), // 1.12-only
    COMMANDS_ADVANCEMENT_REVOKE_ONLY_SUCCESS("commands.advancement.revoke.only.success"), // 1.12-only
    COMMANDS_ADVANCEMENT_REVOKE_EVERYTHING_SUCCESS("commands.advancement.revoke.everything.success"), // 1.12-only
    COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ONE("commands.achievement.give.success.one"), // legacy 1.8-1.11
    COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ALL("commands.achievement.give.success.all"), // legacy 1.8-1.11
    COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ONE("commands.achievement.take.success.one"), // legacy 1.8-1.11
    COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ALL("commands.achievement.take.success.all"), // legacy 1.8-1.11

    COMMANDS_ATTRIBUTE_BASE_VALUE_GET_SUCCESS("commands.attribute.base_value.get.success"), // 1.16+
    COMMANDS_ATTRIBUTE_BASE_VALUE_RESET_SUCCESS("commands.attribute.base_value.reset.success"), // 1.16+
    COMMANDS_ATTRIBUTE_BASE_VALUE_SET_SUCCESS("commands.attribute.base_value.set.success"), // 1.16+
    COMMANDS_ATTRIBUTE_MODIFIER_ADD_SUCCESS("commands.attribute.modifier.add.success"), // 1.16+
    COMMANDS_ATTRIBUTE_MODIFIER_REMOVE_SUCCESS("commands.attribute.modifier.remove.success"), // 1.16+
    COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS("commands.attribute.modifier.value.get.success"), // 1.16+
    COMMANDS_ATTRIBUTE_VALUE_GET_SUCCESS("commands.attribute.value.get.success"), // 1.16+

    COMMANDS_CLEAR_SUCCESS_MULTIPLE("commands.clear.success.multiple"), // 1.13+
    COMMANDS_CLEAR_SUCCESS_SINGLE("commands.clear.success.single"), // 1.13+
    COMMANDS_CLEAR_SUCCESS("commands.clear.success"), // legacy 1.8-1.12

    COMMANDS_CLONE_SUCCESS("commands.clone.success"), // 1.8+

    COMMANDS_DAMAGE_SUCCESS("commands.damage.success"), // 1.14+

    COMMANDS_DEOP_SUCCESS("commands.deop.success"), // 1.8+

    COMMANDS_DIALOG_CLEAR_MULTIPLE("commands.dialog.clear.multiple"), // 1.20.5+
    COMMANDS_DIALOG_CLEAR_SINGLE("commands.dialog.clear.single"), // 1.20.5+
    COMMANDS_DIALOG_SHOW_MULTIPLE("commands.dialog.show.multiple"), // 1.20.5+
    COMMANDS_DIALOG_SHOW_SINGLE("commands.dialog.show.single"), // 1.20.5+

    COMMANDS_DIFFICULTY_QUERY("commands.difficulty.query"), // 1.13+
    COMMANDS_DIFFICULTY_SUCCESS("commands.difficulty.success"), // 1.8+

    COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE("commands.effect.clear.everything.success.multiple"), // 1.13+
    COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE("commands.effect.clear.everything.success.single"), // 1.13+
    COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE("commands.effect.clear.specific.success.multiple"), // 1.13+
    COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE("commands.effect.clear.specific.success.single"), // 1.13+
    COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE("commands.effect.give.success.multiple"), // 1.13+
    COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE("commands.effect.give.success.single"), // 1.13+
    COMMANDS_EFFECT_SUCCESS("commands.effect.success"), // legacy 1.8-1.12
    COMMANDS_EFFECT_SUCCESS_REMOVED("commands.effect.success.removed"), // legacy 1.8-1.12
    COMMANDS_EFFECT_SUCCESS_REMOVED_ALL("commands.effect.success.removed.all"), // legacy 1.8-1.12

    COMMANDS_ENCHANT_SUCCESS_MULTIPLE("commands.enchant.success.multiple"), // 1.13+
    COMMANDS_ENCHANT_SUCCESS_SINGLE("commands.enchant.success.single"), // 1.13+
    COMMANDS_ENCHANT_SUCCESS("commands.enchant.success"), // legacy 1.8-1.12

    COMMANDS_EXECUTE_CONDITIONAL_PASS("commands.execute.conditional.pass"), // 1.19+
    COMMANDS_EXECUTE_CONDITIONAL_PASS_COUNT("commands.execute.conditional.pass_count"), // 1.19+

    COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE("commands.experience.add.levels.success.multiple"), // 1.13+
    COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE("commands.experience.add.levels.success.single"), // 1.13+
    COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE("commands.experience.add.points.success.multiple"), // 1.13+
    COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE("commands.experience.add.points.success.single"), // 1.13+
    COMMANDS_EXPERIENCE_QUERY_LEVELS("commands.experience.query.levels"), // 1.13+
    COMMANDS_EXPERIENCE_QUERY_POINTS("commands.experience.query.points"), // 1.13+
    COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE("commands.experience.set.levels.success.multiple"), // 1.13+
    COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE("commands.experience.set.levels.success.single"), // 1.13+
    COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE("commands.experience.set.points.success.multiple"), // 1.13+
    COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE("commands.experience.set.points.success.single"), // 1.13+
    COMMANDS_XP_SUCCESS("commands.xp.success"), // legacy 1.8-1.12
    COMMANDS_XP_SUCCESS_LEVELS("commands.xp.success.levels"), // legacy 1.8-1.12
    COMMANDS_XP_SUCCESS_NEGATIVE_LEVELS("commands.xp.success.negative.levels"), // legacy 1.8-1.12

    COMMANDS_FILL_SUCCESS("commands.fill.success"), // 1.8+

    COMMANDS_FILLBIOME_SUCCESS("commands.fillbiome.success"), // 1.19.4+
    COMMANDS_FILLBIOME_SUCCESS_COUNT("commands.fillbiome.success.count"), // 1.19.4+

    COMMANDS_DEFAULTGAMEMODE_SUCCESS("commands.defaultgamemode.success"), // 1.13+
    COMMANDS_GAMEMODE_SUCCESS_OTHER("commands.gamemode.success.other"), // 1.13+
    COMMANDS_GAMEMODE_SUCCESS_SELF("commands.gamemode.success.self"), // 1.13+
    GAMEMODE_CHANGED("gameMode.changed"), // 1.8+

    COMMANDS_GAMERULE_QUERY("commands.gamerule.query"), // 1.13+
    COMMANDS_GAMERULE_SET("commands.gamerule.set"), // 1.13+
    COMMANDS_GAMERULE_SUCCESS("commands.gamerule.success"), // legacy 1.8-1.12

    COMMANDS_GIVE_SUCCESS_MULTIPLE("commands.give.success.multiple"), // 1.13+
    COMMANDS_GIVE_SUCCESS_SINGLE("commands.give.success.single"), // 1.13+
    COMMANDS_GIVE_SUCCESS("commands.give.success"), // legacy 1.8-1.12

    COMMANDS_KILL_SUCCESS_MULTIPLE("commands.kill.success.multiple"), // 1.13+
    COMMANDS_KILL_SUCCESS_SINGLE("commands.kill.success.single"), // 1.13+
    COMMANDS_KILL_SUCCESSFUL("commands.kill.successful"), // legacy 1.8-1.12

    COMMANDS_LOCATE_BIOME_SUCCESS("commands.locate.biome.success"), // 1.16+
    COMMANDS_LOCATE_POI_SUCCESS("commands.locate.poi.success"), // 1.14+
    COMMANDS_LOCATE_STRUCTURE_SUCCESS("commands.locate.structure.success"), // 1.13+
    COMMANDS_LOCATE_SUCCESS("commands.locate.success"), // 1.11-1.16
    COMMANDS_LOCATEBIOME_SUCCESS("commands.locatebiome.success"), // 1.16-1.18

    COMMANDS_OP_SUCCESS("commands.op.success"), // 1.8+

    COMMANDS_PARTICLE_SUCCESS("commands.particle.success"), // 1.8+

    COMMANDS_RECIPE_GIVE_SUCCESS_MULTIPLE("commands.recipe.give.success.multiple"), // 1.13+
    COMMANDS_RECIPE_GIVE_SUCCESS_SINGLE("commands.recipe.give.success.single"), // 1.13+
    COMMANDS_RECIPE_TAKE_SUCCESS_MULTIPLE("commands.recipe.take.success.multiple"), // 1.13+
    COMMANDS_RECIPE_TAKE_SUCCESS_SINGLE("commands.recipe.take.success.single"), // 1.13+

    COMMANDS_RELOAD_SUCCESS("commands.reload.success"), // 1.13+

    COMMANDS_RIDE_DISMOUNT_SUCCESS("commands.ride.dismount.success"), // 1.20.2+
    COMMANDS_RIDE_MOUNT_SUCCESS("commands.ride.mount.success"), // 1.20.2+

    COMMANDS_ROTATE_SUCCESS("commands.rotate.success"), // 1.20.2+

    COMMANDS_SAVE_DISABLED("commands.save.disabled"), // 1.13+
    COMMANDS_SAVE_ENABLED("commands.save.enabled"), // 1.13+
    COMMANDS_SAVE_START("commands.save.start"), // legacy 1.8-1.12
    COMMANDS_SAVE_SAVING("commands.save.saving"), // 1.13+
    COMMANDS_SAVE_SUCCESS("commands.save.success"), // 1.13+

    COMMANDS_SEED_SUCCESS("commands.seed.success"), // 1.8+
    COMMANDS_SETBLOCK_SUCCESS("commands.setblock.success"), // 1.8+

    COMMANDS_PLAYSOUND_SUCCESS_MULTIPLE("commands.playsound.success.multiple"), // 1.13+
    COMMANDS_PLAYSOUND_SUCCESS_SINGLE("commands.playsound.success.single"), // 1.13+
    COMMANDS_PLAYSOUND_SUCCESS("commands.playsound.success"), // legacy 1.8-1.12
    COMMANDS_STOPSOUND_SUCCESS_SOURCE_ANY("commands.stopsound.success.source.any"), // 1.13+
    COMMANDS_STOPSOUND_SUCCESS_SOURCE_SOUND("commands.stopsound.success.source.sound"), // 1.13+
    COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_ANY("commands.stopsound.success.sourceless.any"), // 1.13+
    COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_SOUND("commands.stopsound.success.sourceless.sound"), // 1.13+
    COMMANDS_STOPSOUND_SUCCESS_INDIVIDUALSOUND("commands.stopsound.success.individualSound"), // legacy 1.9-1.12
    COMMANDS_STOPSOUND_SUCCESS_SOUNDSOURCE("commands.stopsound.success.soundSource"), // legacy 1.9-1.12
    COMMANDS_STOPSOUND_SUCCESS_ALL("commands.stopsound.success.all"), // legacy 1.9-1.12

    COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE("commands.spawnpoint.success.multiple"), // 1.13+
    COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE_NEW("commands.spawnpoint.success.multiple.new"), // 1.21.9+
    COMMANDS_SPAWNPOINT_SUCCESS_SINGLE("commands.spawnpoint.success.single"), // 1.13+
    COMMANDS_SPAWNPOINT_SUCCESS_SINGLE_NEW("commands.spawnpoint.success.single.new"), // 1.21.9+
    COMMANDS_SPAWNPOINT_SUCCESS("commands.spawnpoint.success"), // legacy 1.8-1.12
    COMMANDS_SETWORLDSPAWN_SUCCESS("commands.setworldspawn.success"), // 1.8+
    COMMANDS_SETWORLDSPAWN_SUCCESS_NEW("commands.setworldspawn.success.new"), // 1.21.9+

    COMMANDS_STOP_SUCCESS("commands.stop.stopping"), // 1.13+
    COMMANDS_STOP_START("commands.stop.start"), // legacy 1.8-1.12

    COMMANDS_SUMMON_SUCCESS("commands.summon.success"), // 1.8+

    COMMANDS_TELEPORT_SUCCESS_ENTITY_MULTIPLE("commands.teleport.success.entity.multiple"), // 1.13+
    COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE("commands.teleport.success.entity.single"), // 1.13+
    COMMANDS_TELEPORT_SUCCESS_LOCATION_MULTIPLE("commands.teleport.success.location.multiple"), // 1.13+
    COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE("commands.teleport.success.location.single"), // 1.13+
    COMMANDS_TP_SUCCESS("commands.tp.success"), // legacy 1.8-1.12
    COMMANDS_TP_SUCCESS_COORDINATES("commands.tp.success.coordinates"), // legacy 1.8-1.12

    COMMANDS_TIME_QUERY("commands.time.query"), // 1.13+
    COMMANDS_TIME_SET("commands.time.set"), // 1.13+

    DEATH_ATTACK_ANVIL("death.attack.anvil"), // 1.8+
    DEATH_ATTACK_ANVIL_PLAYER("death.attack.anvil.player"), // 1.8+
    DEATH_ATTACK_ARROW("death.attack.arrow"), // 1.8+
    DEATH_ATTACK_ARROW_ITEM("death.attack.arrow.item"), // 1.8+
    DEATH_ATTACK_BAD_RESPAWN_POINT_MESSAGE("death.attack.badRespawnPoint.message"), // 1.15+
    DEATH_ATTACK_CACTUS("death.attack.cactus"), // 1.8+
    DEATH_ATTACK_CACTUS_PLAYER("death.attack.cactus.player"), // 1.8+
    DEATH_ATTACK_CRAMMING("death.attack.cramming"), // 1.9+
    DEATH_ATTACK_CRAMMING_PLAYER("death.attack.cramming.player"), // 1.9+
    DEATH_ATTACK_DRAGON_BREATH("death.attack.dragonBreath"), // 1.9+
    DEATH_ATTACK_DRAGON_BREATH_PLAYER("death.attack.dragonBreath.player"), // 1.9+
    DEATH_ATTACK_DROWN("death.attack.drown"), // 1.8+
    DEATH_ATTACK_DROWN_PLAYER("death.attack.drown.player"), // 1.8+
    DEATH_ATTACK_DRYOUT("death.attack.dryout"), // 1.13+
    DEATH_ATTACK_DRYOUT_PLAYER("death.attack.dryout.player"), // 1.13+
    DEATH_ATTACK_EVEN_MORE_MAGIC("death.attack.even_more_magic"), // 1.8+
    DEATH_ATTACK_EXPLOSION("death.attack.explosion"), // 1.8+
    DEATH_ATTACK_EXPLOSION_PLAYER("death.attack.explosion.player"), // 1.8+
    DEATH_ATTACK_EXPLOSION_ITEM("death.attack.explosion.item"), // 1.8+
    DEATH_ATTACK_EXPLOSION_PLAYER_ITEM("death.attack.explosion.player.item"), // 1.8+
    DEATH_ATTACK_FALL("death.attack.fall"), // 1.8+
    DEATH_ATTACK_FALL_PLAYER("death.attack.fall.player"), // 1.8+
    DEATH_ATTACK_FALLING_BLOCK("death.attack.fallingBlock"), // 1.8+
    DEATH_ATTACK_FALLING_BLOCK_PLAYER("death.attack.fallingBlock.player"), // 1.8+
    DEATH_ATTACK_FALLING_STALACTITE("death.attack.fallingStalactite"), // 1.17+
    DEATH_ATTACK_FALLING_STALACTITE_PLAYER("death.attack.fallingStalactite.player"), // 1.17+
    DEATH_ATTACK_FIREBALL("death.attack.fireball"), // 1.8+
    DEATH_ATTACK_FIREBALL_ITEM("death.attack.fireball.item"), // 1.8+
    DEATH_ATTACK_FIREWORKS("death.attack.fireworks"), // 1.8+
    DEATH_ATTACK_FIREWORKS_ITEM("death.attack.fireworks.item"), // 1.8+
    DEATH_ATTACK_FIREWORKS_PLAYER("death.attack.fireworks.player"), // 1.8+
    DEATH_ATTACK_FLY_INTO_WALL("death.attack.flyIntoWall"), // 1.13+
    DEATH_ATTACK_FLY_INTO_WALL_PLAYER("death.attack.flyIntoWall.player"), // 1.13+
    DEATH_ATTACK_FREEZE("death.attack.freeze"), // 1.17+
    DEATH_ATTACK_FREEZE_PLAYER("death.attack.freeze.player"), // 1.17+
    DEATH_ATTACK_GENERIC("death.attack.generic"), // 1.8+
    DEATH_ATTACK_GENERIC_PLAYER("death.attack.generic.player"), // 1.8+
    DEATH_ATTACK_GENERIC_KILL("death.attack.genericKill"), // 1.8+
    DEATH_ATTACK_GENERIC_KILL_PLAYER("death.attack.genericKill.player"), // 1.8+
    DEATH_ATTACK_HOT_FLOOR("death.attack.hotFloor"), // 1.8+
    DEATH_ATTACK_HOT_FLOOR_PLAYER("death.attack.hotFloor.player"), // 1.8+
    DEATH_ATTACK_INDIRECT_MAGIC("death.attack.indirectMagic"), // 1.8+
    DEATH_ATTACK_INDIRECT_MAGIC_ITEM("death.attack.indirectMagic.item"), // 1.8+
    DEATH_ATTACK_IN_FIRE("death.attack.inFire"), // 1.8+
    DEATH_ATTACK_IN_FIRE_PLAYER("death.attack.inFire.player"), // 1.8+
    DEATH_ATTACK_IN_WALL("death.attack.inWall"), // 1.8+
    DEATH_ATTACK_IN_WALL_PLAYER("death.attack.inWall.player"), // 1.8+
    DEATH_ATTACK_LAVA("death.attack.lava"), // 1.8+
    DEATH_ATTACK_LAVA_PLAYER("death.attack.lava.player"), // 1.8+
    DEATH_ATTACK_LIGHTNING_BOLT("death.attack.lightningBolt"), // 1.8+
    DEATH_ATTACK_LIGHTNING_BOLT_PLAYER("death.attack.lightningBolt.player"), // 1.8+
    DEATH_ATTACK_MACE_SMASH("death.attack.mace_smash"), // 1.20.5+
    DEATH_ATTACK_MACE_SMASH_ITEM("death.attack.mace_smash.item"), // 1.20.5+
    DEATH_ATTACK_MAGIC("death.attack.magic"), // 1.8+
    DEATH_ATTACK_MAGIC_PLAYER("death.attack.magic.player"), // 1.8+
    DEATH_ATTACK_MOB("death.attack.mob"), // 1.8+
    DEATH_ATTACK_MOB_ITEM("death.attack.mob.item"), // 1.8+
    DEATH_ATTACK_ON_FIRE("death.attack.onFire"), // 1.8+
    DEATH_ATTACK_ON_FIRE_ITEM("death.attack.onFire.item"), // 1.8+
    DEATH_ATTACK_ON_FIRE_PLAYER("death.attack.onFire.player"), // 1.8+
    DEATH_ATTACK_OUT_OF_WORLD("death.attack.outOfWorld"), // 1.8+
    DEATH_ATTACK_OUT_OF_WORLD_PLAYER("death.attack.outOfWorld.player"), // 1.8+
    DEATH_ATTACK_OUTSIDE_BORDER("death.attack.outsideBorder"), // 1.8+
    DEATH_ATTACK_OUTSIDE_BORDER_PLAYER("death.attack.outsideBorder.player"), // 1.8+
    DEATH_ATTACK_PLAYER("death.attack.player"), // 1.8+
    DEATH_ATTACK_PLAYER_ITEM("death.attack.player.item"), // 1.8+
    DEATH_ATTACK_SONIC_BOOM("death.attack.sonic_boom"), // 1.19+
    DEATH_ATTACK_SONIC_BOOM_ITEM("death.attack.sonic_boom.item"), // 1.19+
    DEATH_ATTACK_SONIC_BOOM_PLAYER("death.attack.sonic_boom.player"), // 1.19+
    DEATH_ATTACK_STALAGMITE("death.attack.stalagmite"), // 1.17+
    DEATH_ATTACK_STALAGMITE_PLAYER("death.attack.stalagmite.player"), // 1.17+
    DEATH_ATTACK_STARVE("death.attack.starve"), // 1.8+
    DEATH_ATTACK_STARVE_PLAYER("death.attack.starve.player"), // 1.8+
    DEATH_ATTACK_STING("death.attack.sting"), // 1.8+
    DEATH_ATTACK_STING_ITEM("death.attack.sting.item"), // 1.8+
    DEATH_ATTACK_STING_PLAYER("death.attack.sting.player"), // 1.8+
    DEATH_ATTACK_SWEET_BERRY_BUSH("death.attack.sweetBerryBush"), // 1.14+
    DEATH_ATTACK_SWEET_BERRY_BUSH_PLAYER("death.attack.sweetBerryBush.player"), // 1.14+
    DEATH_ATTACK_THORNS("death.attack.thorns"), // 1.8+
    DEATH_ATTACK_THORNS_ITEM("death.attack.thorns.item"), // 1.8+
    DEATH_ATTACK_THROWN("death.attack.thrown"), // 1.8+
    DEATH_ATTACK_THROWN_ITEM("death.attack.thrown.item"), // 1.8+
    DEATH_ATTACK_TRIDENT("death.attack.trident"), // 1.13+
    DEATH_ATTACK_TRIDENT_ITEM("death.attack.trident.item"), // 1.13+
    DEATH_ATTACK_WITHER("death.attack.wither"), // 1.8+
    DEATH_ATTACK_WITHER_PLAYER("death.attack.wither.player"), // 1.8+
    DEATH_ATTACK_WITHER_SKULL("death.attack.witherSkull"), // 1.8+
    DEATH_ATTACK_WITHER_SKULL_ITEM("death.attack.witherSkull.item"), // 1.8+
    DEATH_FELL_ACCIDENT_GENERIC("death.fell.accident.generic"), // 1.8+
    DEATH_FELL_ACCIDENT_LADDER("death.fell.accident.ladder"), // 1.8+
    DEATH_FELL_ACCIDENT_OTHER_CLIMBABLE("death.fell.accident.other_climbable"), // 1.17+
    DEATH_FELL_ACCIDENT_SCAFFOLDING("death.fell.accident.scaffolding"), // 1.14+
    DEATH_FELL_ACCIDENT_TWISTING_VINES("death.fell.accident.twisting_vines"), // 1.17+
    DEATH_FELL_ACCIDENT_VINES("death.fell.accident.vines"), // 1.8+
    DEATH_FELL_ACCIDENT_WEEPING_VINES("death.fell.accident.weeping_vines"), // 1.16+
    DEATH_FELL_ASSIST("death.fell.assist"), // 1.8+
    DEATH_FELL_ASSIST_ITEM("death.fell.assist.item"), // 1.8+
    DEATH_FELL_FINISH("death.fell.finish"), // 1.8+
    DEATH_FELL_FINISH_ITEM("death.fell.finish.item"), // 1.8+
    DEATH_FELL_KILLER("death.fell.killer"), // 1.8+

    ITEM_MINECRAFT_DEBUG_STICK_EMPTY("item.minecraft.debug_stick.empty"), // 1.13+
    ITEM_MINECRAFT_DEBUG_STICK_SELECT("item.minecraft.debug_stick.select"), // 1.13+
    ITEM_MINECRAFT_DEBUG_STICK_UPDATE("item.minecraft.debug_stick.update"), // 1.13+

    MULTIPLAYER_PLAYER_JOINED("multiplayer.player.joined"), // 1.8+
    MULTIPLAYER_PLAYER_JOINED_RENAMED("multiplayer.player.joined.renamed"), // 1.8+
    MULTIPLAYER_PLAYER_LEFT("multiplayer.player.left"), // 1.8+
    MULTIPLAYER_MESSAGE_NOT_DELIVERED("multiplayer.message_not_delivered"), // 1.19+

    SLEEP_NOT_POSSIBLE("sleep.not_possible"), // 1.8+
    SLEEP_PLAYERS_SLEEPING("sleep.players_sleeping"), // 1.8+
    SLEEP_SKIPPING_NIGHT("sleep.skipping_night"), // 1.8+

    COMMANDS_WEATHER_SET_CLEAR("commands.weather.set.clear"), // 1.13+
    COMMANDS_WEATHER_SET_RAIN("commands.weather.set.rain"), // 1.13+
    COMMANDS_WEATHER_SET_THUNDER("commands.weather.set.thunder"), // 1.13+
    COMMANDS_WEATHER_CLEAR("commands.weather.clear"), // legacy 1.8-1.12
    COMMANDS_WEATHER_RAIN("commands.weather.rain"), // legacy 1.8-1.12
    COMMANDS_WEATHER_THUNDER("commands.weather.thunder"), // legacy 1.8-1.12

    COMMANDS_WORLDBORDER_CENTER_SUCCESS("commands.worldborder.center.success"), // 1.13+
    COMMANDS_WORLDBORDER_DAMAGE_AMOUNT_SUCCESS("commands.worldborder.damage.amount.success"), // 1.13+
    COMMANDS_WORLDBORDER_DAMAGE_BUFFER_SUCCESS("commands.worldborder.damage.buffer.success"), // 1.13+
    COMMANDS_WORLDBORDER_GET_SUCCESS("commands.worldborder.get.success"), // legacy 1.8-1.12
    COMMANDS_WORLDBORDER_GET("commands.worldborder.get"), // 1.13+
    COMMANDS_WORLDBORDER_SET_SUCCESS("commands.worldborder.set.success"), // legacy 1.8-1.12
    COMMANDS_WORLDBORDER_SETSLOWLY_GROW_SUCCESS("commands.worldborder.setSlowly.grow.success"), // legacy 1.8-1.12
    COMMANDS_WORLDBORDER_SET_GROW("commands.worldborder.set.grow"), // 1.13+
    COMMANDS_WORLDBORDER_SET_IMMEDIATE("commands.worldborder.set.immediate"), // 1.13+
    COMMANDS_WORLDBORDER_SETSLOWLY_SHRINK_SUCCESS("commands.worldborder.setSlowly.shrink.success"), // legacy 1.8-1.12
    COMMANDS_WORLDBORDER_SET_SHRINK("commands.worldborder.set.shrink"), // 1.13+
    COMMANDS_WORLDBORDER_WARNING_DISTANCE_SUCCESS("commands.worldborder.warning.distance.success"), // 1.13+
    COMMANDS_WORLDBORDER_WARNING_TIME_SUCCESS("commands.worldborder.warning.time.success"), // 1.13+

    UNKNOWN("unknown"); // not supported message

    private static final Map<String, MinecraftTranslationKey> ENUM_BY_KEY = Arrays.stream(MinecraftTranslationKey.values())
            .collect(Collectors.toUnmodifiableMap(
                    translationKey -> translationKey.key,
                    translationKey -> translationKey
            ));

    private final String key;

    MinecraftTranslationKey(String key) {
        this.key = key;
    }

    public static MinecraftTranslationKey fromString(TranslatableComponent component) {
        return fromString(component.key());
    }

    public static MinecraftTranslationKey fromString(String string) {
        if (string == null || string.isEmpty()) return UNKNOWN;

        return ENUM_BY_KEY.getOrDefault(string, UNKNOWN);
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
