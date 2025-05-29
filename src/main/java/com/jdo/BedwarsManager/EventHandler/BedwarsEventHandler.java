package com.jdo.BedwarsManager.EventHandler;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

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
}