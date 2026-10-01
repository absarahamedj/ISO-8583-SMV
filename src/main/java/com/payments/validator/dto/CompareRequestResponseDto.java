package com.payments.validator.dto;

import com.payments.validator.model.ScenarioContext;

public class CompareRequestResponseDto {
    private String rawRequest;
    private String rawResponse;
    private boolean strip16ByteHeader = true;
    private ScenarioContext overrides;

    public CompareRequestResponseDto() {}

    public String getRawRequest() { return rawRequest; }
    public void setRawRequest(String rawRequest) { this.rawRequest = rawRequest; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    public boolean isStrip16ByteHeader() { return strip16ByteHeader; }
    public void setStrip16ByteHeader(boolean strip16ByteHeader) { this.strip16ByteHeader = strip16ByteHeader; }
    public ScenarioContext getOverrides() { return overrides; }
    public void setOverrides(ScenarioContext overrides) { this.overrides = overrides; }
}
