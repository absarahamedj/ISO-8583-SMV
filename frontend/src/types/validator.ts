export type RequirementLevel =
  | 'MANDATORY'
  | 'CONDITIONAL'
  | 'OPTIONAL'
  | 'PROHIBITED'
  | 'NOT_APPLICABLE'
  | 'INFORMATIONAL';

export type ValidationSeverity = 'BLOCKER' | 'ERROR' | 'WARNING' | 'INFO';

export type ValidationStatus =
  | 'PASS'
  | 'PASS_WITH_WARNINGS'
  | 'FAIL_PARSE'
  | 'FAIL_MANDATORY_FIELD'
  | 'FAIL_CONDITIONAL_FIELD'
  | 'FAIL_FIELD_FORMAT'
  | 'FAIL_FIELD_VALUE'
  | 'FAIL_CROSS_FIELD'
  | 'FAIL_PROHIBITED_FIELD'
  | 'FAIL_SCENARIO_CONFLICT'
  | 'FAIL_RULE_CONFIGURATION'
  | 'INDETERMINATE';

export interface EmvTag {
  tag: string;
  name: string;
  length: number;
  rawHex: string;
  maskedHex: string;
  decodedValue?: string;
  constructed: boolean;
  startOffset: number;
  endOffset: number;
  children?: EmvTag[];
}

export interface IsoField {
  fieldNumber: number;
  path: string;
  name: string;
  rawValue?: string;
  decodedValue: string;
  maskedValue: string;
  encoding?: string;
  length: number;
  startOffset: number;
  endOffset: number;
  validParse: boolean;
  parseError?: string;
  subfields?: Record<string, string>;
  emvTags?: EmvTag[];
}

export interface IsoMessage {
  rawInput: string;
  inputFormat: string;
  transportHeaderHex?: string;
  mti: string;
  primaryBitmapHex?: string;
  secondaryBitmapHex?: string;
  tertiaryBitmapHex?: string;
  direction: 'REQUEST' | 'RESPONSE' | string;
  fields: Record<string, IsoField>;
  parseWarnings: string[];
  parseErrors: string[];
  fullyParsed: boolean;
  totalBytesParsed: number;
}

export interface ScenarioContext {
  network: string;
  ruleProfile?: string;
  ruleProfileVersion?: string;
  region?: string;
  direction?: string;
  mti?: string;
  transactionFamily?: string;
  processingCode?: string;
  channel?: string;
  posEntryMode?: string;
  cardReadMethod?: string;
  cardProduct?: string;
  terminalProfile?: string;
  confidenceScore: number;
  autoDetected: boolean;
  userOverridden: boolean;
  conflicts: string[];
  appliedRuleStack: string[];
}

export interface ValidationFinding {
  findingId: string;
  ruleId?: string;
  ruleVersion?: string;
  fieldPath: string;
  fieldName: string;
  severity: ValidationSeverity;
  category: string;
  requirementLevel: RequirementLevel;
  present: boolean;
  valid: boolean;
  actualValueMasked?: string;
  expectedValueOrFormat?: string;
  reason: string;
  conditionExpression?: string;
  suggestedFix?: string;
  isRootCause: boolean;
  parentFieldPath?: string;
  dependentFindings?: ValidationFinding[];
  byteOffset?: number;
}

export interface ValidationReport {
  reportId: string;
  timestamp: string;
  status: ValidationStatus;
  scenario: ScenarioContext;
  parsedMessage: IsoMessage;
  totalParsedFields: number;
  presentAndValidCount: number;
  missingMandatoryCount: number;
  missingConditionalCount: number;
  invalidFieldsCount: number;
  prohibitedFieldsCount: number;
  warningCount: number;
  infoCount: number;
  rootCauses: ValidationFinding[];
  otherErrors: ValidationFinding[];
  warnings: ValidationFinding[];
  informationalItems: ValidationFinding[];
  completeMatrix: ValidationFinding[];
  businessDeclineCode?: string;
  businessDeclineDescription?: string;
  isPureBusinessDecline: boolean;
}

export interface CompareReportResponse {
  correlationStan?: string;
  correlationRrn?: string;
  correlationTerminalId?: string;
  correlated: boolean;
  requestReport: ValidationReport;
  responseReport: ValidationReport;
  correlationDifferences: string[];
  overallAnalysis: string;
}
