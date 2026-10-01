package com.payments.validator.parser;

import com.payments.validator.model.EmvTag;
import com.payments.validator.security.SensitiveDataMasker;

import java.util.*;

public class BerTlvParser {

    private static final Map<String, String> TAG_NAMES = new HashMap<>();

    static {
        TAG_NAMES.put("9F26", "Application Cryptogram (AC)");
        TAG_NAMES.put("9F42", "Application Currency Code");
        TAG_NAMES.put("9F27", "Cryptogram Information Data (CID)");
        TAG_NAMES.put("9F10", "Issuer Application Data (IAD)");
        TAG_NAMES.put("9F37", "Unpredictable Number (UN)");
        TAG_NAMES.put("9F36", "Application Transaction Counter (ATC)");
        TAG_NAMES.put("95",   "Terminal Verification Results (TVR)");
        TAG_NAMES.put("9A",   "Transaction Date");
        TAG_NAMES.put("9C",   "Transaction Type");
        TAG_NAMES.put("9F02", "Amount, Authorized (Numeric)");
        TAG_NAMES.put("9F03", "Amount, Other (Numeric)");
        TAG_NAMES.put("5F2A", "Transaction Currency Code");
        TAG_NAMES.put("82",   "Application Interchange Profile (AIP)");
        TAG_NAMES.put("9F1A", "Terminal Country Code");
        TAG_NAMES.put("9F34", "Cardholder Verification Method (CVM) Results");
        TAG_NAMES.put("9F33", "Terminal Capabilities");
        TAG_NAMES.put("9F35", "Terminal Type");
        TAG_NAMES.put("9F1E", "Interface Device (IFD) Serial Number");
        TAG_NAMES.put("84",   "Dedicated File (DF) Name / AID");
        TAG_NAMES.put("5F34", "Application PAN Sequence Number");
        TAG_NAMES.put("9F09", "Application Version Number");
        TAG_NAMES.put("9F63", "Card Product Type Information");
        TAG_NAMES.put("91",   "Issuer Authentication Data");
        TAG_NAMES.put("71",   "Issuer Script Template 1");
        TAG_NAMES.put("72",   "Issuer Script Template 2");
        TAG_NAMES.put("89",   "Authorization Code");
        TAG_NAMES.put("8A",   "Authorization Response Code (ARC)");
    }

    public static List<EmvTag> parseTlv(String hexString, int baseByteOffset) {
        List<EmvTag> tags = new ArrayList<>();
        if (hexString == null || hexString.trim().isEmpty()) {
            return tags;
        }

        byte[] bytes = hexStringToByteArray(hexString);
        int index = 0;

        while (index < bytes.length) {
            // Skip trailing padding bytes (0x00 or 0xFF)
            if (bytes[index] == 0x00 || (bytes[index] & 0xFF) == 0xFF) {
                index++;
                continue;
            }

            int tagStart = index;

            // 1. Parse Tag
            int b1 = bytes[index++] & 0xFF;
            boolean constructed = (b1 & 0x20) != 0;
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(String.format("%02X", b1));

            if ((b1 & 0x1F) == 0x1F) { // Subsequent bytes follow
                while (index < bytes.length) {
                    int nextB = bytes[index++] & 0xFF;
                    tagBuilder.append(String.format("%02X", nextB));
                    if ((nextB & 0x80) == 0) {
                        break; // End of multi-byte tag
                    }
                }
            }
            String tagHex = tagBuilder.toString().toUpperCase();

            if (index >= bytes.length) {
                break; // Incomplete TLV
            }

            // 2. Parse Length
            int lenByte = bytes[index++] & 0xFF;
            int length = 0;
            if ((lenByte & 0x80) == 0) {
                length = lenByte;
            } else {
                int numLenBytes = lenByte & 0x7F;
                for (int i = 0; i < numLenBytes && index < bytes.length; i++) {
                    length = (length << 8) | (bytes[index++] & 0xFF);
                }
            }

            // 3. Parse Value
            if (index + length > bytes.length) {
                length = bytes.length - index; // Truncated tag value
            }

            byte[] valBytes = Arrays.copyOfRange(bytes, index, index + length);
            index += length;
            int tagEnd = index;

            String valHex = byteArrayToHexString(valBytes);
            String tagName = TAG_NAMES.getOrDefault(tagHex, "EMV Tag " + tagHex);
            String maskedHex = SensitiveDataMasker.maskEmvTag(tagHex, valHex);

            EmvTag emvTag = new EmvTag(
                    tagHex,
                    tagName,
                    length,
                    valHex,
                    maskedHex,
                    constructed,
                    baseByteOffset + tagStart,
                    baseByteOffset + tagEnd
            );

            // If constructed tag, recurse
            if (constructed && length > 0) {
                emvTag.setChildren(parseTlv(valHex, baseByteOffset + tagStart + (tagEnd - tagStart - length)));
            }

            tags.add(emvTag);
        }

        return tags;
    }

    private static byte[] hexStringToByteArray(String s) {
        String clean = s.replaceAll("[^0-9A-Fa-f]", "");
        int len = clean.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(clean.charAt(i), 16) << 4)
                    + Character.digit(clean.charAt(i + 1), 16));
        }
        return data;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
