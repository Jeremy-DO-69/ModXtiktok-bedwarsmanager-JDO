package com.jdo.modbedwarsmanager.License;

import static com.mojang.text2speech.Narrator.LOGGER;

public class LicenseChecker {

    public static boolean verified = false;

    public static void setLicense(boolean value) {
        verified = value;
        System.out.println("[LicenseChecker] Licence définie sur : " + value);
        LOGGER.info("[LicenseChecker] Licence définie sur : " + value);
    }

    public static boolean isLicenseValid() {
        return verified;
    }
}