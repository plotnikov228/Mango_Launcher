package com.mango.mango_tv.Utils;

public class VersionComparator {
    public static boolean compareVersions(String currentVersion, String newVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] newParts = newVersion.split("\\.");

        int length = Math.max(currentParts.length, newParts.length);
        for (int i = 0; i < length; i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int newPart = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;

            if (currentPart < newPart) {
                return true;
            } else if (currentPart > newPart) {
                return false;
            }
        }

        return false;
    }
}
