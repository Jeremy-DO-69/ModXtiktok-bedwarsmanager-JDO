package com.jdo.modbedwarsmanager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


@Mod("modbedwarsmanager")
public class ModBedwarsManager {

    public static final Set<String> STATIC_ALLOWED_BLOCKS = Set.of(
            "fetzisasiandeco:framed_block_fence",
            "minecraft:oak_fence_gate",
            "handcrafted:spruce_corner_trim",
            "fetzisasiandeco:white_roof_slab_long_framed_block",
            "fetzisasiandeco:white_roof_slab_framed_block",
            "minecraft:soul_lantern",
            "minecraft:stripped_spruce_log",
            "fetzisasiandeco:light_gray_roof_block_framed_block",
            "fetzisasiandeco:japanese3_wall_framed_block",
            "minecraft:stripped_spruce_wood",
            "fetzisasiandeco:japanese4_wall_framed_block",
            "minecraft:white_concrete",
            "fetzisasiandeco:light_gray_roof_stairs_long_framed_block",
            "fetzisasiandeco:white_roof_stairs_framed_block",
            "minecraft:barrel",
            "minecraft:ladder",
            "fetzisasiandeco:japanese2_wall_framed_block",
            "handcrafted:jungle_fancy_bed",
            "minecraft:spruce_trapdoor",
            "minecraft:white_banner",
            "fetzisasiandeco:white_roof_block_framed_block",
            "minecraft:lantern",
            "minecraft:dark_oak_stairs",
            "minecraft:stone_brick_stairs",
            "minecraft:stripped_dark_oak_log",
            "minecraft:cracked_stone_bricks",
            "minecraft:stone_bricks",
            "minecraft:stone_brick_wall",
            "supplementaries:timber_cross_brace",
            "supplementaries:daub_cross_brace",
            "minecraft:stripped_dark_oak_wood",
            "minecraft:red_banner",
            "fantasyfurniture:royal/bed_single",
            "supplementaries:stone_tile",
            "minecraft:oak_fence"
    );

    public static final Set<String> STATIC_ALLOWED_BLOCKS_CANNON = Set.of(
            "minecraft:flowering_azalea_leaves",
            "minecraft:warped_stem",
            "minecraft:cherry_leaves",
            "minecraft:crimson_stem"
    );

    public static final Set<String> STATIC_ALLOWED_MOBS = Set.of(
            "minecraft:zombie",
            "minecraft:creeper",
            "minecraft:evoker",
            "minecraft:skeleton",
            "minecraft:witch",
            "mutantmonsters:mutant_zombie",
            "mutantmonsters:creeper_minion",
            "mutantmonsters:mutant_snow_golem",
            "mutantmonsters:mutant_skeleton"
    );

    public static boolean verified = false;
    public  static Mode currentMode = Mode.NOTHING;

    public static Player Player1;
    public static Player Player2;

    public static final String MODID = "modbedwarsmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public enum Mode {
        NOTHING,
        SOLO,
        MULTI,
    }

    public ModBedwarsManager() {
        System.out.println("[ModBedwarsManager] Mod constructor called.");
        // You can do mod setup here if needed (like loading configs or capabilities)
    }

    public static boolean isBlockAllowed(String id, Set<String> blockList) {
        System.out.println("is block allowed : " + id + blockList.contains(id));
        return blockList.contains(id);
    }

    public static void StartSoloGame() {
        System.out.println("Starting solo game");
    }

    public static void EndOfGame() {
        // Remove map

        if (Player1 != null) {
            // TP current player 1 to spawn
            Player1.teleportTo(0,209,16);
            // Heal player
            Player1.heal(20);
            //Make player 1 immortal

            // Remove player 1 from system
            Player1 = null;
        }
        if (Player2 != null) {
            // TP current player 2 to spawn
            Player2.teleportTo(0,209,16);
            // Heal player
            Player2.heal(20);
            //Make player 2 immortal

            // Remove player 2 from system
            Player2 = null;
        }
    }
}