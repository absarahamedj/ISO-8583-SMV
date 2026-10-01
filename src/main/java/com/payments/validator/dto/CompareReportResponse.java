package com.payments.validator.dto;

import com.payments.validator.model.ValidationReport;

import java.util.ArrayList;
import java.util.List;

public class CompareReportResponse {
    private String correlationStan;
    private String correlationRrn;
    private String correlationTerminalId;
    private boolean correlated;
    private ValidationReport requestReport;
    private ValidationReport responseReport;
    private List<String> correlationDifferences = new ArrayList<>();
    private String overallAnalysis;

    public CompareReportResponse() {}

    public String getCorrelationStan() { return correlationStan; }
    public void setCorrelationStan(String correlationStan) { this.correlationStan = correlationStan; }
    public String getCorrelationRrn() { return correlationRrn; }
    public void setCorrelationRrn(String correlationRrn) { this.correlationRrn = correlationRrn; }
    public String getCorrelationTerminalId() { return correlationTerminalId; }
    public void setCorrelationTerminalId(String correlationTerminalId) { this.correlationTerminalId = correlationTerminalId; }
    public boolean isCorrelated() { return correlated; }
    public void setCorrelated(boolean correlated) { this.correlated = correlated; }
    public ValidationReport getRequestReport() { return requestReport; }
    public void setRequestReport(ValidationReport requestReport) { this.requestReport = requestReport; }
    public ValidationReport getResponseReport() { return responseReport; }
    public void setResponseReport(ValidationReport responseReport) { this.responseReport = responseReport; }
    public List<String> getCorrelationDifferences() { return correlationDifferences; }
    public void setCorrelationDifferences(List<String> correlationDifferences) { this.correlationDifferences = correlationDifferences; }
    public String getOverallAnalysis() { return overallAnalysis; }
    public void setOverallAnalysis(String overallAnalysis) { this.overallAnalysis = overallAnalysis; }
}
