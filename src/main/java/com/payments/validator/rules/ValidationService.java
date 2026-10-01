package com.payments.validator.rules;

import com.payments.validator.model.*;
import com.payments.validator.parser.BerTlvParser;
import com.payments.validator.parser.FieldDefinitionRegistry;
import com.payments.validator.parser.IsoParserEngine;
import com.payments.validator.scenario.DefaultScenarioResolver;
import com.payments.validator.security.SensitiveDataMasker;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ValidationService {

    private final IsoParserEngine parserEngine = new IsoParserEngine();
    private final DefaultScenarioResolver scenarioResolver = new DefaultScenarioResolver();
    private final FieldDefinitionRegistry registry = new FieldDefinitionRegistry();

    public ValidationReport validate(String rawMessage, boolean stripHeader, ScenarioContext userOverrides) {
        // 1. Parse raw message
        IsoMessage parsed = parserEngine.parse(rawMessage, stripHeader);

        // 2. Resolve scenario
        ScenarioContext scenario = scenarioResolver.resolve(parsed, userOverrides);

        ValidationReport report = new ValidationReport();
        report.setParsedMessage(parsed);
        report.setScenario(scenario);
        report.setTotalParsedFields(parsed.getFields().size());

        // Check for parse errors first
        if (!parsed.getParseErrors().isEmpty()) {
            report.setStatus(ValidationStatus.FAIL_PARSE);
            for (String err : parsed.getParseErrors()) {
                ValidationFinding f = new ValidationFinding();
                f.setFindingId(UUID.randomUUID().toString());
                f.setFieldName("Message Structure");
                f.setSeverity(ValidationSeverity.BLOCKER);
                f.setCategory(RootCauseCategory.PARSE_TRANSPORT_ERROR);
                f.setRequirementLevel(RequirementLevel.MANDATORY);
                f.setReason(err);
                f.setSuggestedFix("Inspect transport header, hex encoding length, and bitmap alignment.");
                f.setRootCause(true);
                report.getRootCauses().add(f);
            }
            return report;
        }

        // Check for scenario conflicts
        if (!scenario.getConflicts().isEmpty()) {
            for (String conflict : scenario.getConflicts()) {
                ValidationFinding f = new ValidationFinding();
                f.setFindingId(UUID.randomUUID().toString());
                f.setFieldName("Scenario Compatibility");
                f.setSeverity(ValidationSeverity.ERROR);
                f.setCategory(RootCauseCategory.SCENARIO_CONFLICT);
                f.setRequirementLevel(RequirementLevel.MANDATORY);
                f.setReason(conflict);
                f.setSuggestedFix("Review transaction processing code (DE 3) and entry mode (DE 22) or provide explicit scenario override.");
                f.setRootCause(true);
                report.getRootCauses().add(f);
            }
        }

        // 3. Load active rule set based on scenario
        List<RuleModel> rules = loadApplicableRules(scenario);

        // 4. Evaluate each rule
        Map<String, ValidationFinding> findingsMap = new LinkedHashMap<>();
        List<ValidationFinding> parentMissingFindings = new ArrayList<>();

        for (RuleModel rule : rules) {
            boolean conditionMet = ConditionExpressionEvaluator.evaluate(rule.getConditionExpression(), parsed, scenario);

            ValidationFinding finding = new ValidationFinding();
            finding.setFindingId(UUID.randomUUID().toString());
            finding.setRuleId(rule.getRuleId());
            finding.setRuleVersion(rule.getRuleVersion());
            finding.setFieldPath(rule.getFieldPath());
            finding.setFieldName(rule.getRuleName());
            finding.setRequirementLevel(rule.getRequirementLevel());
            finding.setConditionExpression(rule.getConditionExpression());
            finding.setExpectedValueOrFormat(rule.getExpectedDataType());
            finding.setSuggestedFix(rule.getSuggestedCorrection());
            finding.setParentFieldPath(rule.getParentFieldPath());

            boolean isPresent = false;
            String actualMasked = null;

            if (rule.getTargetTagOrSubfield() != null && !rule.getTargetTagOrSubfield().isEmpty()) {
                // Child Tag or Subfield
                IsoField parentField = parsed.getField(rule.getTargetFieldNumber());
                if (parentField != null) {
                    if (rule.getTargetFieldNumber() == 55) {
                        EmvTag tag = parentField.findTag(rule.getTargetTagOrSubfield());
                        if (tag != null) {
                            isPresent = true;
                            actualMasked = tag.getMaskedHex();
                        }
                    } else if (parentField.getSubfields().containsKey(rule.getTargetTagOrSubfield())) {
                        isPresent = true;
                        actualMasked = parentField.getSubfields().get(rule.getTargetTagOrSubfield());
                    }
                }
            } else {
                // Top-level DE
                IsoField f = parsed.getField(rule.getTargetFieldNumber());
                if (f != null) {
                    isPresent = true;
                    actualMasked = f.getMaskedValue();
                    finding.setByteOffset(f.getStartOffset());
                }
            }

            finding.setPresent(isPresent);
            finding.setActualValueMasked(actualMasked);

            // Determine status based on requirement level and presence
            if (rule.getRequirementLevel() == RequirementLevel.MANDATORY) {
                if (!isPresent) {
                    finding.setValid(false);
                    finding.setSeverity(ValidationSeverity.ERROR);
                    finding.setCategory(RootCauseCategory.MISSING_MANDATORY_FIELD);
                    finding.setReason(rule.getUserMessage() != null ? rule.getUserMessage() : "Mandatory element missing");
                    classifyAndAddFinding(report, finding);
                } else {
                    finding.setValid(true);
                    finding.setSeverity(ValidationSeverity.INFO);
                    finding.setReason("Present and verified");
                    report.setPresentAndValidCount(report.getPresentAndValidCount() + 1);
                }
            } else if (rule.getRequirementLevel() == RequirementLevel.CONDITIONAL) {
                if (conditionMet) {
                    if (!isPresent) {
                        finding.setValid(false);
                        finding.setSeverity(ValidationSeverity.ERROR);
                        finding.setCategory(RootCauseCategory.MISSING_CONDITIONAL_FIELD);
                        finding.setReason("Condition met [" + rule.getConditionExpression() + "] but element is missing: " + rule.getUserMessage());
                        classifyAndAddFinding(report, finding);
                    } else {
                        finding.setValid(true);
                        finding.setSeverity(ValidationSeverity.INFO);
                        finding.setReason("Conditional requirement satisfied");
                        report.setPresentAndValidCount(report.getPresentAndValidCount() + 1);
                    }
                } else {
                    finding.setValid(true);
                    finding.setSeverity(ValidationSeverity.INFO);
                    finding.setReason("Condition not met; not required");
                }
            } else if (rule.getRequirementLevel() == RequirementLevel.PROHIBITED) {
                if (isPresent) {
                    finding.setValid(false);
                    finding.setSeverity(ValidationSeverity.ERROR);
                    finding.setCategory(RootCauseCategory.PROHIBITED_FIELD);
                    finding.setReason("Prohibited element present in this scenario: " + rule.getUserMessage());
                    classifyAndAddFinding(report, finding);
                } else {
                    finding.setValid(true);
                    finding.setSeverity(ValidationSeverity.INFO);
                    finding.setReason("Correctly absent");
                }
            } else if (rule.getRequirementLevel() == RequirementLevel.OPTIONAL) {
                finding.setValid(true);
                finding.setSeverity(ValidationSeverity.INFO);
                finding.setReason(isPresent ? "Optional element present" : "Optional element absent");
                if (isPresent) report.setPresentAndValidCount(report.getPresentAndValidCount() + 1);
            }

            report.getCompleteMatrix().add(finding);
            findingsMap.put(rule.getFieldPath(), finding);
        }

        // 5. Group dependent child tags under missing parent (e.g. if DE 55 is missing, DE 55 is root cause)
        nestDependentFindings(report);

        // 6. Check DE 39 for Business Decline vs Structural Failure
        String de39 = parsed.getFieldValue(39);
        if (de39 != null && !"00".equals(de39)) {
            report.setBusinessDeclineCode(de39);
            String desc = interpretDeclineCode(de39);
            report.setBusinessDeclineDescription(desc);
            // A "pure business decline" means the issuer/simulator declined the transaction,
            // and any missing fields are explained by the decline itself (issuers don't always
            // populate all optional/conditional response fields on declines).
            boolean hasStructuralErrors = report.getRootCauses().stream()
                    .anyMatch(f -> f.getCategory() != null &&
                            f.getCategory() != RootCauseCategory.MISSING_MANDATORY_FIELD &&
                            f.getCategory() != RootCauseCategory.MISSING_CONDITIONAL_FIELD);
            if (!hasStructuralErrors && report.getOtherErrors().isEmpty()) {
                report.setPureBusinessDecline(true);
            }
        }

        // 7. Calculate overall status
        if (!report.getRootCauses().isEmpty()) {
            report.setStatus(determineFailureStatus(report.getRootCauses().get(0)));
        } else if (!report.getOtherErrors().isEmpty()) {
            report.setStatus(ValidationStatus.FAIL_MANDATORY_FIELD);
        } else if (!report.getWarnings().isEmpty()) {
            report.setStatus(ValidationStatus.PASS_WITH_WARNINGS);
        } else {
            report.setStatus(ValidationStatus.PASS);
        }

        return report;
    }

    private void classifyAndAddFinding(ValidationReport report, ValidationFinding finding) {
        if (finding.getCategory() == RootCauseCategory.MISSING_MANDATORY_FIELD) {
            report.setMissingMandatoryCount(report.getMissingMandatoryCount() + 1);
            finding.setRootCause(true);
            report.getRootCauses().add(finding);
        } else if (finding.getCategory() == RootCauseCategory.MISSING_CONDITIONAL_FIELD) {
            report.setMissingConditionalCount(report.getMissingConditionalCount() + 1);
            finding.setRootCause(true);
            report.getRootCauses().add(finding);
        } else if (finding.getCategory() == RootCauseCategory.PROHIBITED_FIELD) {
            report.setProhibitedFieldsCount(report.getProhibitedFieldsCount() + 1);
            report.getOtherErrors().add(finding);
        } else {
            report.getOtherErrors().add(finding);
        }
    }

    private void nestDependentFindings(ValidationReport report) {
        // If DE 55 is in root causes, find any DE 55.* child tags and nest them under DE 55
        ValidationFinding de55Finding = report.getRootCauses().stream()
                .filter(f -> "DE 55".equalsIgnoreCase(f.getFieldPath()))
                .findFirst().orElse(null);

        if (de55Finding != null) {
            Iterator<ValidationFinding> it = report.getRootCauses().iterator();
            while (it.hasNext()) {
                ValidationFinding f = it.next();
                if (f != de55Finding && f.getFieldPath() != null && f.getFieldPath().startsWith("DE 55.")) {
                    f.setRootCause(false);
                    de55Finding.getDependentFindings().add(f);
                    it.remove();
                }
            }
        }
    }

    private String interpretDeclineCode(String de39) {
        switch (de39) {
            case "05": return "Do Not Honor (Generic decline from card issuer / simulator)";
            case "51": return "Insufficient Funds";
            case "54": return "Expired Card";
            case "55": return "Incorrect PIN / Security violation";
            case "82": return "Negative CAM, dCVV, iCVV, or CVV verification failure";
            default: return "Issuer decline code " + de39;
        }
    }

    private ValidationStatus determineFailureStatus(ValidationFinding primary) {
        switch (primary.getCategory()) {
            case PARSE_TRANSPORT_ERROR: return ValidationStatus.FAIL_PARSE;
            case SCENARIO_CONFLICT: return ValidationStatus.FAIL_SCENARIO_CONFLICT;
            case MISSING_MANDATORY_FIELD: return ValidationStatus.FAIL_MANDATORY_FIELD;
            case MISSING_CONDITIONAL_FIELD: return ValidationStatus.FAIL_CONDITIONAL_FIELD;
            case PROHIBITED_FIELD: return ValidationStatus.FAIL_PROHIBITED_FIELD;
            default: return ValidationStatus.FAIL_FIELD_FORMAT;
        }
    }

    /**
     * Illustrative configurable rule set for demonstration and testing.
     * In production, loaded dynamically from PostgreSQL database via RuleRepository.
     */
    private List<RuleModel> loadApplicableRules(ScenarioContext scenario) {
        List<RuleModel> list = new ArrayList<>();

        boolean isRequest = "REQUEST".equalsIgnoreCase(scenario.getDirection());

        // Base ISO 8583 Universal Rules
        list.add(new RuleModel("RULE-BASE-002", "Primary Account Number", "DE 2", 2,
                RequirementLevel.MANDATORY, "ALWAYS", "PAN must be present in authorization request", "Include card PAN in DE 2"));
        list.add(new RuleModel("RULE-BASE-003", "Processing Code", "DE 3", 3,
                RequirementLevel.MANDATORY, "ALWAYS", "Processing code defines the transaction type", "Populate DE 3 e.g. 010000 for cash disbursement"));
        list.add(new RuleModel("RULE-BASE-004", "Amount, Transaction", "DE 4", 4,
                RequirementLevel.MANDATORY, "ALWAYS", "Transaction amount required for cash withdrawal/purchase", "Include 12-digit numeric amount in DE 4"));
        list.add(new RuleModel("RULE-BASE-007", "Transmission Date & Time", "DE 7", 7,
                RequirementLevel.MANDATORY, "ALWAYS", "Transmission timestamp MMDDhhmmss", "Set DE 7 GMT timestamp"));
        list.add(new RuleModel("RULE-BASE-011", "System Trace Audit Number (STAN)", "DE 11", 11,
                RequirementLevel.MANDATORY, "ALWAYS", "STAN required for message tracking", "Provide 6-digit STAN"));
        list.add(new RuleModel("RULE-BASE-022", "POS Entry Mode", "DE 22", 22,
                RequirementLevel.MANDATORY, "ALWAYS", "Indicates cardholder reading capability and PIN entry", "Set 4-digit POS entry mode e.g. 0510"));
        list.add(new RuleModel("RULE-BASE-041", "Card Acceptor Terminal ID", "DE 41", 41,
                RequirementLevel.MANDATORY, "ALWAYS", "Identifies terminal/ATM", "Populate DE 41 with Terminal ID"));
        list.add(new RuleModel("RULE-BASE-042", "Card Acceptor ID Code", "DE 42", 42,
                RequirementLevel.MANDATORY, "ALWAYS", "Identifies merchant or acquiring institution", "Populate DE 42"));

        if (isRequest) {
            // Chip / VSDC Rules
            RuleModel de55Rule = new RuleModel("RULE-VSDC-055", "ICC System Related Data", "DE 55", 55,
                    RequirementLevel.CONDITIONAL, "scenario.cardReadMethod == 'CHIP'",
                    "Chip transactions require ICC EMV tag data in DE 55", "Include EMV TLV data block in DE 55");
            list.add(de55Rule);

            // Child EMV Tags under DE 55
            RuleModel tag9F26 = new RuleModel("RULE-EMV-9F26", "Application Cryptogram", "DE 55.9F26", 55,
                    RequirementLevel.CONDITIONAL, "exists('55')", "ARQC Application Cryptogram mandatory for online chip auth", "Include tag 9F26 in DE 55");
            tag9F26.setTargetTagOrSubfield("9F26");
            tag9F26.setParentFieldPath("DE 55");
            list.add(tag9F26);

            RuleModel tag9F36 = new RuleModel("RULE-EMV-9F36", "Application Transaction Counter", "DE 55.9F36", 55,
                    RequirementLevel.CONDITIONAL, "exists('55')", "ATC mandatory for chip verification", "Include tag 9F36 in DE 55");
            tag9F36.setTargetTagOrSubfield("9F36");
            tag9F36.setParentFieldPath("DE 55");
            list.add(tag9F36);

            RuleModel tag95 = new RuleModel("RULE-EMV-95", "Terminal Verification Results", "DE 55.95", 55,
                    RequirementLevel.CONDITIONAL, "exists('55')", "TVR mandatory for chip risk evaluation", "Include tag 95 in DE 55");
            tag95.setTargetTagOrSubfield("95");
            tag95.setParentFieldPath("DE 55");
            list.add(tag95);

            // ATM PIN Data (DE 52)
            list.add(new RuleModel("RULE-ATM-052", "PIN Block", "DE 52", 52,
                    RequirementLevel.CONDITIONAL, "scenario.channel == 'ATM'",
                    "ATM cash disbursement requires encrypted PIN block in DE 52", "Include encrypted PIN block in DE 52"));

            // Visa Private Field DE 60
            list.add(new RuleModel("RULE-VISA-060", "Additional POS Information", "DE 60", 60,
                    RequirementLevel.CONDITIONAL, "scenario.network == 'VISA'",
                    "Visa requires terminal capability & transaction specifier in DE 60", "Include DE 60"));

            // Prohibited field in request: DE 39
            RuleModel de39Rule = new RuleModel("RULE-REQ-PROHIBIT-039", "Response Code", "DE 39", 39,
                    RequirementLevel.PROHIBITED, "ALWAYS", "Response code DE 39 must not be present in authorization request (0100)", "Remove DE 39 from request");
            list.add(de39Rule);
        } else {
            // Response 0110 / 0210
            list.add(new RuleModel("RULE-RESP-039", "Response Code", "DE 39", 39,
                    RequirementLevel.MANDATORY, "ALWAYS", "Response code indicates approval or reason for decline", "Include 2-char response code in DE 39"));

            list.add(new RuleModel("RULE-RESP-038", "Authorization Identification Response", "DE 38", 38,
                    RequirementLevel.CONDITIONAL, "field('39') == '00'",
                    "Approval code required when transaction is approved (DE 39 = 00)", "Include 6-char auth code in DE 38"));

            RuleModel respDe55 = new RuleModel("RULE-RESP-055", "Issuer Authentication Data", "DE 55", 55,
                    RequirementLevel.CONDITIONAL, "field('39') == '00' && scenario.cardReadMethod == 'CHIP'",
                    "Issuer chip response data (ARPC/scripts) required on chip approval", "Include tag 91 or scripts in DE 55");
            list.add(respDe55);
        }

        return list;
    }
}
