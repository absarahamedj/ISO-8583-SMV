import { CompareReportResponse, IsoMessage, ValidationReport } from '../types/validator';

const BASE_URL = '/api/v1';

export interface ParseAndValidatePayload {
  rawMessage: string;
  inputFormat: string;
  strip16ByteHeader: boolean;
  overrides?: {
    network?: string;
    channel?: string;
    transactionFamily?: string;
    ruleProfile?: string;
  };
}

export interface ComparePayload {
  rawRequest: string;
  rawResponse: string;
  strip16ByteHeader: boolean;
}

export async function parseOnly(payload: { rawMessage: string; inputFormat: string; strip16ByteHeader: boolean }): Promise<IsoMessage> {
  const res = await fetch(`${BASE_URL}/parse`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  }
  return res.json();
}

export async function parseAndValidate(payload: ParseAndValidatePayload): Promise<ValidationReport> {
  const res = await fetch(`${BASE_URL}/parse-and-validate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  }
  return res.json();
}

export async function compareRequestResponse(payload: ComparePayload): Promise<CompareReportResponse> {
  const res = await fetch(`${BASE_URL}/compare-request-response`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  }
  return res.json();
}
