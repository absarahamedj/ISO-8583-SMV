package com.payments.validator;

import com.payments.validator.model.*;
import com.payments.validator.rules.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    public void setup() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Should successfully validate compliant Visa VSDC ATM Cash Withdrawal Request 0100")
    public void testValidVsdcAtmCashDisbursementRequest() {
        // VTS Log representation of a valid Visa ATM Cash Withdrawal 0100
        String vtsLog = """
                MTI: 0100
                Bitmap: F238448108E18000
                F2: 4413600000002899
                F3: 010000
                F4: 000000010000
                F7: 0928120000
                F11: 123456
                F18: 6011
                F22: 0510
                F25: 02
                F41: ATM00001
                F42: CARD ACCEPTOR 01
                F52: 1A2B3C4D5E6F7081
                F55: 9F260811223344556677889F3602001595050000000000
                F55.9F26: 1122334455667788
                F55.9F36: 0015
                F55.95: 0000000000
                F60: 0100000000
                """;

        ValidationReport report = validationService.validate(vtsLog, true, null);

        assertNotNull(report);
        assertEquals(ValidationStatus.PASS, report.getStatus());
        assertEquals("VISA", report.getScenario().getNetwork());
        assertEquals("ATM", report.getScenario().getChannel());
        assertEquals("CASH_WITHDRAWAL", report.getScenario().getTransactionFamily());
        assertEquals("CHIP", report.getScenario().getCardReadMethod());
        assertTrue(report.getRootCauses().isEmpty());
        assertTrue(report.getMissingMandatoryCount() == 0);
        assertTrue(report.getPresentAndValidCount() >= 8);
    }

    @Test
    @DisplayName("Should flag missing DE 55 as root cause and nest child tags under it")
    public void testMissingChipDe55NestsChildTags() {
        // VTS Log where DE 22 specifies Chip (0510), but DE 55 is missing
        String vtsLog = """
                MTI: 0100
                Bitmap: F238448108E18000
                F2: 4413600000002899
                F3: 010000
                F4: 000000010000
                F7: 0928120000
                F11: 123456
                F18: 6011
                F22: 0510
                F25: 02
                F41: ATM00001
                F42: CARD ACCEPTOR 01
                F52: 1A2B3C4D5E6F7081
                F60: 0100000000
                """;

        ValidationReport report = validationService.validate(vtsLog, true, null);

        assertNotNull(report);
        assertNotEquals(ValidationStatus.PASS, report.getStatus());
        assertEquals(ValidationStatus.FAIL_CONDITIONAL_FIELD, report.getStatus());

        // Verify root cause is DE 55, not individual child tags
        boolean hasDe55RootCause = report.getRootCauses().stream()
                .anyMatch(f -> "DE 55".equalsIgnoreCase(f.getFieldPath()));
        assertTrue(hasDe55RootCause, "DE 55 must be identified as root cause");

        // Verify no child tags (DE 55.9F26) are present in the top-level root causes list
        boolean hasChildTagInRootCauses = report.getRootCauses().stream()
                .anyMatch(f -> f.getFieldPath() != null && f.getFieldPath().startsWith("DE 55."));
        assertFalse(hasChildTagInRootCauses, "Child tags must NOT pollute top-level root causes list");
    }

    @Test
    @DisplayName("Should interpret DE 39=55 as simulator decline rather than parser error")
    public void testDeclineInterpretation() {
        String responseLog = """
                MTI: 0110
                Bitmap: F238448108E18000
                F2: 4413600000002899
                F3: 010000
                F4: 000000010000
                F7: 0928120000
                F11: 123456
                F39: 55
                F41: ATM00001
                """;

        ValidationReport report = validationService.validate(responseLog, true, null);

        assertNotNull(report);
        assertEquals("55", report.getBusinessDeclineCode());
        assertTrue(report.isPureBusinessDecline());
        assertTrue(report.getBusinessDeclineDescription().contains("Incorrect PIN"));
        assertNotEquals(ValidationStatus.FAIL_PARSE, report.getStatus());
    }

    @Test
    @DisplayName("Should successfully parse and validate user's exact VTS Exported Raw Hex Dump")
    public void testUserVtsExportedRawHexDump() {
        String vtsRawDump = """
                --------------------------------------------------------------------------------
                VTS Exported Raw Message: \t

                Date - Message Printed: \t09/18/2026
                Time - Message Printed: \t18:13:31
                --------------------------------------------------------------------------------
                --------------------------------------------------------------------------------

                Raw Hex Dump
                Displacement   00+        04+        08+        0C+      
                --------------------------------------------------------------------------------
                0000           16010200   7D000000   00000000   00000000 
                0010           00000000   00000110   72202281   0AD08002 
                0020           10441360   00000030   87000000   00000000 
                0030           13000917   12121100   02240608   0001000B 
                0040           01234567   8901F6F2   F6F0F1F2   F0F0F0F2 
                0050           F2F4F0F5   F74BF240   40404040   C3C1D9C4 
                0060           40C1C3C3   C5D7E3D6   D9404009   40404040 
                0070           40404040   F2060805   80000000   02       
                ================================================================================
                """;

        ValidationReport report = validationService.validate(vtsRawDump, true, null);

        assertNotNull(report);
        assertEquals("0110", report.getParsedMessage().getMti(), "MTI must be 0110");
        assertEquals("RESPONSE", report.getParsedMessage().getDirection());
        assertEquals("VISA", report.getScenario().getNetwork());
        
        // Assert DE 2 (PAN) extracted and safely masked
        IsoField de2 = report.getParsedMessage().getField(2);
        assertNotNull(de2, "DE 2 (PAN) must be parsed");
        assertEquals("4413600000003087", de2.getDecodedValue());
        assertEquals("441360******3087", de2.getMaskedValue());

        // Assert DE 39 (Response Code = 05)
        IsoField de39 = report.getParsedMessage().getField(39);
        assertNotNull(de39, "DE 39 (Response Code) must be parsed");
        assertEquals("05", de39.getDecodedValue());
        assertEquals("05", report.getBusinessDeclineCode());

        // Assert DE 37 (RRN in EBCDIC)
        IsoField de37 = report.getParsedMessage().getField(37);
        assertNotNull(de37, "DE 37 (RRN) must be parsed");
        assertEquals("626012000224", de37.getDecodedValue());

        // Assert DE 42 (Acceptor ID in EBCDIC)
        IsoField de42 = report.getParsedMessage().getField(42);
        assertNotNull(de42, "DE 42 (Card Acceptor ID) must be parsed");
        assertTrue(de42.getDecodedValue().contains("CARD ACCEPTOR"));
    }
}

