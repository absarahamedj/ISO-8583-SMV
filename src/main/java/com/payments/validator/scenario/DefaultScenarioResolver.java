package com.payments.validator.scenario;

import com.payments.validator.model.IsoMessage;
import com.payments.validator.model.ScenarioContext;

import java.util.ArrayList;
import java.util.List;

public class DefaultScenarioResolver {

    public ScenarioContext resolve(IsoMessage message, ScenarioContext userOverrides) {
        ScenarioContext ctx = new ScenarioContext();
        List<String> conflicts = new ArrayList<>();
        double score = 1.0;

        // 1. MTI & Direction
        String mti = (userOverrides != null && userOverrides.getMti() != null) ?
                userOverrides.getMti() : message.getMti();
        ctx.setMti(mti);
        ctx.setDirection(message.getDirection());

        // 2. Resolve Network
        String network = null;
        if (userOverrides != null && userOverrides.getNetwork() != null && !userOverrides.getNetwork().equalsIgnoreCase("AUTO")) {
            network = userOverrides.getNetwork().toUpperCase();
            ctx.setUserOverridden(true);
        } else {
            String pan = message.getFieldValue(2);
            if (pan != null) {
                if (pan.startsWith("4")) network = "VISA";
                else if (pan.startsWith("51") || pan.startsWith("52") || pan.startsWith("53") ||
                         pan.startsWith("54") || pan.startsWith("55") || pan.startsWith("2")) network = "MASTERCARD";
            }
            if (network == null) network = "VISA"; // Default fallback
        }
        ctx.setNetwork(network);

        // 3. Resolve Transaction Family via DE 3 (Processing Code)
        String pcode = message.getFieldValue(3);
        ctx.setProcessingCode(pcode);
        String txFamily = "PURCHASE";
        if (pcode != null && pcode.length() >= 2) {
            String txType = pcode.substring(0, 2);
            if ("01".equals(txType)) {
                txFamily = "CASH_WITHDRAWAL";
            } else if ("00".equals(txType)) {
                txFamily = "PURCHASE";
            } else if ("20".equals(txType)) {
                txFamily = "REFUND";
            } else if ("31".equals(txType)) {
                txFamily = "BALANCE_INQUIRY";
            }
        }
        ctx.setTransactionFamily(txFamily);

        // 4. Resolve Channel & Entry Mode via DE 22, DE 18, DE 25
        String posEntry = message.getFieldValue(22);
        ctx.setPosEntryMode(posEntry);

        String readMethod = "MAGSTRIPE";
        String channel = "POS";

        if (posEntry != null && posEntry.length() >= 2) {
            String panEntry = posEntry.substring(0, 2);
            if ("05".equals(panEntry)) {
                readMethod = "CHIP";
            } else if ("07".equals(panEntry)) {
                readMethod = "CONTACTLESS_CHIP";
            } else if ("91".equals(panEntry)) {
                readMethod = "CONTACTLESS_MAGSTRIPE";
            } else if ("01".equals(panEntry)) {
                readMethod = "MANUAL";
            } else if ("10".equals(panEntry) || "81".equals(panEntry)) {
                readMethod = "ECOMMERCE";
                channel = "ECOMMERCE";
            }
        }
        ctx.setCardReadMethod(readMethod);

        String mcc = message.getFieldValue(18);
        String posCond = message.getFieldValue(25);
        if ("6011".equals(mcc) || "02".equals(posCond)) {
            channel = "ATM";
        }
        ctx.setChannel(channel);

        // 5. Conflict Detection
        if ("CASH_WITHDRAWAL".equals(txFamily) && "ECOMMERCE".equals(channel)) {
            conflicts.add("DE 3 indicates cash withdrawal ('01'), but DE 22/channel indicates e-commerce ('10'). Correct message or select intended profile.");
            score -= 0.4;
        }

        // Note: Missing DE 55 for chip is handled by the conditional rule RULE-VSDC-055 in ValidationService,
        // so we do NOT flag it as a scenario conflict here — that would override the more specific validation status.

        // 6. User overrides application
        if (userOverrides != null) {
            if (userOverrides.getChannel() != null) ctx.setChannel(userOverrides.getChannel());
            if (userOverrides.getTransactionFamily() != null) ctx.setTransactionFamily(userOverrides.getTransactionFamily());
            if (userOverrides.getRuleProfile() != null) ctx.setRuleProfile(userOverrides.getRuleProfile());
        }

        // 7. Resolve Rule Profile Stack
        List<String> stack = new ArrayList<>();
        stack.add("BASE_ISO8583_v1.0");
        if ("VISA".equalsIgnoreCase(ctx.getNetwork())) {
            stack.add("VISA_CORE_v2026.1");
            if ("ATM".equalsIgnoreCase(ctx.getChannel()) && "CASH_WITHDRAWAL".equalsIgnoreCase(ctx.getTransactionFamily())) {
                stack.add("VISA_ATM_CASH_DISBURSEMENT_v1.2");
                stack.add("VSDC_CHIP_PIN_PROFILE_v1.0");
                ctx.setRuleProfile("VISA_VSDC_ATM_CASH_DISBURSEMENT");
            } else if ("ECOMMERCE".equalsIgnoreCase(ctx.getChannel())) {
                stack.add("VISA_ECOMMERCE_PROFILE_v1.1");
                ctx.setRuleProfile("VISA_ECOMMERCE_PURCHASE");
            } else {
                stack.add("VISA_CARD_PRESENT_POS_v1.0");
                ctx.setRuleProfile("VISA_POS_PURCHASE");
            }
        } else {
            stack.add("MASTERCARD_CORE_v2026.1");
            if ("ATM".equalsIgnoreCase(ctx.getChannel())) {
                stack.add("MASTERCARD_ATM_CASH_v1.0");
                ctx.setRuleProfile("MASTERCARD_ATM_CASH");
            } else {
                stack.add("MASTERCARD_POS_PURCHASE_v1.0");
                ctx.setRuleProfile("MASTERCARD_POS_PURCHASE");
            }
        }

        ctx.setRuleProfileVersion("2026.1.0");
        ctx.setAppliedRuleStack(stack);
        ctx.setConflicts(conflicts);
        ctx.setConfidenceScore(Math.max(0.0, score));

        return ctx;
    }
}
