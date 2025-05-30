package com.jdo.modbedwarsmanager.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.text2speech.Narrator.LOGGER;


@Mixin(BlockBehaviour.class)
public class BlockMixin {
    @Inject(method = "spawnAfterBreak(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void onSpawnAfterBreak(BlockState p_222949_, ServerLevel p_222950_, BlockPos p_222951_, ItemStack p_222952_, boolean p_222953_, CallbackInfo ci) {
        LOGGER.info("cancelled block break spawn item");
        ci.cancel();
    }
}
