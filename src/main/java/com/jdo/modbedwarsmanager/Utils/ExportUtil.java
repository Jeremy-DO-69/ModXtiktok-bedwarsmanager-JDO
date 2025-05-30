package com.jdo.modbedwarsmanager.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;


public class ExportUtil {

    public static void exportBlockIdsBetween(Level level, Vec3 from, Vec3 to, File file) {
        BlockPos start = new BlockPos((int) from.x, (int) from.y, (int) from.z);
        BlockPos end = new BlockPos((int) to.x, (int) to.y, (int) to.z);

        Set<String> blockIds = new LinkedHashSet<>();

        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (id != null) {
                    blockIds.add("\"" + id.toString() + "\",");
                }
            }
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            blockIds.forEach(writer::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
