package com.jdo.modbedwarsmanager;
import com.jdo.modbedwarsmanager.Utils.ArenaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.jdo.CustomMobsSpawnIa.command.SpawnMobCommand.spawnWave;
import static com.mojang.text2speech.Narrator.LOGGER;


@Mod("modbedwarsmanager")
public class ModBedwarsManager {

    public static boolean isForStreamUp = true;
    public static String ARENA_TIKTOK = "arena_tiktok.schem";
    public static String ARENA_STREAMUP = "arena_streamup.schem";
    public static String HOUSE = "house.schem";

    //todo fix respawn
    public static Vec3 SpawnPos = new Vec3(0,209,16);

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

    public enum GamePhase {
        NOTHING,
        STARTING,
        WAVE_RUNNING,
        BETWEEN_WAVES,
        FINISHED
    }

    public enum Side {
        BLUE,
        RED,
        NOTHING
    }

    public static Mode currentMode = Mode.NOTHING;
    public static int currentWave = 0;
    public static GamePhase currentPhase = GamePhase.NOTHING;
    public static BlockPos BedPositionBlueZone = new BlockPos(-2, -15, -33);
    public static BlockPos BedPositionRedZone = new BlockPos(3, -15, 35);
    public static Vec3 SpawnPosBlueSide = new Vec3(-2, -15, -34);
    public static Vec3 SpawnPosRedSide = new Vec3(3, -15, 35);
    public static List<BlockPos> spawnPointsBlueSide = Arrays.asList(
            new BlockPos(25, -14, -47),
            new BlockPos(25, -14, -41),
            new BlockPos(25, -14, -35),
            new BlockPos(25, -14, -29),
            new BlockPos(25, -14, -23),
            new BlockPos(25, -14, -17),
            new BlockPos(25, -14, -11),
            new BlockPos(25, -14, -5),

            new BlockPos(-25, -14, -47),
            new BlockPos(-25, -14, -41),
            new BlockPos(-25, -14, -35),
            new BlockPos(-25, -14, -29),
            new BlockPos(-25, -14, -23),
            new BlockPos(-25, -14, -17),
            new BlockPos(-25, -14, -11),
            new BlockPos(-25, -14, -5)
    );
    public static List<BlockPos> spawnPointsRedSide = Arrays.asList(
            new BlockPos(25, -14, 47),
            new BlockPos(25, -14, 41),
            new BlockPos(25, -14, 35),
            new BlockPos(25, -14, 29),
            new BlockPos(25, -14, 23),
            new BlockPos(25, -14, 17),
            new BlockPos(25, -14, 11),
            new BlockPos(25, -14, 5),

            new BlockPos(-25, -14, 47),
            new BlockPos(-25, -14, 41),
            new BlockPos(-25, -14, 35),
            new BlockPos(-25, -14, 29),
            new BlockPos(-25, -14, 23),
            new BlockPos(-25, -14, 17),
            new BlockPos(-25, -14, 11),
            new BlockPos(-25, -14, 5)
    );
    public static List<BlockPos> fireworksBlueSide = Arrays.asList(
            new BlockPos(12, -12, -13),
            new BlockPos(-12, -12, -13)
    );;
    public static List<BlockPos> fireworksRedSide = Arrays.asList(
            new BlockPos(12, -12, 13),
            new BlockPos(-12, -12, 13)
    );;
    public static List<BlockPos> spawnPointsBlueSideTikTok = Arrays.asList(
            new BlockPos(-5, -14, -3),
            new BlockPos(0, -14, -3),
            new BlockPos(5, -14, -3)
    );;
    public static List<BlockPos> spawnPointsRedSideTikTok = Arrays.asList(
            new BlockPos(-5, -14, 3),
            new BlockPos(0, -14, 3),
            new BlockPos(5, -14, 3)
    );

    public static Player Player1 = null;
    public static Player Player2 = null;
    public static Side Player1Side = Side.NOTHING;
    public static Side Player2Side = Side.NOTHING;
    public static Vec3 Player1Respawn = null;
    public static Vec3 Player2Respawn = null;
    public static BlockPos BedPositionPlayer1 = null;
    public static BlockPos BedPositionPlayer2 = null;

    public static Player Winner = null;
    public static Player Loser = null;

