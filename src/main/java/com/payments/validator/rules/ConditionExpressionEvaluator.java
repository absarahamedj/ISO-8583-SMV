package com.payments.validator.rules;

import com.payments.validator.model.IsoField;
import com.payments.validator.model.IsoMessage;
import com.payments.validator.model.ScenarioContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionExpressionEvaluator {

    private static final Pattern EXISTS_PATTERN = Pattern.compile("exists\\([\"']?(\\d+)[\"']?\\)");
    private static final Pattern TAG_EXISTS_PATTERN = Pattern.compile("tagExists\\([\"']?(\\d+)[\"']?,\\s*[\"']?([0-9A-Fa-f]+)[\"']?\\)");
    private static final Pattern FIELD_EQUALS_PATTERN = Pattern.compile("field\\([\"']?(\\d+)[\"']?\\)\\s*(==|!=)\\s*[\"']([^\"']*)[\"']");
    private static final Pattern FIELD_STARTS_WITH_PATTERN = Pattern.compile("field\\([\"']?(\\d+)[\"']?\\)\\.startsWith\\([\"']([^\"']*)[\"']\\)");
    private static final Pattern SCENARIO_PROP_PATTERN = Pattern.compile("scenario\\.(\\w+)\\s*(==|!=)\\s*[\"']([^\"']*)[\"']");

    public static boolean evaluate(String expression, IsoMessage message, ScenarioContext scenario) {
        if (expression == null || expression.trim().isEmpty() || "ALWAYS".equalsIgnoreCase(expression.trim())) {
            return true;
        }

        String expr = expression.trim();

        // Handle logical AND "&&"
        if (expr.contains("&&")) {
            String[] parts = expr.split("&&");
            for (String part : parts) {
                if (!evaluate(part.trim(), message, scenario)) {
                    return false;
                }
            }
            return true;
        }

        // Handle logical OR "||"
        if (expr.contains("||")) {
            String[] parts = expr.split("\\|\\|");
            for (String part : parts) {
                if (evaluate(part.trim(), message, scenario)) {
                    return true;
                }
            }
            return false;
        }

        // Handle NOT "!exists(X)"
        if (expr.startsWith("!exists(")) {
            Matcher m = EXISTS_PATTERN.matcher(expr.substring(1));
            if (m.find()) {
                int de = Integer.parseInt(m.group(1));
                return !message.hasField(de);
            }
        }

        // 1. exists("55")
        Matcher existsMatcher = EXISTS_PATTERN.matcher(expr);
        if (existsMatcher.matches()) {
            int de = Integer.parseInt(existsMatcher.group(1));
            return message.hasField(de);
        }

        // 2. tagExists("55", "9F26")
        Matcher tagMatcher = TAG_EXISTS_PATTERN.matcher(expr);
        if (tagMatcher.matches()) {
            int de = Integer.parseInt(tagMatcher.group(1));
            String tagHex = tagMatcher.group(2);
            IsoField f = message.getField(de);
            return f != null && f.findTag(tagHex) != null;
        }

        // 3. field("22").startsWith("05")
        Matcher startsMatcher = FIELD_STARTS_WITH_PATTERN.matcher(expr);
        if (startsMatcher.matches()) {
            int de = Integer.parseInt(startsMatcher.group(1));
            String prefix = startsMatcher.group(2);
            String val = message.getFieldValue(de);
            return val != null && val.startsWith(prefix);
        }

        // 4. field("3") == "010000"
        Matcher fieldEqMatcher = FIELD_EQUALS_PATTERN.matcher(expr);
        if (fieldEqMatcher.matches()) {
            int de = Integer.parseInt(fieldEqMatcher.group(1));
            String op = fieldEqMatcher.group(2);
            String expected = fieldEqMatcher.group(3);
            String val = message.getFieldValue(de);
            boolean equals = expected.equals(val);
            return "==".equals(op) ? equals : !equals;
        }

        // 5. scenario.channel == "ATM"
        Matcher scenMatcher = SCENARIO_PROP_PATTERN.matcher(expr);
        if (scenMatcher.matches()) {
            String prop = scenMatcher.group(1);
            String op = scenMatcher.group(2);
            String expected = scenMatcher.group(3);
            String actual = getScenarioProperty(scenario, prop);
            boolean equals = expected.equalsIgnoreCase(actual);
            return "==".equals(op) ? equals : !equals;
        }

        return true;
    }

    private static String getScenarioProperty(ScenarioContext scenario, String prop) {
        if (scenario == null) return null;
        switch (prop.toLowerCase()) {
            case "network": return scenario.getNetwork();
            case "channel": return scenario.getChannel();
            case "direction": return scenario.getDirection();
            case "transactionfamily": return scenario.getTransactionFamily();
            case "cardreadmethod": return scenario.getCardReadMethod();
            case "mti": return scenario.getMti();
            default: return null;
        }
    }
}
