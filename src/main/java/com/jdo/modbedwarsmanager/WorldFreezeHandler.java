package com.jdo.modbedwarsmanager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "modbedwarsmanager")
public class WorldFreezeHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                level.setDayTime(6000);
                level.setWeatherParameters(0, 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCanceled(true);
        }
    }
}