    private static final Random random = new Random();
    public static boolean replacingArena = false;
    public static MinecraftServer Server = null;
    public static final String MODID = "modbedwarsmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public enum Mode {
        NOTHING,
        SOLO,
        MULTI
    }

    public ModBedwarsManager() {
    }

    public static boolean isBlockAllowed(String id, Set<String> blockList) {
        return blockList.contains(id);
    }

    public static BlockPos getBedPos(Side side) {
        BlockPos basePos;

        if (side == Side.RED) {
            basePos = BedPositionRedZone;
        } else {
            basePos = BedPositionBlueZone;
        }

        int radius = 4;
        BlockPos closestBed = null;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = basePos.offset(x, y, z);
                    BlockState state = Server.getLevel(Level.OVERWORLD).getBlockState(checkPos);
                    if (state.getBlock() instanceof BedBlock) {
                        closestBed = checkPos;
                    }
                }
            }
        }

        if (closestBed != null) {
            return closestBed;
        } else {
            return basePos;
        }
    }

    public static void BedDestroyed(Player playerEliminated) {
        if (currentMode == Mode.SOLO) {
            currentMode = Mode.NOTHING;
        }
        if (currentMode == Mode.MULTI) {
            if (playerEliminated == Player1) {
                Winner = Player2;
                Loser = Player1;
            } else if (playerEliminated == Player2) {
                Winner = Player1;
                Loser = Player2;
            }
            currentMode = Mode.NOTHING;
        }
    }

    public static void BedChoice(Mode currentMode) {

       if (currentMode == Mode.SOLO) {

            boolean isRedOrBlueSide = Math.random() < 0.5;

            if (isRedOrBlueSide) {
                Player1Side = Side.RED;
                BedPositionPlayer1 = getBedPos(Side.RED);
                Player1Respawn = SpawnPosRedSide;
            } else {
                Player1Side = Side.BLUE;
                BedPositionPlayer1 = getBedPos(Side.BLUE);
                Player1Respawn = SpawnPosBlueSide;
            }
           LOGGER.error(String.valueOf(Player1Side));
           LOGGER.error(String.valueOf(BedPositionPlayer1));
           LOGGER.error(String.valueOf(Player1Respawn));
       }
        if (currentMode == Mode.MULTI) {

            /*boolean isRedOrBlueSide = Math.random() < 0.5;

            if (isRedOrBlueSide) {
                Player1Side = Side.RED;
                BedPositionRed = BedPositionRedZone;
                System.out.println("Player1 is assigned to RED side.");
                Player1.sendSystemMessage(Component.literal("Vous êtes dans l'équipe ROUGE").withStyle(style -> style.withColor(0xFF0000)));
            } else {
                Player1Side = Side.BLUE;
                BedPosition = BedPositionBlueZone;
                System.out.println("Player1 is assigned to BLUE side.");
                Player1.sendSystemMessage(Component.literal("Vous êtes dans l'équipe BLEUE").withStyle(style -> style.withColor(0x0000FF)));
            }*/
        }
        return;
    }

    public static void StartSoloGame(TickEvent.PlayerTickEvent event) {
        ServerLevel level = Server.getLevel(Level.OVERWORLD);
        replacingArena = true;
        ArenaUtils.clearArea(level, new BlockPos(-400, -23, 400), new BlockPos(400, 100, -400));
        ArenaUtils.loadSchematic(new File("config/worldedit/schematics/" + (isForStreamUp ? ARENA_STREAMUP : ARENA_TIKTOK)), level, new BlockPos(-46, -23, 75));
        ArenaUtils.loadSchematic(new File("config/worldedit/schematics/" + HOUSE), level, new BlockPos(28, -20, -56));
        replacingArena = false;
        BedChoice(currentMode);
        event.player.teleportTo(Player1Respawn.x, Player1Respawn.y, Player1Respawn.z);
        LOGGER.error("Here4");
        StartGame(Server.getLevel(Level.OVERWORLD), currentMode);
    }

    public static void StartGame(ServerLevel level, Mode currentMode) {
        //killAllNonPlayersFromHosterWorld();
        LOGGER.error("Here5");
        if (currentMode == Mode.SOLO) {
            giveStartingGear(Player1);
            LOGGER.error("Here6");
            currentPhase = GamePhase.STARTING;
            broadcast("La partie va commencer... Utilise la touche R pour te mettre en mode attaque ! Tu verras l'animation changer...", ChatFormatting.YELLOW);
            schedule(() -> {
                broadcast("La partie commence !", ChatFormatting.GREEN);
                if (Player1Side == Side.RED) {
                    spawnFireworks(level, fireworksRedSide, true);
                    startWave(level, BedPositionPlayer1, Player1Side);
                } else {
                    spawnFireworks(level, fireworksRedSide, false);
                    startWave(level, BedPositionPlayer1, Player1Side);
                }
            }, 100);
        }
    }

    public static void giveStartingGear(Player player) {

        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helmet.getOrCreateTag().putBoolean("Unbreakable", true);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.getOrCreateTag().putBoolean("Unbreakable", true);

        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        leggings.getOrCreateTag().putBoolean("Unbreakable", true);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.getOrCreateTag().putBoolean("Unbreakable", true);

        ResourceLocation swordItemId = ResourceLocation.fromNamespaceAndPath("epicfight", "netherite_longsword");
        Item swordModItem = ForgeRegistries.ITEMS.getValue(swordItemId);
        if (swordModItem != null) {
            ItemStack sword = new ItemStack(swordModItem);
            sword.enchant(Enchantments.SHARPNESS, 3);
            sword.getOrCreateTag().putBoolean("Unbreakable", true);
            player.getInventory().add(sword);
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        }

        ResourceLocation zweihanderItemId = ResourceLocation.fromNamespaceAndPath("magistuarmory", "netherite_zweihander");
        Item zweihanderModItem = ForgeRegistries.ITEMS.getValue(zweihanderItemId);
        if (zweihanderModItem != null) {
            ItemStack zweihander = new ItemStack(zweihanderModItem);
            zweihander.enchant(Enchantments.SHARPNESS, 3);
            zweihander.getOrCreateTag().putBoolean("Unbreakable", true);
            player.getInventory().add(zweihander);
        }

        ResourceLocation lochaberaxeItemId = ResourceLocation.fromNamespaceAndPath("magistuarmory", "netherite_lochaberaxe");
        Item lochaberaxeModItem = ForgeRegistries.ITEMS.getValue(lochaberaxeItemId);
        if (lochaberaxeModItem != null) {
            ItemStack lochaberaxe = new ItemStack(lochaberaxeModItem);
            lochaberaxe.enchant(Enchantments.SHARPNESS, 3);
            lochaberaxe.getOrCreateTag().putBoolean("Unbreakable", true);
            player.getInventory().add(lochaberaxe);
        }

        ResourceLocation paveseItemId = ResourceLocation.fromNamespaceAndPath("magistuarmory", "netherite_pavese");
        Item paveseModItem = ForgeRegistries.ITEMS.getValue(paveseItemId);
        if (paveseModItem != null) {
            ItemStack pavese = new ItemStack(paveseModItem);
            pavese.getOrCreateTag().putBoolean("Unbreakable", true);
            player.setItemInHand(InteractionHand.OFF_HAND, pavese);
        }

        player.getInventory().armor.set(3, helmet);
        player.getInventory().armor.set(2, chestplate);
        player.getInventory().armor.set(1, leggings);
        player.getInventory().armor.set(0, boots);
    }

    //todo
    public static int HowMuchMobForCurrentWave(int currentWave) {
        return 1;
    }

    //todo
    public static int WhatMobToSpawnForCurrentWave(int currentWave) {
        return 1;
    }

    public static void startWave(ServerLevel level, BlockPos BedPosition, Side side) {
        currentWave++;
        currentPhase = GamePhase.WAVE_RUNNING;
        LOGGER.error("try startWave");
        broadcast("Vague " + currentWave + " ! Commence maintenant !", ChatFormatting.GOLD);
        if (currentMode == Mode.SOLO) {
            ItemStack enchantedApple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
            LOGGER.error("give gapple");
            Player1.getInventory().add(enchantedApple);
        }
        if (currentMode == Mode.MULTI) {
            ItemStack enchantedApple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
            Player1.getInventory().add(enchantedApple);
            Player2.getInventory().add(enchantedApple);
        }
        BlockPos spawn = getRandomSpawn(side);
        int count = HowMuchMobForCurrentWave(currentWave);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "zombie");
        spawnWave(level, id, count, BedPosition, spawn.getX(), spawn.getY(), spawn.getZ());
        LOGGER.error("here7");
        schedule(() -> {
            currentPhase = GamePhase.BETWEEN_WAVES;
            broadcast("Pause entre les vagues", ChatFormatting.GRAY);
            if (currentMode == Mode.NOTHING) {
                EndOfGame();
            } else {
                schedule(() -> startWave(level, BedPosition, side), 100);
            }
        }, 600);
    }

    public static void spawnFireworks(ServerLevel level, List<BlockPos> positions, boolean isRed) {
        int color = isRed ? 0xFF0000 : 0x00FF00;
        LOGGER.error("try spawnFireworks");
        for (BlockPos pos : positions) {
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag fireworksTag = new CompoundTag();
            CompoundTag explosion = new CompoundTag();
            explosion.putByte("Type", (byte) 1); // Type: Large Ball
            explosion.putIntArray("Colors", new int[]{color});
            fireworksTag.put("Explosions", new ListTag() {{
                add(explosion);
            }});
            fireworksTag.putByte("Flight", (byte) 1);

            CompoundTag display = new CompoundTag();
            display.put("Fireworks", fireworksTag);
            fireworkStack.setTag(display);

            FireworkRocketEntity rocket = new FireworkRocketEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, fireworkStack);
            level.addFreshEntity(rocket);
        }
    }

    public static BlockPos getRandomSpawn(Side side) {

        if (side == Side.RED) {
            return spawnPointsRedSide.get(random.nextInt(spawnPointsRedSide.size()));
        } else {
            return spawnPointsBlueSide.get(random.nextInt(spawnPointsBlueSide.size()));
        }
    }

    public static void schedule(Runnable task, int ticks) {
        MinecraftForge.EVENT_BUS.register(new Object() {
            int counter = 0;

            @SubscribeEvent
            public void onTick(TickEvent.ServerTickEvent event) {
                if (event.phase == TickEvent.Phase.END) {
                    counter++;
                    if (counter >= ticks) {
                        task.run();
                        MinecraftForge.EVENT_BUS.unregister(this);
                    }
                }
            }
        });
    }

    public static void broadcast(String message, ChatFormatting color) {
        Component component = Component.literal(message).withStyle(color);
        for (ServerPlayer player : Objects.requireNonNull(Server.getLevel(Level.OVERWORLD)).players()) {
            if (player == Player1 || player == Player2) {
                player.sendSystemMessage(component);
            }
        }
    }

    public static void broadcastTo(ServerPlayer player, String message, ChatFormatting color) {
        Component component = Component.literal(message).withStyle(color);
        player.sendSystemMessage(component);
    }

    public static void clearPlayerGear(Player player) {
        player.getInventory().clearContent();
        player.removeAllEffects();
    }

    public static void cleanup() {
        if (Player1 != null) {
            Player1.teleportTo(SpawnPos.x, SpawnPos.y, SpawnPos.z);
            Player1.heal(20);
            clearPlayerGear(Player1);
            Player1 = null;
        }
        if (Player2 != null) {
            Player2.teleportTo(SpawnPos.x, SpawnPos.y, SpawnPos.z);
            Player2.heal(20);
            clearPlayerGear(Player2);
            Player2 = null;
        }
        Winner = null;
        Loser = null;
        killAllNonPlayersFromHosterWorld();
        ArenaUtils.clearArea(Server.getLevel(Level.OVERWORLD), new BlockPos(-400, -23, 400), new BlockPos(400, 100, -400));
    }

    public static void EndOfGame() {
        // Remove map
        currentPhase = GamePhase.FINISHED;

        if (currentMode == Mode.SOLO) {
            spawnFireworks(Server.getLevel(Level.OVERWORLD), Player1Side == Side.RED ? fireworksRedSide : fireworksBlueSide, Player1Side == Side.RED);
            broadcast("Fin de la partie ! Record : vague " + currentWave, ChatFormatting.AQUA);
        } else if (currentMode == Mode.MULTI) {
            if (Winner == Player1) {
                spawnFireworks(Server.getLevel(Level.OVERWORLD), Player1Side == Side.RED ? fireworksRedSide : fireworksBlueSide, Player1Side == Side.RED);
                broadcastTo((ServerPlayer) Player1, "Victoire !", ChatFormatting.GREEN);
                broadcastTo((ServerPlayer) Player2, "Défaite...", ChatFormatting.RED);
            } else {
                spawnFireworks(Server.getLevel(Level.OVERWORLD), Player2Side == Side.RED ? fireworksRedSide : fireworksBlueSide, Player2Side == Side.RED);
                broadcastTo((ServerPlayer) Player2, "Victoire !", ChatFormatting.GREEN);
                broadcastTo((ServerPlayer) Player1, "Défaite...", ChatFormatting.RED);
            }
        }

        schedule(ModBedwarsManager::cleanup, 100); // 5 secondes après fin
    }

    public static void killAllNonPlayersFromHosterWorld() {
        if (Server == null) return;

        ServerLevel level = Server.getLevel(Level.OVERWORLD); // or (ServerLevel) hoster.level()
        for (Entity entity : level.getEntities(null, e -> !(e instanceof Player))) {
            entity.discard();
        }
    }
}