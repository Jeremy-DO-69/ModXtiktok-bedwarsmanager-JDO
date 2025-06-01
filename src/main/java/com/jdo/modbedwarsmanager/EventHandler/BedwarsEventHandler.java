package com.jdo.modbedwarsmanager.EventHandler;
import com.jdo.modbedwarsmanager.ModBedwarsManager;
import com.jdo.modbedwarsmanager.Utils.ArenaUtils;
import com.jdo.modbedwarsmanager.Utils.ExportUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static com.jdo.modbedwarsmanager.License.LicenseChecker.isLicenseValid;
import static com.jdo.modbedwarsmanager.ModBedwarsManager.*;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(modid = "modbedwarsmanager", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BedwarsEventHandler {

    private static boolean wasLanOpen = false;

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        System.out.println("[BedwarsEventHandler] Server started");
        Server = event.getServer();
        if (Server.isSingleplayer() && Server.isPublished()) {
            System.out.println("[BedwarsEventHandler] Monde en LAN ouvert !");
        }
        Objects.requireNonNull(Server.getLevel(Level.OVERWORLD)).getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, Server);
        Objects.requireNonNull(Server.getLevel(Level.OVERWORLD)).getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, Server);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                level.setDayTime(6000);
                level.setWeatherParameters(0, 0, false, false);
            }
        }

        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        boolean isLanNow = server.isSingleplayer() && server.isPublished();

        if (!wasLanOpen && isLanNow) {
            System.out.println("🎉 Partie LAN détectée !");
        }

        wasLanOpen = isLanNow;
    }

    @SubscribeEvent
    public static void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("A player joined");
        event.getEntity().teleportTo(SpawnPos.x, SpawnPos.y, SpawnPos.z);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        BlockPos pos = event.player.blockPosition();
        if (currentMode == Mode.NOTHING) {
            if (isInZone(pos)) {
                if (!isLicenseValid(event.player)) {
                    event.player.displayClientMessage(
                            Component.literal("Licence non activé ou invalide").withStyle(ChatFormatting.RED),
                            true
                    );
                    return;
                }
                currentMode = ModBedwarsManager.Mode.SOLO;
                Player1 = event.player;
                Player1.heal(20);
                StartSoloGame(event);
            }
        } else {
            if (isInZone(pos)) {
                event.player.displayClientMessage(
                        Component.literal("Partie en cours").withStyle(style -> style.withColor(0x00FF00)),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (player.equals(Player1)) {
            Player1 = null;
            Player2 = null;
            currentMode = Mode.NOTHING;
            Server.getPlayerList().broadcastSystemMessage(
                    Component.literal("Le joueur 1 : " + player.getName() + " a quitté la partie. Fin de la session.").withStyle(ChatFormatting.RED),
                    false
            );
            EndOfGame();
        } else if (player.equals(Player2)) {
            Player1 = null;
            Player2 = null;
            currentMode = Mode.NOTHING;
            Server.getPlayerList().broadcastSystemMessage(
                    Component.literal("Le joueur 2 " + player.getName() + " a quitté la partie. Fin de la session.").withStyle(ChatFormatting.RED),
                    false
            );
            EndOfGame();
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Entity exploder = event.getExplosion().getExploder();
        ModBedwarsManager.LOGGER.info("exploder: " + exploder);
        List<BlockPos> affected = event.getAffectedBlocks();

        affected.removeIf(pos -> {
            BlockState state = exploder.level().getBlockState(pos);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

            boolean protectedBlock = !isBlockAllowed(id.toString(), STATIC_ALLOWED_BLOCKS);
            if (protectedBlock) {
            }
            return protectedBlock;
        });

        for (BlockPos pos : affected) {
            BlockState state = exploder.level().getBlockState(pos);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("exportblocks")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ServerLevel level = player.serverLevel();

                    Vec3 from = new Vec3(-28, 3, 57);
                    Vec3 to = new Vec3(25, -20, -54);

                    File file = new File("exported_blocks_" + player.getName().getString() + ".txt");

                    ExportUtil.exportBlockIdsBetween(level, from, to, file);

                    return 1;
                })
        );
        LOGGER.info("Commande /exportblocks enregistrée !");


        CommandDispatcher<CommandSourceStack> dispatcher2 = event.getDispatcher();
        dispatcher2.register(Commands.literal("testmixins")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("remove");
                            builder.suggest("setBlock");
                            builder.suggest("destroyBlock");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String action = StringArgumentType.getString(ctx, "action");
                            CommandSourceStack source = ctx.getSource();
                            Entity entity = source.getEntity();

                            if (!(entity instanceof ServerPlayer player)) {
                                source.sendFailure(net.minecraft.network.chat.Component.literal("Cette commande doit être exécutée par un joueur."));
                                return 0;
                            }

                            Level level = player.level();
                            BlockPos posBelow = player.blockPosition().below();

                            switch (action) {
                                case "remove" -> {
                                    LOGGER.info("Commande /testmixins remove !");
                                    level.removeBlock(posBelow, false);
                                }
                                case "setBlock" -> {
                                    LOGGER.info("Commande /testmixins setBlock !");
                                    level.setBlock(posBelow, Blocks.AIR.defaultBlockState(), 3);
                                }
                                case "destroyBlock" -> {
                                    LOGGER.info("Commande /testmixins destroyBlock !");
                                    level.destroyBlock(posBelow, true, player, 512);
                                }
                                default -> {
                                    source.sendFailure(net.minecraft.network.chat.Component.literal("Argument inconnu : " + action));
                                    return 0;
                                }
                            }

                            return 1;
                        })
                )
        );
        LOGGER.info("Commande /testmixins enregistrée !");

        CommandDispatcher<CommandSourceStack> dispatcher3 = event.getDispatcher();
        dispatcher3.register(Commands.literal("testspawnArena")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("streamup");
                            builder.suggest("tiktok");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("arenaX", IntegerArgumentType.integer())
                                .then(Commands.argument("arenaY", IntegerArgumentType.integer())
                                        .then(Commands.argument("arenaZ", IntegerArgumentType.integer())
                                                .executes(ctx -> {

                                                    String mode = StringArgumentType.getString(ctx, "mode");
                                                    boolean isForStreamUp = mode.equalsIgnoreCase("streamup");
                                                    ServerLevel level = Server.getLevel(Level.OVERWORLD);

                                                    int arenaX = IntegerArgumentType.getInteger(ctx, "arenaX");
                                                    int arenaY = IntegerArgumentType.getInteger(ctx, "arenaY");
                                                    int arenaZ = IntegerArgumentType.getInteger(ctx, "arenaZ");

                                                    String arenaFile = isForStreamUp ? ARENA_STREAMUP : ARENA_TIKTOK;

                                                    ArenaUtils.loadSchematic(
                                                            new File("config/worldedit/schematics/" + arenaFile),
                                                            level,
                                                            new BlockPos(arenaX, arenaY, arenaZ)
                                                    );
                                                    LOGGER.info("/testspawnArena executed with mode: " + mode);
                                                    return 1;
                                                })))))
        );
        LOGGER.info("Commande /testspawnArena enregistrée !");

        CommandDispatcher<CommandSourceStack> dispatcher5 = event.getDispatcher();
        dispatcher5.register(Commands.literal("testspawnHouse")
                .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("houseX", IntegerArgumentType.integer())
                            .then(Commands.argument("houseY", IntegerArgumentType.integer())
                                    .then(Commands.argument("houseZ", IntegerArgumentType.integer())
                                            .executes(ctx -> {

                                                ServerLevel level = Server.getLevel(Level.OVERWORLD);

                                                int houseX = IntegerArgumentType.getInteger(ctx, "houseX");
                                                int houseY = IntegerArgumentType.getInteger(ctx, "houseY");
                                                int houseZ = IntegerArgumentType.getInteger(ctx, "houseZ");

                                                ArenaUtils.loadSchematic(
                                                        new File("config/worldedit/schematics/" + HOUSE),
                                                        level,
                                                        new BlockPos(houseX, houseY, houseZ)
                                                );

                                                LOGGER.info("/testspawnHouse executed");
                                                return 1;
                                            }))))
        );
        LOGGER.info("Commande /testspawnHouse enregistrée !");

        CommandDispatcher<CommandSourceStack> dispatcher4 = event.getDispatcher();

        dispatcher4.register(Commands.literal("testcleanArena")
                .requires(source -> source.hasPermission(2)) // Optional: only allow OPs
                .executes(ctx -> {
                    ServerLevel level = Server.getLevel(Level.OVERWORLD);
                    ArenaUtils.clearArea(level, new BlockPos(-400, -23, 400), new BlockPos(400, 100, -400));
                    LOGGER.info("Commande /testcleanArena executé !");
                    return 1;
                })
        );
        LOGGER.info("Commande /testcleanArena enregistrée !");

        CommandDispatcher<CommandSourceStack> dispatcher6 = event.getDispatcher();
        dispatcher6.register(Commands.literal("teststuff")
                .requires(source -> source.hasPermission(2)) // Optional: only allow OPs
                .executes(ctx -> {
                    giveStartingGear(ctx.getSource().getPlayer());
                    LOGGER.info("Commande /teststuff executé !");
                    return 1;
                })
        );
        LOGGER.info("Commande /teststuff enregistrée !");
    }

    private static boolean isInZone(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return x >= 5 && x <= 6
                && y == 209
                && z >= -13 && z <= -12;
    }

    @SubscribeEvent
    public static void onMobExperienceDrop(LivingExperienceDropEvent event) {
        event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof net.minecraft.world.entity.Mob || (entity instanceof net.minecraft.world.entity.player.Player)) {
            BlockPos pos = entity.blockPosition();
            if (pos.getZ() >= 0 && pos.getZ() <= 1 &&
                    pos.getY() >= -16 && pos.getY() <= 23 &&
                    pos.getX() >= -27 && pos.getX() <= 27) {
                entity.setDeltaMovement(0, 0, -0.1);
                entity.teleportTo(entity.getX(), entity.getY(), entity.getZ());
                LOGGER.info("Block " + entity.getName());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
           if (player == Player1) {
               player.teleportTo(Player1Respawn.x, Player1Respawn.y, Player1Respawn.z);
           } else if (player == Player2) {
                player.teleportTo(Player2Respawn.x, Player2Respawn.y, Player2Respawn.z);
           } else {
               player.teleportTo(SpawnPos.x, SpawnPos.y, SpawnPos.z);
           }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCanceled(true);
        }
    }

   @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        ModBedwarsManager.LOGGER.info("event: " + event);
        if (event.getPlayer().level().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer)) {

            BlockState state = event.getState();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

            if (!isBlockAllowed(id.toString(), STATIC_ALLOWED_BLOCKS)) {
                event.setCanceled(true);
            }
        } else {
            ModBedwarsManager.LOGGER.info("event setCanceled: true");
            event.setCanceled(true);
        }
    }
}