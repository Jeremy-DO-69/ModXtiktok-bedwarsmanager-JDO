package com.jdo.BedwarsManager.License;

public class LicenseChecker {

    public static boolean verified = false;

    public static void setLicense(boolean value) {
        verified = value;
        System.out.println("[LicenseChecker] Licence définie sur : " + value);
    }

    public static boolean isLicenseValid() {
        return verified;
    }
}