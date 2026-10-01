package com.payments.validator.model;

public enum RootCauseCategory {
    PARSE_TRANSPORT_ERROR(1),
    SCENARIO_CONFLICT(2),
    MISSING_MANDATORY_FIELD(3),
    MISSING_CONDITIONAL_FIELD(4),
    INVALID_FIELD_FORMAT_OR_VALUE(5),
    CROSS_FIELD_CONFLICT(6),
    PROHIBITED_FIELD(7),
    OPTIONAL_FIELD_WARNING(8),
    BUSINESS_DECLINE(9);

    private final int priority;

    RootCauseCategory(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
