package com.payments.validator.rules;

import com.payments.validator.model.RequirementLevel;
import com.payments.validator.model.ValidationSeverity;

import java.util.List;

public class RuleModel {
    private String ruleId;
    private String ruleName;
    private String description;
    private String businessPurpose;
    private String network; // VISA, MASTERCARD, COMMON, CUSTOM
    private String ruleProfile;
    private String ruleVersion;
    private int priority;
    private String messageDirection; // REQUEST, RESPONSE, ANY
    private String mtiPattern; // e.g. "0100", "0110", "01.*"
    private String transactionFamily; // CASH_WITHDRAWAL, PURCHASE, ANY
    private String channel; // ATM, POS, ECOMMERCE, ANY

    private String fieldPath; // e.g. "DE 55", "DE 55.9F26", "DE 62.23"
    private int targetFieldNumber;
    private String targetTagOrSubfield;
    private String parentFieldPath;

    private RequirementLevel requirementLevel;
    private String conditionExpression; // Safe DSL expression
    private String prohibitedCondition;

    private String expectedDataType;
    private Integer minLength;
    private Integer maxLength;
    private String regexPattern;
    private List<String> allowedValues;

    private ValidationSeverity severity = ValidationSeverity.ERROR;
    private String errorCode;
    private String userMessage;
    private String suggestedCorrection;
    private String docReference;

    public RuleModel() {}

    // Builder-like constructor for core rules
    public RuleModel(String ruleId, String ruleName, String fieldPath, int de, RequirementLevel req, String condition, String message, String fix) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.fieldPath = fieldPath;
        this.targetFieldNumber = de;
        this.requirementLevel = req;
        this.conditionExpression = condition;
        this.userMessage = message;
        this.suggestedCorrection = fix;
        this.ruleVersion = "1.0.0";
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBusinessPurpose() { return businessPurpose; }
    public void setBusinessPurpose(String businessPurpose) { this.businessPurpose = businessPurpose; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getRuleProfile() { return ruleProfile; }
    public void setRuleProfile(String ruleProfile) { this.ruleProfile = ruleProfile; }
    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getMessageDirection() { return messageDirection; }
    public void setMessageDirection(String messageDirection) { this.messageDirection = messageDirection; }
    public String getMtiPattern() { return mtiPattern; }
    public void setMtiPattern(String mtiPattern) { this.mtiPattern = mtiPattern; }
    public String getTransactionFamily() { return transactionFamily; }
    public void setTransactionFamily(String transactionFamily) { this.transactionFamily = transactionFamily; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getFieldPath() { return fieldPath; }
    public void setFieldPath(String fieldPath) { this.fieldPath = fieldPath; }
    public int getTargetFieldNumber() { return targetFieldNumber; }
    public void setTargetFieldNumber(int targetFieldNumber) { this.targetFieldNumber = targetFieldNumber; }
    public String getTargetTagOrSubfield() { return targetTagOrSubfield; }
    public void setTargetTagOrSubfield(String targetTagOrSubfield) { this.targetTagOrSubfield = targetTagOrSubfield; }
    public String getParentFieldPath() { return parentFieldPath; }
    public void setParentFieldPath(String parentFieldPath) { this.parentFieldPath = parentFieldPath; }
    public RequirementLevel getRequirementLevel() { return requirementLevel; }
    public void setRequirementLevel(RequirementLevel requirementLevel) { this.requirementLevel = requirementLevel; }
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    public String getProhibitedCondition() { return prohibitedCondition; }
    public void setProhibitedCondition(String prohibitedCondition) { this.prohibitedCondition = prohibitedCondition; }
    public String getExpectedDataType() { return expectedDataType; }
    public void setExpectedDataType(String expectedDataType) { this.expectedDataType = expectedDataType; }
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    public String getRegexPattern() { return regexPattern; }
    public void setRegexPattern(String regexPattern) { this.regexPattern = regexPattern; }
    public List<String> getAllowedValues() { return allowedValues; }
    public void setAllowedValues(List<String> allowedValues) { this.allowedValues = allowedValues; }
    public ValidationSeverity getSeverity() { return severity; }
    public void setSeverity(ValidationSeverity severity) { this.severity = severity; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public String getSuggestedCorrection() { return suggestedCorrection; }
    public void setSuggestedCorrection(String suggestedCorrection) { this.suggestedCorrection = suggestedCorrection; }
    public String getDocReference() { return docReference; }
    public void setDocReference(String docReference) { this.docReference = docReference; }
}
