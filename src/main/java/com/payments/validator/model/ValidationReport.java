package com.payments.validator.model;

import java.time.Instant;
import java.util.*;

public class ValidationReport {
    private String reportId = UUID.randomUUID().toString();
    private Instant timestamp = Instant.now();
    private ValidationStatus status = ValidationStatus.PASS;
    private ScenarioContext scenario;
    private IsoMessage parsedMessage;

    // Summary counts
    private int totalParsedFields = 0;
    private int presentAndValidCount = 0;
    private int missingMandatoryCount = 0;
    private int missingConditionalCount = 0;
    private int invalidFieldsCount = 0;
    private int prohibitedFieldsCount = 0;
    private int warningCount = 0;
    private int infoCount = 0;

    // Findings organized by root-cause priority
    private List<ValidationFinding> rootCauses = new ArrayList<>();
    private List<ValidationFinding> otherErrors = new ArrayList<>();
    private List<ValidationFinding> warnings = new ArrayList<>();
    private List<ValidationFinding> informationalItems = new ArrayList<>();

    // Complete Mandatory Matrix
    private List<ValidationFinding> completeMatrix = new ArrayList<>();

    // Business decline annotation (if simulator response indicates decline e.g. DE 39 = 55)
    private String businessDeclineCode;
    private String businessDeclineDescription;
    private boolean isPureBusinessDecline = false;

    public ValidationReport() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public ValidationStatus getStatus() { return status; }
    public void setStatus(ValidationStatus status) { this.status = status; }
    public ScenarioContext getScenario() { return scenario; }
    public void setScenario(ScenarioContext scenario) { this.scenario = scenario; }
    public IsoMessage getParsedMessage() { return parsedMessage; }
    public void setParsedMessage(IsoMessage parsedMessage) { this.parsedMessage = parsedMessage; }
    public int getTotalParsedFields() { return totalParsedFields; }
    public void setTotalParsedFields(int totalParsedFields) { this.totalParsedFields = totalParsedFields; }
    public int getPresentAndValidCount() { return presentAndValidCount; }
    public void setPresentAndValidCount(int presentAndValidCount) { this.presentAndValidCount = presentAndValidCount; }
    public int getMissingMandatoryCount() { return missingMandatoryCount; }
    public void setMissingMandatoryCount(int missingMandatoryCount) { this.missingMandatoryCount = missingMandatoryCount; }
    public int getMissingConditionalCount() { return missingConditionalCount; }
    public void setMissingConditionalCount(int missingConditionalCount) { this.missingConditionalCount = missingConditionalCount; }
    public int getInvalidFieldsCount() { return invalidFieldsCount; }
    public void setInvalidFieldsCount(int invalidFieldsCount) { this.invalidFieldsCount = invalidFieldsCount; }
    public int getProhibitedFieldsCount() { return prohibitedFieldsCount; }
    public void setProhibitedFieldsCount(int prohibitedFieldsCount) { this.prohibitedFieldsCount = prohibitedFieldsCount; }
    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
    public int getInfoCount() { return infoCount; }
    public void setInfoCount(int infoCount) { this.infoCount = infoCount; }
    public List<ValidationFinding> getRootCauses() { return rootCauses; }
    public void setRootCauses(List<ValidationFinding> rootCauses) { this.rootCauses = rootCauses; }
    public List<ValidationFinding> getOtherErrors() { return otherErrors; }
    public void setOtherErrors(List<ValidationFinding> otherErrors) { this.otherErrors = otherErrors; }
    public List<ValidationFinding> getWarnings() { return warnings; }
    public void setWarnings(List<ValidationFinding> warnings) { this.warnings = warnings; }
    public List<ValidationFinding> getInformationalItems() { return informationalItems; }
    public void setInformationalItems(List<ValidationFinding> informationalItems) { this.informationalItems = informationalItems; }
    public List<ValidationFinding> getCompleteMatrix() { return completeMatrix; }
    public void setCompleteMatrix(List<ValidationFinding> completeMatrix) { this.completeMatrix = completeMatrix; }
    public String getBusinessDeclineCode() { return businessDeclineCode; }
    public void setBusinessDeclineCode(String businessDeclineCode) { this.businessDeclineCode = businessDeclineCode; }
    public String getBusinessDeclineDescription() { return businessDeclineDescription; }
    public void setBusinessDeclineDescription(String businessDeclineDescription) { this.businessDeclineDescription = businessDeclineDescription; }
    public boolean isPureBusinessDecline() { return isPureBusinessDecline; }
    public void setPureBusinessDecline(boolean pureBusinessDecline) { isPureBusinessDecline = pureBusinessDecline; }
}
