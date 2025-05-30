package com.jdo.modbedwarsmanager.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.jdo.modbedwarsmanager.ModBedwarsManager.*;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mixin(Level.class)
public class LevelMixin {

    static {
        System.out.println("[ModBedwarsManager] LevelMixin has been applied to Level.class");
    }

    /*@Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("HEAD"), cancellable = true)
    public void onSetBlock(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        Level thisLevel = (Level) (Object) this;
        BlockState oldState = thisLevel.getBlockState(pos);
        System.out.println("setBlock 1 called at " + pos + " from " + oldState + " to " + newState);
        LOGGER.info("setBlock called 1 at " + pos + " from " + oldState + " to " + newState);
        if (newState.isAir()) {
            Block oldBlock = oldState.getBlock();

            if (!STATIC_ALLOWED_BLOCKS.contains(oldBlock) &&
                    !STATIC_ALLOWED_BLOCKS_CANNON.contains(oldBlock)) {
                LOGGER.warn("Interception: tentative de suppression non autorisée de " + oldBlock + " à " + pos);
                cir.setReturnValue(false);
            }
        }
    }*/

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    public void onSetBlock(BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        Level thisLevel = (Level) (Object) this;
        BlockState oldState = thisLevel.getBlockState(pos);
        System.out.println("setBlock 2 called at " + pos + " from " + oldState + " to " + newState);
        LOGGER.info("setBlock 2 called at " + pos + " from " + oldState + " to " + newState);
        Block oldBlock = oldState.getBlock();

        if (!STATIC_ALLOWED_BLOCKS.contains(oldBlock) &&
                !STATIC_ALLOWED_BLOCKS_CANNON.contains(oldBlock)) {
            LOGGER.warn("Interception: tentative de suppression non autorisée de " + oldBlock + " à " + pos);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
            at = @At("HEAD"))
    public void onRemoveBlock(BlockPos pos, boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
        Level thisLevel = (Level) (Object) this;
        BlockState oldState = thisLevel.getBlockState(pos);
        System.out.println("removeBlock called at " + pos + " removing " + oldState);
        LOGGER.info("removeBlock called at " + pos + " removing " + oldState);
    }

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
            at = @At("HEAD"), cancellable = true)
    public void onDestroyBlock(BlockPos pos, boolean dropItems, net.minecraft.world.entity.Entity entity, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        Level thisLevel = (Level) (Object) this;
        BlockState oldState = thisLevel.getBlockState(pos);
        System.out.println("destroyBlock called at " + pos + " destroying " + oldState + " by " + entity);
        LOGGER.info("destroyBlock called at " + pos + " destroying " + oldState + " by " + entity);
        Block block = oldState.getBlock();

        if (!STATIC_ALLOWED_BLOCKS.contains(block)
                && (entity == null || !STATIC_ALLOWED_MOBS.contains(entity.getType()))) {
            LOGGER.warn("Interception: destroyBlock interdit sur " + block + " à " + pos + " par " + entity);
            cir.setReturnValue(false);
        }
    }
}