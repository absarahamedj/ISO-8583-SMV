package com.payments.validator.model;

import java.util.ArrayList;
import java.util.List;

public class EmvTag {
    private String tag;
    private String name;
    private int length;
    private String rawHex;
    private String maskedHex;
    private String decodedValue;
    private boolean constructed;
    private int startOffset;
    private int endOffset;
    private List<EmvTag> children = new ArrayList<>();

    public EmvTag() {}

    public EmvTag(String tag, String name, int length, String rawHex, String maskedHex, boolean constructed, int startOffset, int endOffset) {
        this.tag = tag;
        this.name = name;
        this.length = length;
        this.rawHex = rawHex;
        this.maskedHex = maskedHex;
        this.constructed = constructed;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
    public String getRawHex() { return rawHex; }
    public void setRawHex(String rawHex) { this.rawHex = rawHex; }
    public String getMaskedHex() { return maskedHex; }
    public void setMaskedHex(String maskedHex) { this.maskedHex = maskedHex; }
    public String getDecodedValue() { return decodedValue; }
    public void setDecodedValue(String decodedValue) { this.decodedValue = decodedValue; }
    public boolean isConstructed() { return constructed; }
    public void setConstructed(boolean constructed) { this.constructed = constructed; }
    public int getStartOffset() { return startOffset; }
    public void setStartOffset(int startOffset) { this.startOffset = startOffset; }
    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }
    public List<EmvTag> getChildren() { return children; }
    public void setChildren(List<EmvTag> children) { this.children = children; }
}
