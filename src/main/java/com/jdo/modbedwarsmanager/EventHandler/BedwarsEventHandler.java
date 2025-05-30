package com.jdo.modbedwarsmanager.EventHandler;
import com.jdo.modbedwarsmanager.ModBedwarsManager;
import com.jdo.modbedwarsmanager.Utils.ExportUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.jdo.modbedwarsmanager.ModBedwarsManager.*;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(modid = "modbedwarsmanager", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BedwarsEventHandler {

    private static boolean wasLanOpen = false;

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        System.out.println("[BedwarsEventHandler] Server started");
        MinecraftServer server = event.getServer();
        if (server.isSingleplayer() && server.isPublished()) {
            System.out.println("[BedwarsEventHandler] Monde en LAN ouvert !");
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        BlockPos pos = event.player.blockPosition();
        if (isInZone(pos)) {
            currentMode = ModBedwarsManager.Mode.SOLO;
            Player1 = event.player;
            Player1.heal(20);
            StartSoloGame();
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
            event.setCanceled(true);
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
    }

    private static boolean isInZone(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return x >= 5 && x <= 6
                && y == 209
                && z >= -13 && z <= -12;
    }
}