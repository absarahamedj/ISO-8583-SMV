package com.payments.validator.model;

import java.util.ArrayList;
import java.util.List;

public class ScenarioContext {
    private String network; // VISA, MASTERCARD, COMMON, CUSTOM
    private String ruleProfile;
    private String ruleProfileVersion;
    private String region; // GLOBAL, US, EU, PH, etc.
    private String direction; // REQUEST, RESPONSE
    private String mti;
    private String transactionFamily; // CASH_WITHDRAWAL, PURCHASE, BALANCE_INQUIRY, REVERSAL
    private String processingCode;
    private String channel; // ATM, POS, ECOMMERCE
    private String posEntryMode;
    private String cardReadMethod; // CHIP, CONTACTLESS_CHIP, CONTACTLESS_MAGSTRIPE, MAGSTRIPE, MANUAL
    private String cardProduct;
    private String terminalProfile;
    private double confidenceScore = 1.0;
    private boolean autoDetected = true;
    private boolean userOverridden = false;
    private List<String> conflicts = new ArrayList<>();
    private List<String> appliedRuleStack = new ArrayList<>();

    public ScenarioContext() {}

    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getRuleProfile() { return ruleProfile; }
    public void setRuleProfile(String ruleProfile) { this.ruleProfile = ruleProfile; }
    public String getRuleProfileVersion() { return ruleProfileVersion; }
    public void setRuleProfileVersion(String ruleProfileVersion) { this.ruleProfileVersion = ruleProfileVersion; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getMti() { return mti; }
    public void setMti(String mti) { this.mti = mti; }
    public String getTransactionFamily() { return transactionFamily; }
    public void setTransactionFamily(String transactionFamily) { this.transactionFamily = transactionFamily; }
    public String getProcessingCode() { return processingCode; }
    public void setProcessingCode(String processingCode) { this.processingCode = processingCode; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPosEntryMode() { return posEntryMode; }
    public void setPosEntryMode(String posEntryMode) { this.posEntryMode = posEntryMode; }
    public String getCardReadMethod() { return cardReadMethod; }
    public void setCardReadMethod(String cardReadMethod) { this.cardReadMethod = cardReadMethod; }
    public String getCardProduct() { return cardProduct; }
    public void setCardProduct(String cardProduct) { this.cardProduct = cardProduct; }
    public String getTerminalProfile() { return terminalProfile; }
    public void setTerminalProfile(String terminalProfile) { this.terminalProfile = terminalProfile; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public boolean isAutoDetected() { return autoDetected; }
    public void setAutoDetected(boolean autoDetected) { this.autoDetected = autoDetected; }
    public boolean isUserOverridden() { return userOverridden; }
    public void setUserOverridden(boolean userOverridden) { this.userOverridden = userOverridden; }
    public List<String> getConflicts() { return conflicts; }
    public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
    public List<String> getAppliedRuleStack() { return appliedRuleStack; }
    public void setAppliedRuleStack(List<String> appliedRuleStack) { this.appliedRuleStack = appliedRuleStack; }
}
