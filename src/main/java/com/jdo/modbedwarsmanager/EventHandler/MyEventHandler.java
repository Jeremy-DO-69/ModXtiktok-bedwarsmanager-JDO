package com.jdo.modbedwarsmanager.EventHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MyEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void on_toss(ItemTossEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity item_e) {
            ItemStack itemstack = item_e.getItem();
            if (event.getPlayer() != null) {
                event.getPlayer().getInventory().placeItemBackInInventory(itemstack);
            }
        }
        event.setCanceled(true);
    }
}