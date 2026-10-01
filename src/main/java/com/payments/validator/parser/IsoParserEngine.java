package com.payments.validator.parser;

import com.payments.validator.model.EmvTag;
import com.payments.validator.model.IsoField;
import com.payments.validator.model.IsoMessage;
import com.payments.validator.security.SensitiveDataMasker;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoParserEngine {

    private final FieldDefinitionRegistry registry = new FieldDefinitionRegistry();
    private static final Pattern DISPLACEMENT_LINE_PATTERN = Pattern.compile("^\\s*([0-9A-Fa-f]{4})\\s+((?:[0-9A-Fa-f]{8}\\s*)+|[0-9A-Fa-f\\s]+)$");

    public IsoMessage parse(String rawInput, boolean strip16ByteHeader) {
        IsoMessage message = new IsoMessage();
        message.setRawInput(rawInput);

        if (rawInput == null || rawInput.trim().isEmpty()) {
            message.getParseErrors().add("Input message is empty");
            return message;
        }

        String trimmed = rawInput.trim();

        // 1. Detect VTS Formatted Text Log (with MTI:, F2:, F3:, etc.)
        if (isVtsTextLogFormat(trimmed)) {
            message.setInputFormat("VTS_LOG");
            parseVtsLogFormat(trimmed, message);
            return message;
        }

        // 2. Detect and extract VTS Exported Raw Hex Dump (with Displacement 0000, 0010, etc.)
        String hexClean;
        if (isVtsDisplacementDump(trimmed)) {
            message.setInputFormat("VTS_HEX_DUMP");
            hexClean = extractHexFromVtsDisplacementDump(trimmed);
        } else {
            hexClean = trimmed.replaceAll("\\s+", "");
            message.setInputFormat("HEX");
        }

        if (hexClean.isEmpty()) {
            message.getParseErrors().add("No valid hexadecimal characters extracted from message.");
            return message;
        }

        int offset = 0;

        // 3. Intelligent Transport Header Detection
        // VTS headers can be 22 bytes (44 hex chars, often starting with 16) or 16 bytes (32 hex chars)
        if (hexClean.length() >= 48 && isKnownMti(hexClean.substring(44, 48))) {
            // 22-byte (44 hex char) VTS transport header detected
            message.setTransportHeaderHex(hexClean.substring(0, 44));
            offset = 44;
        } else if (hexClean.length() >= 36 && isKnownMti(hexClean.substring(32, 36))) {
            // 16-byte (32 hex char) transport header detected
            message.setTransportHeaderHex(hexClean.substring(0, 32));
            offset = 32;
        } else if (strip16ByteHeader && hexClean.length() >= 32 && !isKnownMti(hexClean.substring(0, 4))) {
            message.setTransportHeaderHex(hexClean.substring(0, 32));
            offset = 32;
        }

        // 4. Parse MTI
        if (hexClean.length() < offset + 4) {
            message.getParseErrors().add("Message too short to contain MTI");
            return message;
        }

        String mti;
        boolean isAsciiMti = false;
        if (hexClean.length() >= offset + 8 &&
            (hexClean.substring(offset).startsWith("30") || hexClean.substring(offset).startsWith("31") ||
             hexClean.substring(offset).startsWith("32") || hexClean.substring(offset).startsWith("34"))) {
            mti = hexToAscii(hexClean.substring(offset, offset + 8));
            offset += 8;
            isAsciiMti = true;
        } else {
            mti = hexClean.substring(offset, offset + 4);
            offset += 4;
        }
        message.setMti(mti);
        message.setDirection(determineDirection(mti));

        // 5. Parse Primary Bitmap (16 hex chars = 64 bits)
        if (hexClean.length() < offset + 16) {
            message.getParseErrors().add("Message too short to contain primary bitmap");
            return message;
        }

        String primaryBitmapHex = hexClean.substring(offset, offset + 16);
        offset += 16;
        message.setPrimaryBitmapHex(primaryBitmapHex);

        boolean hasSecondary = isBitSet(primaryBitmapHex, 1);
        String secondaryBitmapHex = null;
        if (hasSecondary) {
            if (hexClean.length() < offset + 16) {
                message.getParseErrors().add("Bit 1 set but insufficient bytes for secondary bitmap");
                return message;
            }
            secondaryBitmapHex = hexClean.substring(offset, offset + 16);
            offset += 16;
            message.setSecondaryBitmapHex(secondaryBitmapHex);
        }

        BitSet activeFields = buildBitSet(primaryBitmapHex, secondaryBitmapHex);

        // Detect if payload uses Visa Base I packed BCD / EBCDIC encoding
        boolean isPackedBcdEbcdic = !isAsciiMti && detectPackedBcdEbcdic(hexClean, offset);

        // 6. Parse Fields in ascending DE order
        for (int de = 2; de <= (hasSecondary ? 128 : 64); de++) {
            if (!activeFields.get(de)) {
                continue;
            }

            FieldDefinitionRegistry.FieldDef def = registry.getDefinition(de);
            int fieldStart = offset / 2;

            try {
                if (isPackedBcdEbcdic) {
                    offset = parsePackedField(de, def, hexClean, offset, fieldStart, message);
                } else {
                    offset = parseAsciiOrBinaryField(de, def, hexClean, offset, fieldStart, message);
                }
            } catch (Exception ex) {
                message.getParseErrors().add("Error parsing DE " + de + " at byte offset " + (offset / 2) + ": " + ex.getMessage());
                break;
            }
        }

        message.setFullyParsed(message.getParseErrors().isEmpty());
        message.setTotalBytesParsed(offset / 2);
        return message;
    }

    private boolean isKnownMti(String code) {
        return code.equals("0100") || code.equals("0110") || code.equals("0120") || code.equals("0130") ||
               code.equals("0200") || code.equals("0210") || code.equals("0220") || code.equals("0230") ||
               code.equals("0400") || code.equals("0410") || code.equals("0420") || code.equals("0800") || code.equals("0810");
    }

    private boolean isVtsDisplacementDump(String input) {
        if (input.contains("Raw Hex Dump") || input.contains("VTS Exported Raw Message")) {
            return true;
        }
        if (input.contains("Displacement")) {
            return true;
        }
        // Check if the input actually contains displacement-format hex lines (e.g., "0000  16010200")
        return DISPLACEMENT_LINE_PATTERN.matcher(input).find();
    }

    private String extractHexFromVtsDisplacementDump(String input) {
        StringBuilder hex = new StringBuilder();
        String[] lines = input.split("\\r?\\n");
        boolean inDump = false;

        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("---") || l.startsWith("===") ||
                l.startsWith("Date") || l.startsWith("Time") ||
                l.startsWith("VTS Exported") || l.contains("00+") || l.contains("04+") ||
                l.contains("----") || l.contains("____")) {
                continue;
            }
            if (l.startsWith("Displacement") || l.contains("Raw Hex Dump")) {
                inDump = true;
                continue;
            }

            // Match displacement line like "0000 16010200 7D000000 ..."
            Matcher matcher = DISPLACEMENT_LINE_PATTERN.matcher(l);
            if (matcher.matches()) {
                inDump = true;
                String hexContent = matcher.group(2).replaceAll("\\s+", "");
                hex.append(hexContent);
            } else if (inDump && l.matches("^[0-9A-Fa-f\\s]+$")) {
                hex.append(l.replaceAll("\\s+", ""));
            }
        }

        return hex.toString();
    }

    private boolean detectPackedBcdEbcdic(String hex, int offset) {
        // In packed Visa Base I, DE 2 starts with a 1-byte BCD length prefix like "10" or "16"
        // and string fields like DE 37, DE 41, DE 42 contain EBCDIC bytes (0xF0-F9, 0xC1-C9, 0x40)
        return hex.length() > offset && (hex.substring(offset).contains("F6F2") || hex.substring(offset).contains("C3C1"));
    }

    private int parsePackedField(int de, FieldDefinitionRegistry.FieldDef def, String hexClean, int offset, int fieldStart, IsoMessage message) {
        int lengthChars;
        String decodedVal;
        String rawFieldHex;

        if (de == 2) { // PAN (LLVAR BCD, 1-byte length prefix = 2 hex chars, value is hex)
            int numDigits = Integer.parseInt(hexClean.substring(offset, offset + 2), 16);
            offset += 2;
            int bytesNeeded = (numDigits % 2 == 0) ? numDigits : numDigits + 1;
            rawFieldHex = hexClean.substring(offset, offset + bytesNeeded);
            decodedVal = rawFieldHex.substring(0, numDigits);
            offset += bytesNeeded;
        } else if (de == 3) { // Processing Code: 6 digits BCD = 3 bytes = 6 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 6);
            decodedVal = rawFieldHex;
            offset += 6;
        } else if (de == 4) { // Amount: 12 digits BCD = 6 bytes = 12 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 12);
            decodedVal = rawFieldHex;
            offset += 12;
        } else if (de == 7) { // Date/Time: 10 digits BCD = 5 bytes = 10 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 10);
            decodedVal = rawFieldHex;
            offset += 10;
        } else if (de == 11) { // STAN: 6 digits BCD = 3 bytes = 6 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 6);
            decodedVal = rawFieldHex;
            offset += 6;
        } else if (de == 12) { // Time: 6 digits BCD = 3 bytes = 6 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 6);
            decodedVal = rawFieldHex;
            offset += 6;
        } else if (de == 13 || de == 14) { // Date: 4 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex;
            offset += 4;
        } else if (de == 18) { // MCC: 4 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex;
            offset += 4;
        } else if (de == 19) { // Acquiring Country: 3 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex;
            offset += 4;
        } else if (de == 22) { // POS Entry Mode: 4 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex;
            offset += 4;
        } else if (de == 23) { // Card Sequence Number: 3 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex;
            offset += 4;
        } else if (de == 25) { // POS Condition: 2 digits BCD = 1 byte = 2 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 2);
            decodedVal = rawFieldHex;
            offset += 2;
        } else if (de == 32) { // Acquiring Inst ID: LLVAR BCD
            int lenDigits = Integer.parseInt(hexClean.substring(offset, offset + 2), 16);
            offset += 2;
            int hexLen = (lenDigits % 2 == 0) ? lenDigits : lenDigits + 1;
            rawFieldHex = hexClean.substring(offset, offset + hexLen);
            decodedVal = rawFieldHex.substring(0, lenDigits);
            offset += hexLen;
        } else if (de == 37) { // RRN: 12 chars EBCDIC = 12 bytes = 24 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 24);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 24;
        } else if (de == 38) { // Auth Code: 6 chars EBCDIC = 6 bytes = 12 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 12);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 12;
        } else if (de == 39) { // Response Code: 2 chars EBCDIC = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 4;
        } else if (de == 41) { // Terminal ID: 8 chars EBCDIC = 8 bytes = 16 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 16);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 16;
        } else if (de == 42) { // Acceptor ID: 15 chars EBCDIC = 15 bytes = 30 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 30);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 30;
        } else if (de == 43) { // Acceptor Name: 40 chars EBCDIC = 40 bytes = 80 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 80);
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += 80;
        } else if (de == 44) { // Additional Response Data: LLVAR EBCDIC
            int byteLen = Integer.parseInt(hexClean.substring(offset, offset + 2), 16);
            offset += 2;
            rawFieldHex = hexClean.substring(offset, offset + (byteLen * 2));
            decodedVal = ebcdicToAscii(hexStringToByteArray(rawFieldHex));
            offset += (byteLen * 2);
        } else if (de == 49) { // Currency Code: 3 digits BCD = 2 bytes = 4 hex chars
            rawFieldHex = hexClean.substring(offset, offset + 4);
            decodedVal = rawFieldHex.substring(1);
            offset += 4;
        } else if (de == 55) { // ICC Data: LLLVAR Binary
            int byteLen = Integer.parseInt(hexClean.substring(offset, offset + 4), 16);
            offset += 4;
            rawFieldHex = hexClean.substring(offset, offset + (byteLen * 2));
            decodedVal = rawFieldHex;
            offset += (byteLen * 2);
        } else if (de == 62 || de == 63) { // Private Fields
            if (offset + 4 <= hexClean.length()) {
                int byteLen = Integer.parseInt(hexClean.substring(offset, offset + 4), 16);
                offset += 4;
                int hexNeeded = Math.min(byteLen * 2, hexClean.length() - offset);
                rawFieldHex = hexClean.substring(offset, offset + hexNeeded);
                decodedVal = rawFieldHex;
                offset += hexNeeded;
            } else {
                rawFieldHex = hexClean.substring(offset);
                decodedVal = rawFieldHex;
                offset = hexClean.length();
            }
        } else {
            // Generic fallback
            int take = Math.min(def.getFixedLengthOrMax() * 2, hexClean.length() - offset);
            rawFieldHex = hexClean.substring(offset, offset + take);
            decodedVal = rawFieldHex;
            offset += take;
        }

        int fieldEnd = offset / 2;
        String maskedVal = SensitiveDataMasker.maskField(de, decodedVal);
        IsoField field = new IsoField(de, def.getName(), decodedVal, maskedVal, fieldStart, fieldEnd);
        field.setRawValue(rawFieldHex);
        field.setEncoding("VISA_BASE_I");

        if (de == 55) {
            field.setEmvTags(BerTlvParser.parseTlv(rawFieldHex, fieldStart));
        }

        message.addField(field);
        return offset;
    }

    private int parseAsciiOrBinaryField(int de, FieldDefinitionRegistry.FieldDef def, String hexClean, int offset, int fieldStart, IsoMessage message) {
        int fieldLength;
        if (def.getLengthType() == FieldDefinitionRegistry.LengthType.FIXED) {
            fieldLength = def.getFixedLengthOrMax();
        } else if (def.getLengthType() == FieldDefinitionRegistry.LengthType.LLVAR) {
            String lenStr = hexToAsciiOrDirect(hexClean.substring(offset, offset + 4));
            fieldLength = Integer.parseInt(lenStr);
            offset += 4;
        } else if (def.getLengthType() == FieldDefinitionRegistry.LengthType.LLLVAR) {
            String lenStr = hexToAsciiOrDirect(hexClean.substring(offset, offset + 6));
            fieldLength = Integer.parseInt(lenStr);
            offset += 6;
        } else {
            String lenStr = hexToAsciiOrDirect(hexClean.substring(offset, offset + 8));
            fieldLength = Integer.parseInt(lenStr);
            offset += 8;
        }

        int hexCharsNeeded = fieldLength * 2;
        if (hexClean.length() < offset + hexCharsNeeded) {
            hexCharsNeeded = hexClean.length() - offset;
            message.getParseWarnings().add("Field DE " + de + " truncated: expected " + fieldLength + " bytes");
        }

        String rawFieldHex = hexClean.substring(offset, offset + hexCharsNeeded);
        offset += hexCharsNeeded;
        int fieldEnd = offset / 2;

        String decodedVal = (def.getDataType() == FieldDefinitionRegistry.DataType.BINARY) ?
                rawFieldHex : hexToAscii(rawFieldHex);
        String maskedVal = SensitiveDataMasker.maskField(de, decodedVal);

        IsoField field = new IsoField(de, def.getName(), decodedVal, maskedVal, fieldStart, fieldEnd);
        field.setRawValue(rawFieldHex);
        field.setEncoding(def.getDataType().name());

        if (de == 55) {
            field.setEmvTags(BerTlvParser.parseTlv(rawFieldHex, fieldStart));
        }

        message.addField(field);
        return offset;
    }

    private boolean isVtsTextLogFormat(String input) {
        return input.contains("MTI:") || input.contains("F2:") || input.contains("F3:") || input.contains("DE 3") || input.contains("Expected, But Not Received");
    }

    private void parseVtsLogFormat(String logText, IsoMessage message) {
        String[] lines = logText.split("\\r?\\n");
        Pattern dePattern = Pattern.compile("^(?:F|DE\\s*)(\\d+)(?:\\.([0-9A-Fa-f]+))?\\s*[:=]\\s*(.*)$");

        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("#") || l.startsWith("//")) continue;

            if (l.toUpperCase().startsWith("MTI")) {
                String[] parts = l.split("[:=]");
                if (parts.length > 1) {
                    String mti = parts[1].trim();
                    message.setMti(mti);
                    message.setDirection(determineDirection(mti));
                }
                continue;
            }

            if (l.toUpperCase().startsWith("BITMAP")) {
                String[] parts = l.split("[:=]");
                if (parts.length > 1) {
                    message.setPrimaryBitmapHex(parts[1].trim());
                }
                continue;
            }

            if (l.contains("Expected, But Not Received")) {
                message.getParseWarnings().add("VTS Simulator Annotation: " + l);
                continue;
            }

            Matcher matcher = dePattern.matcher(l);
            if (matcher.matches()) {
                int deNum = Integer.parseInt(matcher.group(1));
                String subfieldOrTag = matcher.group(2);
                String val = matcher.group(3).trim();

                FieldDefinitionRegistry.FieldDef def = registry.getDefinition(deNum);
                IsoField field = message.getField(deNum);
                if (field == null) {
                    String masked = SensitiveDataMasker.maskField(deNum, val);
                    field = new IsoField(deNum, def.getName(), val, masked, 0, val.length());
                    message.addField(field);
                }

                if (subfieldOrTag != null && !subfieldOrTag.isEmpty()) {
                    field.getSubfields().put(subfieldOrTag, val);
                    if (deNum == 55) {
                        field.getEmvTags().add(new EmvTag(subfieldOrTag, "Tag " + subfieldOrTag, val.length() / 2, val, SensitiveDataMasker.maskEmvTag(subfieldOrTag, val), false, 0, val.length()));
                    }
                }
            }
        }

        if (message.getMti() == null && message.hasField(3)) {
            message.setMti("0100");
        }
        message.setFullyParsed(true);
    }

    private String determineDirection(String mti) {
        if (mti == null || mti.length() < 3) return "UNKNOWN";
        char secondDigit = mti.charAt(2);
        return (secondDigit == '0' || secondDigit == '2') ? "REQUEST" : "RESPONSE";
    }

    private BitSet buildBitSet(String primaryHex, String secondaryHex) {
        BitSet bs = new BitSet(129);
        populateBitSetFromHex(bs, primaryHex, 1);
        if (secondaryHex != null && !secondaryHex.isEmpty()) {
            populateBitSetFromHex(bs, secondaryHex, 65);
        }
        return bs;
    }

    private void populateBitSetFromHex(BitSet bs, String hex, int startBit) {
        for (int i = 0; i < hex.length(); i++) {
            int nibble = Character.digit(hex.charAt(i), 16);
            for (int b = 0; b < 4; b++) {
                if ((nibble & (1 << (3 - b))) != 0) {
                    bs.set(startBit + (i * 4) + b);
                }
            }
        }
    }

    private boolean isBitSet(String hex, int bit1Indexed) {
        int nibbleIdx = (bit1Indexed - 1) / 4;
        int bitInNibble = (bit1Indexed - 1) % 4;
        if (nibbleIdx >= hex.length()) return false;
        int nibble = Character.digit(hex.charAt(nibbleIdx), 16);
        return (nibble & (1 << (3 - bitInNibble))) != 0;
    }

    private String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexStr.length() - 1; i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private String hexToAsciiOrDirect(String hexStr) {
        try {
            return hexToAscii(hexStr);
        } catch (Exception e) {
            return hexStr;
        }
    }

    private String ebcdicToAscii(byte[] bytes) {
        try {
            return new String(bytes, Charset.forName("Cp1047"));
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int val = b & 0xFF;
                if (val == 0x40) sb.append(' ');
                else if (val == 0x4B) sb.append('.');
                else if (val >= 0xF0 && val <= 0xF9) sb.append((char) ('0' + (val - 0xF0)));
                else if (val >= 0xC1 && val <= 0xC9) sb.append((char) ('A' + (val - 0xC1)));
                else if (val >= 0xD1 && val <= 0xD9) sb.append((char) ('J' + (val - 0xD1)));
                else if (val >= 0xE2 && val <= 0xE9) sb.append((char) ('S' + (val - 0xE2)));
                else sb.append((char) val);
            }
            return sb.toString();
        }
    }

    private byte[] hexStringToByteArray(String s) {
        String clean = s.replaceAll("[^0-9A-Fa-f]", "");
        int len = clean.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(clean.charAt(i), 16) << 4)
                    + Character.digit(clean.charAt(i + 1), 16));
        }
        return data;
    }
}
