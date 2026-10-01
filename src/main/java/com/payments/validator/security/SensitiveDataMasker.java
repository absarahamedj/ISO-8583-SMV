package com.payments.validator.security;

import java.util.Set;

public class SensitiveDataMasker {

    private static final Set<String> SENSITIVE_EMV_TAGS = Set.of(
            "5A",   // Application PAN
            "57",   // Track 2 Equivalent Data
            "56",   // Track 1 Equivalent Data
            "9F1F", // Track 1 Discretionary Data
            "9F20", // Track 2 Discretionary Data
            "9F6B", // Track 2 Equivalent Data (MSD)
            "99"    // Transaction PIN
    );

    public static String maskPan(String pan) {
        if (pan == null || pan.trim().isEmpty()) return pan;
        String cleanPan = pan.replaceAll("[^0-9]", "");
        if (cleanPan.length() < 10) {
                return "******";
        }
        String bin = cleanPan.substring(0, 6);
        String last4 = cleanPan.substring(cleanPan.length() - 4);
        int maskLength = cleanPan.length() - 10;
        return bin + "*".repeat(maskLength) + last4;
    }

    public static String maskTrack2(String track2) {
        if (track2 == null || track2.trim().isEmpty()) return track2;

        int separatorIdx = track2.indexOf('=');
        if (separatorIdx == -1) separatorIdx = track2.indexOf('D');
        if (separatorIdx >6) {
            String panPart = track2.substring(0, separatorIdx);
            String maskedPan = maskPan(panPart);
            return maskedPan + "=****************";
        }
        return "********************";
    }

    public static String maskField(int fieldNumber, String value) {
        if (value == null) return null;
        switch (fieldNumber) {
            case 2: // PAN
                return maskPan(value);
            case 35: // Track 2
                return maskTrack2(value);
            case 45: // Track 1
                return "********************************";
            case 52: // PIN Data
            case 53: // Security Related Control Info
                return "[PROTECTED_PIN_BLOCK]";
            default:
                return value;
        }
    }

    public static String maskEmvTag(String tagHex, String rawHex) {
        if (rawHex == null) return null;
        if (SENSITIVE_EMV_TAGS.contains(tagHex.toUpperCase())) {
            if ("5A".equalsIgnoreCase(tagHex)) {
                return maskPan(rawHex);
            }
            return "[PROTECTED_EMV_TAG_DATA]";
        }
        return rawHex;
    }
}
