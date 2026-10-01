package com.payments.validator.dto;

import com.payments.validator.model.ScenarioContext;

public class ParseAndValidateRequest {
    private String rawMessage;
    private String inputFormat; // HEX, ASCII, EBCDIC, BCD, VTS_LOG
    private boolean strip16ByteHeader = true;
    private ScenarioContext overrides;

    public ParseAndValidateRequest() {}

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public boolean isStrip16ByteHeader() { return strip16ByteHeader; }
    public void setStrip16ByteHeader(boolean strip16ByteHeader) { this.strip16ByteHeader = strip16ByteHeader; }
    public ScenarioContext getOverrides() { return overrides; }
    public void setOverrides(ScenarioContext overrides) { this.overrides = overrides; }
}
