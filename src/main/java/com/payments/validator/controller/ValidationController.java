package com.payments.validator.controller;

import com.payments.validator.dto.CompareReportResponse;
import com.payments.validator.dto.CompareRequestResponseDto;
import com.payments.validator.dto.ParseAndValidateRequest;
import com.payments.validator.model.IsoMessage;
import com.payments.validator.model.ValidationReport;
import com.payments.validator.parser.IsoParserEngine;
import com.payments.validator.rules.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Tag(name = "ISO 8583 Validation API", description = "Endpoints for parsing, validating, and comparing ISO 8583 card payment messages")
public class ValidationController {

    private final ValidationService validationService;
    private final IsoParserEngine parserEngine = new IsoParserEngine();

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/parse")
    @Operation(summary = "Parse raw ISO 8583 message without business validation")
    public ResponseEntity<IsoMessage> parse(@RequestBody ParseAndValidateRequest request) {
        IsoMessage parsed = parserEngine.parse(request.getRawMessage(), request.isStrip16ByteHeader());
        return ResponseEntity.ok(parsed);
    }

    @PostMapping("/parse-and-validate")
    @Operation(summary = "Parse and dynamically validate ISO 8583 message against scenario rule profile")
    public ResponseEntity<ValidationReport> parseAndValidate(@RequestBody ParseAndValidateRequest request) {
        ValidationReport report = validationService.validate(
                request.getRawMessage(),
                request.isStrip16ByteHeader(),
                request.getOverrides()
        );
        return ResponseEntity.ok(report);
    }

    @PostMapping("/compare-request-response")
    @Operation(summary = "Correlate and validate request/response pair side-by-side")
    public ResponseEntity<CompareReportResponse> compareRequestResponse(@RequestBody CompareRequestResponseDto dto) {
        ValidationReport reqReport = validationService.validate(dto.getRawRequest(), dto.isStrip16ByteHeader(), dto.getOverrides());
        ValidationReport resReport = validationService.validate(dto.getRawResponse(), dto.isStrip16ByteHeader(), dto.getOverrides());

        CompareReportResponse response = new CompareReportResponse();
        response.setRequestReport(reqReport);
        response.setResponseReport(resReport);

        // Correlate on STAN (DE 11), RRN (DE 37), Terminal ID (DE 41)
        String reqStan = reqReport.getParsedMessage().getFieldValue(11);
        String resStan = resReport.getParsedMessage().getFieldValue(11);
        response.setCorrelationStan(reqStan);

        String reqRrn = reqReport.getParsedMessage().getFieldValue(37);
        String resRrn = resReport.getParsedMessage().getFieldValue(37);
        response.setCorrelationRrn(reqRrn);

        String reqTid = reqReport.getParsedMessage().getFieldValue(41);
        String resTid = resReport.getParsedMessage().getFieldValue(41);
        response.setCorrelationTerminalId(reqTid);

        boolean stanMatch = reqStan != null && reqStan.equals(resStan);
        boolean tidMatch = reqTid != null && reqTid.equals(resTid);
        response.setCorrelated(stanMatch || tidMatch);

        if (!stanMatch) {
            response.getCorrelationDifferences().add("STAN mismatch: Request DE 11=" + reqStan + ", Response DE 11=" + resStan);
        }

        // Check if response decline is due to request errors
        if (resReport.getBusinessDeclineCode() != null) {
            if (!reqReport.getRootCauses().isEmpty()) {
                response.setOverallAnalysis("Simulator response declined (" + resReport.getBusinessDeclineCode() +
                        "). Request contains " + reqReport.getRootCauses().size() + " blocking root-cause rule violations which likely caused this decline.");
            } else {
                response.setOverallAnalysis("Simulator response declined (" + resReport.getBusinessDeclineCode() +
                        ": " + resReport.getBusinessDeclineDescription() + "). Request is structurally valid; decline was triggered by test-card/simulator configuration.");
            }
        } else {
            response.setOverallAnalysis("Transaction approved. Structural and EMV requirements satisfied.");
        }

        return ResponseEntity.ok(response);
    }
}
