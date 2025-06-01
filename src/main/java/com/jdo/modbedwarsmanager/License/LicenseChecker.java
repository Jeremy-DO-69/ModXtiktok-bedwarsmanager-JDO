package com.jdo.modbedwarsmanager.License;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.text2speech.Narrator.LOGGER;

public class LicenseChecker {

    public static final Map<UUID, Boolean> playerLicenses = new HashMap<>();

    public static boolean isLicenseValid(Player player) {
        return playerLicenses.getOrDefault(player.getUUID(), false);
    }

    public static void setLicenseValid(Player player, boolean status) {
        LOGGER.info("[LicenseChecker] Licence définie sur : " + status + " pour " + player.getName());
        playerLicenses.put(player.getUUID(), status);
    }
}