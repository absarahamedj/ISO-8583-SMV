package com.payments.validator.model;

import java.util.*;

public class IsoMessage {
    private String rawInput;
    private String inputFormat; // HEX, ASCII, EBCDIC, BCD, VTS_LOG
    private String transportHeaderHex;
    private String mti;
    private String primaryBitmapHex;
    private String secondaryBitmapHex;
    private String tertiaryBitmapHex;
    private String direction; // REQUEST, RESPONSE, REVERSAL, ADVICE
    private Map<Integer, IsoField> fields = new TreeMap<>();
    private List<String> parseWarnings = new ArrayList<>();
    private List<String> parseErrors = new ArrayList<>();
    private boolean fullyParsed = false;
    private int totalBytesParsed = 0;

    public IsoMessage() {}

    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public String getTransportHeaderHex() { return transportHeaderHex; }
    public void setTransportHeaderHex(String transportHeaderHex) { this.transportHeaderHex = transportHeaderHex; }
    public String getMti() { return mti; }
    public void setMti(String mti) { this.mti = mti; }
    public String getPrimaryBitmapHex() { return primaryBitmapHex; }
    public void setPrimaryBitmapHex(String primaryBitmapHex) { this.primaryBitmapHex = primaryBitmapHex; }
    public String getSecondaryBitmapHex() { return secondaryBitmapHex; }
    public void setSecondaryBitmapHex(String secondaryBitmapHex) { this.secondaryBitmapHex = secondaryBitmapHex; }
    public String getTertiaryBitmapHex() { return tertiaryBitmapHex; }
    public void setTertiaryBitmapHex(String tertiaryBitmapHex) { this.tertiaryBitmapHex = tertiaryBitmapHex; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public Map<Integer, IsoField> getFields() { return fields; }
    public void setFields(Map<Integer, IsoField> fields) { this.fields = fields; }
    public List<String> getParseWarnings() { return parseWarnings; }
    public void setParseWarnings(List<String> parseWarnings) { this.parseWarnings = parseWarnings; }
    public List<String> getParseErrors() { return parseErrors; }
    public void setParseErrors(List<String> parseErrors) { this.parseErrors = parseErrors; }
    public boolean isFullyParsed() { return fullyParsed; }
    public void setFullyParsed(boolean fullyParsed) { this.fullyParsed = fullyParsed; }
    public int getTotalBytesParsed() { return totalBytesParsed; }
    public void setTotalBytesParsed(int totalBytesParsed) { this.totalBytesParsed = totalBytesParsed; }

    public boolean hasField(int fieldNumber) {
        return fields.containsKey(fieldNumber);
    }

    public IsoField getField(int fieldNumber) {
        return fields.get(fieldNumber);
    }

    public String getFieldValue(int fieldNumber) {
        IsoField f = fields.get(fieldNumber);
        return (f != null) ? f.getDecodedValue() : null;
    }

    public void addField(IsoField field) {
        this.fields.put(field.getFieldNumber(), field);
    }
}
