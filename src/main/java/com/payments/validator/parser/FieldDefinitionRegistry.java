package com.payments.validator.parser;

import java.util.HashMap;
import java.util.Map;

public class FieldDefinitionRegistry {

    public enum LengthType {
        FIXED, LLVAR, LLLVAR, LLLLVAR
    }

    public enum DataType {
        NUMERIC, ALPHANUMERIC, ALPHANUMERIC_SPECIAL, BINARY, BCD
    }

    public static class FieldDef {
        private final int fieldNumber;
        private final String name;
        private final LengthType lengthType;
        private final int fixedLengthOrMax;
        private final DataType dataType;

        public FieldDef(int fieldNumber, String name, LengthType lengthType, int fixedLengthOrMax, DataType dataType) {
            this.fieldNumber = fieldNumber;
            this.name = name;
            this.lengthType = lengthType;
            this.fixedLengthOrMax = fixedLengthOrMax;
            this.dataType = dataType;
        }

        public int getFieldNumber() { return fieldNumber; }
        public String getName() { return name; }
        public LengthType getLengthType() { return lengthType; }
        public int getFixedLengthOrMax() { return fixedLengthOrMax; }
        public DataType getDataType() { return dataType; }
    }

    private final Map<Integer, FieldDef> fieldDefs = new HashMap<>();

    public FieldDefinitionRegistry() {
        initBaseDefinitions();
    }

    public FieldDef getDefinition(int fieldNumber) {
        return fieldDefs.getOrDefault(fieldNumber,
                new FieldDef(fieldNumber, "Data Element " + fieldNumber, LengthType.LLLVAR, 999, DataType.ALPHANUMERIC_SPECIAL));
    }

    private void initBaseDefinitions() {
        fieldDefs.put(1, new FieldDef(1, "Bitmap Extended", LengthType.FIXED, 16, DataType.BINARY));
        fieldDefs.put(2, new FieldDef(2, "Primary Account Number (PAN)", LengthType.LLVAR, 19, DataType.NUMERIC));
        fieldDefs.put(3, new FieldDef(3, "Processing Code", LengthType.FIXED, 6, DataType.NUMERIC));
        fieldDefs.put(4, new FieldDef(4, "Amount, Transaction", LengthType.FIXED, 12, DataType.NUMERIC));
        fieldDefs.put(7, new FieldDef(7, "Transmission Date and Time", LengthType.FIXED, 10, DataType.NUMERIC));
        fieldDefs.put(11, new FieldDef(11, "Systems Trace Audit Number (STAN)", LengthType.FIXED, 6, DataType.NUMERIC));
        fieldDefs.put(12, new FieldDef(12, "Time, Local Transaction", LengthType.FIXED, 6, DataType.NUMERIC));
        fieldDefs.put(13, new FieldDef(13, "Date, Local Transaction", LengthType.FIXED, 4, DataType.NUMERIC));
        fieldDefs.put(14, new FieldDef(14, "Date, Expiration", LengthType.FIXED, 4, DataType.NUMERIC));
        fieldDefs.put(18, new FieldDef(18, "Merchant Category Code", LengthType.FIXED, 4, DataType.NUMERIC));
        fieldDefs.put(22, new FieldDef(22, "Point of Service Entry Mode", LengthType.FIXED, 4, DataType.NUMERIC)); // Or 3/4 depending on standard
        fieldDefs.put(23, new FieldDef(23, "Card Sequence Number", LengthType.FIXED, 3, DataType.NUMERIC));
        fieldDefs.put(25, new FieldDef(25, "Point of Service Condition Code", LengthType.FIXED, 2, DataType.NUMERIC));
        fieldDefs.put(32, new FieldDef(32, "Acquiring Institution ID", LengthType.LLVAR, 11, DataType.NUMERIC));
        fieldDefs.put(35, new FieldDef(35, "Track 2 Data", LengthType.LLVAR, 37, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(37, new FieldDef(37, "Retrieval Reference Number (RRN)", LengthType.FIXED, 12, DataType.ALPHANUMERIC));
        fieldDefs.put(38, new FieldDef(38, "Authorization Identification Response", LengthType.FIXED, 6, DataType.ALPHANUMERIC));
        fieldDefs.put(39, new FieldDef(39, "Response Code", LengthType.FIXED, 2, DataType.ALPHANUMERIC));
        fieldDefs.put(41, new FieldDef(41, "Card Acceptor Terminal ID", LengthType.FIXED, 8, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(42, new FieldDef(42, "Card Acceptor ID Code", LengthType.FIXED, 15, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(43, new FieldDef(43, "Card Acceptor Name/Location", LengthType.FIXED, 40, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(44, new FieldDef(44, "Additional Response Data", LengthType.LLVAR, 25, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(48, new FieldDef(48, "Additional Data - Private", LengthType.LLLVAR, 999, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(49, new FieldDef(49, "Currency Code, Transaction", LengthType.FIXED, 3, DataType.NUMERIC));
        fieldDefs.put(52, new FieldDef(52, "Personal Identification Number (PIN) Data", LengthType.FIXED, 16, DataType.BINARY));
        fieldDefs.put(53, new FieldDef(53, "Security Related Control Information", LengthType.FIXED, 16, DataType.NUMERIC));
        fieldDefs.put(55, new FieldDef(55, "Integrated Circuit Card (ICC) System Related Data", LengthType.LLLVAR, 999, DataType.BINARY));
        fieldDefs.put(60, new FieldDef(60, "Additional POS Information / Terminal Data", LengthType.LLLVAR, 60, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(62, new FieldDef(62, "Private Field / Custom Field", LengthType.LLLVAR, 999, DataType.ALPHANUMERIC_SPECIAL));
        fieldDefs.put(63, new FieldDef(63, "Network Data / Private Subfields", LengthType.LLLVAR, 999, DataType.ALPHANUMERIC_SPECIAL));
    }
}
