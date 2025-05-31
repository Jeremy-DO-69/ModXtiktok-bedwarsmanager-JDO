package com.jdo.modbedwarsmanager.mixin;
import com.jdo.modbedwarsmanager.ModBedwarsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
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

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    public void onSetBlock(BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        if (!replacingArena) {
            Level thisLevel = (Level) (Object) this;
            BlockState oldState = thisLevel.getBlockState(pos);
            Block oldBlock = oldState.getBlock();
            Block newBlock = newState.getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(oldState.getBlock());
            if (!newState.isAir() && newBlock instanceof DoorBlock && oldBlock instanceof DoorBlock) {
                return;
            }

            if (!oldState.isAir()) {
                if (!isBlockAllowed(id.toString(), STATIC_ALLOWED_BLOCKS) &&
                        !isBlockAllowed(id.toString(), STATIC_ALLOWED_BLOCKS_CANNON)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
            at = @At("HEAD"), cancellable = true)
    public void onDestroyBlock(BlockPos pos, boolean dropItems, net.minecraft.world.entity.Entity entity, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        if (!replacingArena) {
            Level thisLevel = (Level) (Object) this;
            BlockState oldState = thisLevel.getBlockState(pos);
            Block block = oldState.getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(oldState.getBlock());
            System.out.println("destroyBlock called at " + pos + " destroying " + oldState + " by " + entity + "id : " + id.toString());
            LOGGER.info("destroyBlock called at " + pos + " destroying " + oldState + " by " + entity + "id : " + id.toString());
            if (!isBlockAllowed(id.toString(), STATIC_ALLOWED_BLOCKS)
                    && (entity == null || !isBlockAllowed(id.toString(), STATIC_ALLOWED_MOBS))) {
                LOGGER.warn("Interception: destroyBlock interdit sur " + block + " à " + pos + " par " + entity);
                cir.setReturnValue(false);
            }
        }
    }
}