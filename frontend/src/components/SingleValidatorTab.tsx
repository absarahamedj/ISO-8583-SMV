import React, { useState } from 'react';
import { Play, RotateCcw, AlertTriangle } from 'lucide-react';
import { ValidationReport } from '../types/validator';
import { parseAndValidate, parseOnly } from '../api/validatorApi';
import { RootCausesCard } from './RootCausesCard';
import { MandatoryMatrixTable } from './MandatoryMatrixTable';
import { EmvTagInspector } from './EmvTagInspector';

const SAMPLES: Record<string, string> = {
  'valid-visa-atm': `MTI: 0100
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
F60: 0100000000`,

  'missing-de55': `MTI: 0100
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
F60: 0100000000`,

  'decline-55': `MTI: 0110
Bitmap: F238448108E18000
F2: 4413600000002899
F3: 010000
F4: 000000010000
F7: 0928120000
F11: 123456
F39: 55
F41: ATM00001
Expected, But Not Received: F38, F55.91, F62.23`,

  'vts-raw-dump': `--------------------------------------------------------------------------------
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
================================================================================`,
};

export const SingleValidatorTab: React.FC = () => {
  const [rawInput, setRawInput] = useState('');
  const [network, setNetwork] = useState('AUTO');
  const [format, setFormat] = useState('VTS_LOG');
  const [scenarioOverride, setScenarioOverride] = useState('');
  const [stripHeader, setStripHeader] = useState(true);
  const [loading, setLoading] = useState(false);
  const [report, setReport] = useState<ValidationReport | null>(null);

  const handleLoadSample = (key: string) => {
    setRawInput(SAMPLES[key] || '');
    setFormat('VTS_LOG');
  };

  const handleValidate = async () => {
    if (!rawInput.trim()) {
      alert('Please paste an ISO 8583 message or VTS log.');
      return;
    }

    setLoading(true);
    try {
      const res = await parseAndValidate({
        rawMessage: rawInput.trim(),
        inputFormat: format,
        strip16ByteHeader: stripHeader,
        overrides: {
          network: network !== 'AUTO' ? network : undefined,
          channel: scenarioOverride || undefined,
        },
      });
      setReport(res);
    } catch (err: any) {
      alert(`Validation error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleParseOnly = async () => {
    if (!rawInput.trim()) {
      alert('Please paste an ISO 8583 message.');
      return;
    }
    setLoading(true);
    try {
      const res = await parseOnly({
        rawMessage: rawInput.trim(),
        inputFormat: format,
        strip16ByteHeader: stripHeader,
      });
      alert(`Successfully parsed ${Object.keys(res.fields || {}).length} DEs. MTI: ${res.mti}`);
    } catch (err: any) {
      alert(`Parse error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Configuration & Input Card */}
      <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between pb-5 border-b border-slate-800/80 gap-4">
          <div>
            <h2 className="text-base font-bold text-white flex items-center space-x-2">
              <span>ISO 8583 Message Input</span>
              <span className="text-xs font-normal text-slate-400">
                (Hex, ASCII, BCD, or VTS Log)
              </span>
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Paste raw message or simulator log to automatically parse bitmaps and evaluate mandatory DEs.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs text-slate-400 font-medium">Quick Samples:</span>
            <button
              onClick={() => handleLoadSample('valid-visa-atm')}
              className="px-2.5 py-1.5 text-xs bg-slate-800 hover:bg-slate-700 text-blue-300 rounded-lg border border-slate-700 transition"
            >
              ✓ Valid Visa ATM (0100)
            </button>
            <button
              onClick={() => handleLoadSample('missing-de55')}
              className="px-2.5 py-1.5 text-xs bg-red-950/40 hover:bg-red-900/50 text-red-300 rounded-lg border border-red-800/50 transition"
            >
              ✗ Missing DE 55 Chip
            </button>
            <button
              onClick={() => handleLoadSample('decline-55')}
              className="px-2.5 py-1.5 text-xs bg-amber-950/40 hover:bg-amber-900/50 text-amber-300 rounded-lg border border-amber-800/50 transition"
            >
              ⚠ Simulator Decline 55
            </button>
            <button
              onClick={() => handleLoadSample('vts-raw-dump')}
              className="px-2.5 py-1.5 text-xs bg-indigo-950/50 hover:bg-indigo-900/60 text-indigo-300 rounded-lg border border-indigo-700/50 transition font-semibold"
            >
              ⎘ User VTS Dump (0110 Decline 05)
            </button>
          </div>
        </div>

        {/* Controls Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 py-4 text-xs">
          <div>
            <label className="block text-slate-300 font-medium mb-1.5">Network Profile</label>
            <select
              value={network}
              onChange={(e) => setNetwork(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="AUTO">Auto-Detect Network</option>
              <option value="VISA">Visa (VIS / VSDC)</option>
              <option value="MASTERCARD">Mastercard (M/Chip)</option>
              <option value="COMMON">Common ISO 8583</option>
            </select>
          </div>
          <div>
            <label className="block text-slate-300 font-medium mb-1.5">Input Format</label>
            <select
              value={format}
              onChange={(e) => setFormat(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="VTS_LOG">VTS Log / Formatted Text</option>
              <option value="HEX">Raw Hexadecimal</option>
              <option value="ASCII">Raw ASCII</option>
              <option value="BCD">BCD / Binary</option>
            </select>
          </div>
          <div>
            <label className="block text-slate-300 font-medium mb-1.5">Scenario Override</label>
            <select
              value={scenarioOverride}
              onChange={(e) => setScenarioOverride(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="">None (Auto-Classify from DE 3/22)</option>
              <option value="ATM">ATM Cash Disbursement</option>
              <option value="POS">POS Chip Purchase</option>
              <option value="ECOMMERCE">E-Commerce Purchase</option>
            </select>
          </div>
          <div className="flex items-center pt-6">
            <label className="inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={stripHeader}
                onChange={(e) => setStripHeader(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-9 h-5 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
              <span className="ml-2.5 text-xs text-slate-300">Strip 16-byte Header</span>
            </label>
          </div>
        </div>

        {/* Message Textarea */}
        <div>
          <textarea
            rows={8}
            value={rawInput}
            onChange={(e) => setRawInput(e.target.value)}
            placeholder="Paste ISO 8583 raw hex, ASCII, or VTS log format here..."
            className="w-full bg-slate-900/90 border border-slate-800 rounded-xl p-3.5 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between mt-4">
          <button
            onClick={() => {
              setRawInput('');
              setReport(null);
            }}
            className="text-xs text-slate-400 hover:text-slate-200 px-3 py-2 rounded-lg hover:bg-slate-900 transition flex items-center space-x-1"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Clear Input</span>
          </button>
          <div className="flex items-center space-x-3">
            <button
              onClick={handleParseOnly}
              disabled={loading}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition"
            >
              Parse Only
            </button>
            <button
              onClick={handleValidate}
              disabled={loading}
              className="px-5 py-2 text-xs font-bold rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white shadow-lg shadow-blue-500/25 transition flex items-center space-x-2"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>{loading ? 'Validating...' : 'Parse & Validate Message'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Results Section */}
      {report && (
        <div className="space-y-6">
          {/* Diagnostic Status Banner */}
          <div
            className={`rounded-2xl border p-6 flex flex-col lg:flex-row lg:items-center justify-between gap-4 ${
              report.status === 'PASS'
                ? 'border-emerald-800/40 bg-emerald-950/20'
                : report.status === 'PASS_WITH_WARNINGS'
                ? 'border-blue-800/40 bg-blue-950/20'
                : 'border-red-800/40 bg-red-950/20'
            }`}
          >
            <div className="space-y-1">
              <div className="flex items-center space-x-3">
                <span
                  className={`px-3 py-1 rounded-full text-xs font-extrabold uppercase tracking-wide border ${
                    report.status === 'PASS'
                      ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                      : report.status === 'PASS_WITH_WARNINGS'
                      ? 'bg-blue-500/20 text-blue-400 border-blue-500/30'
                      : 'bg-red-500/20 text-red-400 border-red-500/30'
                  }`}
                >
                  {report.status}
                </span>
                <span className="text-sm font-bold text-white">
                  {report.scenario?.network} {report.scenario?.channel} {report.scenario?.transactionFamily}
                </span>
                <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                  {Math.round((report.scenario?.confidenceScore || 1.0) * 100)}% Confidence
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Applied Profile Stack:{' '}
                <span className="font-mono text-slate-300">
                  {report.scenario?.appliedRuleStack?.join(' > ')}
                </span>
              </p>
            </div>

            {/* KPI Counter */}
            <div className="grid grid-cols-3 sm:grid-cols-6 gap-2 text-center">
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-slate-400">Parsed DEs</span>
                <span className="text-base font-extrabold text-white">{report.totalParsedFields}</span>
              </div>
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-emerald-400">Valid</span>
                <span className="text-base font-extrabold text-emerald-400">
                  {report.presentAndValidCount}
                </span>
              </div>
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-red-400">Missing Mand.</span>
                <span className="text-base font-extrabold text-red-400">
                  {report.missingMandatoryCount}
                </span>
              </div>
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-amber-400">Missing Cond.</span>
                <span className="text-base font-extrabold text-amber-400">
                  {report.missingConditionalCount}
                </span>
              </div>
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-purple-400">Prohibited</span>
                <span className="text-base font-extrabold text-purple-400">
                  {report.prohibitedFieldsCount}
                </span>
              </div>
              <div className="bg-slate-900/80 px-3 py-2 rounded-xl border border-slate-800">
                <span className="block text-xs text-blue-400">Warnings</span>
                <span className="text-base font-extrabold text-blue-400">
                  {report.warnings?.length || 0}
                </span>
              </div>
            </div>
          </div>

          {/* Root Causes Section */}
          <RootCausesCard rootCauses={report.rootCauses || []} />

          {/* Business Decline Notice */}
          {report.businessDeclineCode && (
            <div className="bg-slate-950 rounded-2xl border border-amber-900/40 p-6 shadow-xl space-y-2">
              <div className="flex items-center space-x-2 text-amber-400 font-bold text-sm">
                <AlertTriangle className="w-4 h-4" />
                <span>Simulator Decline Interpretation (DE 39 = {report.businessDeclineCode})</span>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                Simulator returned response code <b>{report.businessDeclineCode}</b>:{' '}
                <em>{report.businessDeclineDescription}</em>.
                <br />
                {report.isPureBusinessDecline ? (
                  <span className="text-emerald-400 font-semibold">
                    ✓ Note: Request message is structurally valid. This response is an account/PIN decline generated by the simulator, not an ISO framing failure.
                  </span>
                ) : (
                  <span className="text-amber-400 font-semibold">
                    ⚠ Warning: Message contains structural rule failures which likely provoked this simulator decline.
                  </span>
                )}
              </p>
            </div>
          )}

          {/* Complete Matrix Table */}
          <MandatoryMatrixTable matrix={report.completeMatrix || []} />

          {/* Parsed Fields & EMV Tag Inspector */}
          <EmvTagInspector message={report.parsedMessage} />
        </div>
      )}
    </div>
  );
};
