package com.jdo.modbedwarsmanager.Utils;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.EditSession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.io.FileInputStream;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ArenaUtils {
    //House
    //-28 2 56
    //28 -20 -56
    //Arena
    //46 38 -75
    //-46 -23 75
    public static void loadSchematic(File file, ServerLevel level, BlockPos origin) {
        LOGGER.info("loadSchematic");
        LOGGER.info("Working directory: " + System.getProperty("user.dir"));
        try {
            LOGGER.info("Trying to load file from: " + file.getAbsolutePath());
            if (file != null && file.exists()) {
                LOGGER.info("Le fichier existe !");
            } else {
                LOGGER.info("Le fichier est nul ou introuvable !");
            }
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                LOGGER.info("Unsupported schematic format: " + file.getName());
                return;
            }
            ClipboardReader reader = format.getReader(new FileInputStream(file));
            Clipboard clipboard = reader.read();

            World weWorld = ForgeAdapter.adapt(level);
            BlockVector3 pasteAt = BlockVector3.at(origin.getX(), origin.getY(), origin.getZ());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operations.complete(new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pasteAt)
                        .ignoreAirBlocks(true)
                        .build());
                LOGGER.info("✅ Schematic loaded at " + origin);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Error: " + e);
        }
    }

    public static void clearArea(ServerLevel level, BlockPos from, BlockPos to) {
        BlockPos min = new BlockPos(
                Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()),
                Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(
                Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()),
                Math.max(from.getZ(), to.getZ()));

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        LOGGER.info("🧹 Arène nettoyée de " + from + " à " + to);
    }
}