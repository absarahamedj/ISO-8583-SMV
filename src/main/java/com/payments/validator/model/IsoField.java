package com.payments.validator.model;

import java.util.*;

public class IsoField {
    private int fieldNumber;
    private String path; // e.g. "2", "55.9F26", "62.23"
    private String name;
    private String rawValue;
    private String decodedValue;
    private String maskedValue;
    private String encoding; // ASCII, EBCDIC, BCD, BINARY
    private int length;
    private int startOffset;
    private int endOffset;
    private boolean validParse = true;
    private String parseError;
    private Map<String, String> subfields = new LinkedHashMap<>();
    private List<EmvTag> emvTags = new ArrayList<>();

    public IsoField() {}

    public IsoField(int fieldNumber, String name, String decodedValue, String maskedValue, int startOffset, int endOffset) {
        this.fieldNumber = fieldNumber;
        this.path = String.valueOf(fieldNumber);
        this.name = name;
        this.decodedValue = decodedValue;
        this.maskedValue = maskedValue;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.length = (decodedValue != null) ? decodedValue.length() : 0;
    }

    public int getFieldNumber() { return fieldNumber; }
    public void setFieldNumber(int fieldNumber) { this.fieldNumber = fieldNumber; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
    public String getDecodedValue() { return decodedValue; }
    public void setDecodedValue(String decodedValue) { this.decodedValue = decodedValue; }
    public String getMaskedValue() { return maskedValue; }
    public void setMaskedValue(String maskedValue) { this.maskedValue = maskedValue; }
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
    public int getStartOffset() { return startOffset; }
    public void setStartOffset(int startOffset) { this.startOffset = startOffset; }
    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }
    public boolean isValidParse() { return validParse; }
    public void setValidParse(boolean validParse) { this.validParse = validParse; }
    public String getParseError() { return parseError; }
    public void setParseError(String parseError) { this.parseError = parseError; }
    public Map<String, String> getSubfields() { return subfields; }
    public void setSubfields(Map<String, String> subfields) { this.subfields = subfields; }
    public List<EmvTag> getEmvTags() { return emvTags; }
    public void setEmvTags(List<EmvTag> emvTags) { this.emvTags = emvTags; }

    public EmvTag findTag(String tagHex) {
        if (emvTags == null) return null;
        for (EmvTag tag : emvTags) {
            if (tag.getTag().equalsIgnoreCase(tagHex)) return tag;
            for (EmvTag child : tag.getChildren()) {
                if (child.getTag().equalsIgnoreCase(tagHex)) return child;
            }
        }
        return null;
    }
}
