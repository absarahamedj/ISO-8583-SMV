package com.payments.validator.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationFinding {
    private String findingId;
    private String ruleId;
    private String ruleVersion;
    private String fieldPath; // e.g. "DE 55", "DE 55.9F36", "DE 62.23"
    private String fieldName;
    private ValidationSeverity severity;
    private RootCauseCategory category;
    private RequirementLevel requirementLevel;
    private boolean present;
    private boolean valid;
    private String actualValueMasked;
    private String expectedValueOrFormat;
    private String reason;
    private String conditionExpression;
    private String suggestedFix;
    private boolean isRootCause;
    private String parentFieldPath;
    private List<ValidationFinding> dependentFindings = new ArrayList<>();
    private Integer byteOffset;

    public ValidationFinding() {}

    public String getFindingId() { return findingId; }
    public void setFindingId(String findingId) { this.findingId = findingId; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }
    public String getFieldPath() { return fieldPath; }
    public void setFieldPath(String fieldPath) { this.fieldPath = fieldPath; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public ValidationSeverity getSeverity() { return severity; }
    public void setSeverity(ValidationSeverity severity) { this.severity = severity; }
    public RootCauseCategory getCategory() { return category; }
    public void setCategory(RootCauseCategory category) { this.category = category; }
    public RequirementLevel getRequirementLevel() { return requirementLevel; }
    public void setRequirementLevel(RequirementLevel requirementLevel) { this.requirementLevel = requirementLevel; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getActualValueMasked() { return actualValueMasked; }
    public void setActualValueMasked(String actualValueMasked) { this.actualValueMasked = actualValueMasked; }
    public String getExpectedValueOrFormat() { return expectedValueOrFormat; }
    public void setExpectedValueOrFormat(String expectedValueOrFormat) { this.expectedValueOrFormat = expectedValueOrFormat; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }
    public boolean isRootCause() { return isRootCause; }
    public void setRootCause(boolean rootCause) { isRootCause = rootCause; }
    public String getParentFieldPath() { return parentFieldPath; }
    public void setParentFieldPath(String parentFieldPath) { this.parentFieldPath = parentFieldPath; }
    public List<ValidationFinding> getDependentFindings() { return dependentFindings; }
    public void setDependentFindings(List<ValidationFinding> dependentFindings) { this.dependentFindings = dependentFindings; }
    public Integer getByteOffset() { return byteOffset; }
    public void setByteOffset(Integer byteOffset) { this.byteOffset = byteOffset; }
}
