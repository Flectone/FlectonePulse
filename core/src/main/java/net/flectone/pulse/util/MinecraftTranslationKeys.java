package net.flectone.pulse.util;

import net.kyori.adventure.text.TranslatableComponent;

import java.util.Arrays;

public enum MinecraftTranslationKeys {

    BLOCK_MINECRAFT_BED_NO_SLEEP("block.minecraft.bed.no_sleep"),
    BLOCK_MINECRAFT_BED_NOT_SAFE("block.minecraft.bed.not_safe"),
    BLOCK_MINECRAFT_BED_OBSTRUCTED("block.minecraft.bed.obstructed"),
    BLOCK_MINECRAFT_BED_OCCUPIED("block.minecraft.bed.occupied"),
    BLOCK_MINECRAFT_BED_TOO_FAR_AWAY("block.minecraft.bed.too_far_away"),
    BLOCK_MINECRAFT_SET_SPAWN("block.minecraft.set_spawn"),
    BLOCK_MINECRAFT_SPAWN_NOT_VALID("block.minecraft.spawn.not_valid"),

    CHAT_TYPE_ADVANCEMENT_CHALLENGE("chat.type.advancement.challenge"),
    CHAT_TYPE_ADVANCEMENT_GOAL("chat.type.advancement.goal"),
    CHAT_TYPE_ADVANCEMENT_TASK("chat.type.advancement.task"),

    COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS("commands.advancement.grant.many.to.one.success"),
    COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS("commands.advancement.grant.one.to.one.success"),
    COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS("commands.advancement.revoke.many.to.one.success"),
    COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS("commands.advancement.revoke.one.to.one.success"),
    COMMANDS_CLEAR_SUCCESS_MULTIPLE("commands.clear.success.multiple"),
    COMMANDS_CLEAR_SUCCESS_SINGLE("commands.clear.success.single"),
    COMMANDS_DEOP_SUCCESS("commands.deop.success"),
    COMMANDS_ENCHANT_SUCCESS_MULTIPLE("commands.enchant.success.multiple"),
    COMMANDS_ENCHANT_SUCCESS_SINGLE("commands.enchant.success.single"),
    COMMANDS_GAMEMODE_SUCCESS_OTHER("commands.gamemode.success.other"),
    COMMANDS_GAMEMODE_SUCCESS_SELF("commands.gamemode.success.self"),
    GAMEMODE_CHANGED("gameMode.changed"),
    COMMANDS_OP_SUCCESS("commands.op.success"),
    COMMANDS_SEED_SUCCESS("commands.seed.success"),
    COMMANDS_SETBLOCK_SUCCESS("commands.setblock.success"),
    COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE("commands.spawnpoint.success.multiple"),
    COMMANDS_SPAWNPOINT_SUCCESS_SINGLE("commands.spawnpoint.success.single"),

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

    MULTIPLAYER_PLAYER_JOINED("multiplayer.player.joined"),
    MULTIPLAYER_PLAYER_JOINED_RENAMED("multiplayer.player.joined.renamed"),
    MULTIPLAYER_PLAYER_LEFT("multiplayer.player.left"),
    MULTIPLAYER_MESSAGE_NOT_DELIVERED("multiplayer.message_not_delivered"),

    SLEEP_NOT_POSSIBLE("sleep.not_possible"),
    SLEEP_PLAYERS_SLEEPING("sleep.players_sleeping"),
    SLEEP_SKIPPING_NIGHT("sleep.skipping_night"),

    UNKNOWN("unknown");

    private final String key;

    MinecraftTranslationKeys(String key) {
        this.key = key;
    }

    public static MinecraftTranslationKeys fromString(TranslatableComponent component) {
        return fromString(component.key());
    }

    public static MinecraftTranslationKeys fromString(String string) {
        return Arrays.stream(MinecraftTranslationKeys.values())
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
