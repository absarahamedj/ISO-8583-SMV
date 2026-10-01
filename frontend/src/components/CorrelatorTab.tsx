import React, { useState } from 'react';
import { GitCompare, CheckCircle2, AlertCircle } from 'lucide-react';
import { CompareReportResponse } from '../types/validator';
import { compareRequestResponse } from '../api/validatorApi';

const SAMPLE_REQ = `MTI: 0100
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
F60: 0100000000`;

const SAMPLE_RESP = `MTI: 0110
Bitmap: F238448108E18000
F2: 4413600000002899
F3: 010000
F4: 000000010000
F7: 0928120000
F11: 123456
F39: 55
F41: ATM00001
Expected, But Not Received: F38, F55.91, F62.23`;

export const CorrelatorTab: React.FC = () => {
  const [reqInput, setReqInput] = useState('');
  const [respInput, setRespInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [compareData, setCompareData] = useState<CompareReportResponse | null>(null);

  const handleLoadSample = () => {
    setReqInput(SAMPLE_REQ);
    setRespInput(SAMPLE_RESP);
  };

  const handleCompare = async () => {
    if (!reqInput.trim() || !respInput.trim()) {
      alert('Please provide both a Request and a Response message.');
      return;
    }

    setLoading(true);
    try {
      const res = await compareRequestResponse({
        rawRequest: reqInput.trim(),
        rawResponse: respInput.trim(),
        strip16ByteHeader: true,
      });
      setCompareData(res);
    } catch (err: any) {
      alert(`Compare error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const reqFields = compareData?.requestReport?.parsedMessage?.fields || {};
  const respFields = compareData?.responseReport?.parsedMessage?.fields || {};
  const allDes = Array.from(new Set([...Object.keys(reqFields), ...Object.keys(respFields)]))
    .map(Number)
    .sort((a, b) => a - b);

  return (
    <div className="space-y-6">
      {/* Input Section */}
      <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-3">
          <div>
            <h2 className="text-base font-bold text-white">Correlated Request &amp; Response Comparison</h2>
            <p className="text-xs text-slate-400">
              Correlate 0100/0200 request with 0110/0210 response on STAN, RRN, and Terminal ID. Differentiate genuine omissions from decline-justified absences.
            </p>
          </div>
          <button
            onClick={handleLoadSample}
            className="px-3 py-1.5 text-xs bg-slate-800 hover:bg-slate-700 text-blue-300 rounded-lg border border-slate-700 transition"
          >
            ⎘ Load Sample Visa 0100/0110 Pair (Decline 55)
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Request Message (0100 / 0200)
            </label>
            <textarea
              rows={8}
              value={reqInput}
              onChange={(e) => setReqInput(e.target.value)}
              placeholder="Paste Request ISO message or VTS log..."
              className="w-full bg-slate-900 border border-slate-800 rounded-xl p-3 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Response Message (0110 / 0210)
            </label>
            <textarea
              rows={8}
              value={respInput}
              onChange={(e) => setRespInput(e.target.value)}
              placeholder="Paste Response ISO message or VTS log..."
              className="w-full bg-slate-900 border border-slate-800 rounded-xl p-3 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>
        </div>

        <div className="flex justify-end">
          <button
            onClick={handleCompare}
            disabled={loading}
            className="px-6 py-2.5 text-xs font-bold rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white shadow-lg shadow-blue-500/25 transition flex items-center space-x-2"
          >
            <GitCompare className="w-3.5 h-3.5" />
            <span>{loading ? 'Correlating...' : 'Compare & Correlate Pair'}</span>
          </button>
        </div>
      </div>

      {/* Comparison Results */}
      {compareData && (
        <div className="space-y-6">
          {/* Analysis Card */}
          <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-white">Correlation &amp; Root-Cause Analysis</h3>
              <div className="flex items-center space-x-3 text-xs font-mono">
                <span className="px-2.5 py-1 rounded bg-slate-900 border border-slate-800 text-slate-300">
                  STAN: {compareData.correlationStan || 'N/A'}
                </span>
                <span className="px-2.5 py-1 rounded bg-slate-900 border border-slate-800 text-slate-300">
                  TID: {compareData.correlationTerminalId || 'N/A'}
                </span>
                {compareData.correlated ? (
                  <span className="px-2.5 py-1 rounded font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center space-x-1">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>CORRELATED</span>
                  </span>
                ) : (
                  <span className="px-2.5 py-1 rounded font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30 flex items-center space-x-1">
                    <AlertCircle className="w-3.5 h-3.5" />
                    <span>UNLINKED</span>
                  </span>
                )}
              </div>
            </div>

            <div className="text-xs text-slate-300 leading-relaxed bg-slate-900/60 p-4 rounded-xl border border-slate-800/80">
              {compareData.overallAnalysis}
            </div>
          </div>

          {/* Side-by-Side Comparison Table */}
          <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-white">Side-by-Side Element Comparison</h3>
            <div className="overflow-x-auto rounded-xl border border-slate-800">
              <table className="w-full text-left text-xs text-slate-300 font-mono">
                <thead className="bg-slate-900/90 text-slate-400 font-semibold border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-3.5">DE</th>
                    <th className="py-3 px-3.5">Data Element Name</th>
                    <th className="py-3 px-3.5">Request 0100</th>
                    <th className="py-3 px-3.5">Response 0110</th>
                    <th className="py-3 px-3.5">Correlation</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-[11px]">
                  {allDes.map((de) => {
                    const rf = reqFields[de];
                    const sf = respFields[de];
                    const reqVal = rf ? rf.maskedValue : <span className="text-slate-600">[ABSENT]</span>;
                    const respVal = sf ? sf.maskedValue : <span className="text-slate-600">[ABSENT]</span>;
                    const name = rf ? rf.name : sf ? sf.name : `Data Element ${de}`;
                    const isMatch = rf && sf && rf.decodedValue === sf.decodedValue;

                    return (
                      <tr key={de} className="hover:bg-slate-900/40 transition">
                        <td className="py-2.5 px-3.5 font-bold text-blue-400">DE {de}</td>
                        <td className="py-2.5 px-3.5 text-slate-300 font-sans">{name}</td>
                        <td className="py-2.5 px-3.5 text-slate-300">{reqVal}</td>
                        <td className="py-2.5 px-3.5 text-slate-300">{respVal}</td>
                        <td className="py-2.5 px-3.5 text-xs">
                          {isMatch ? (
                            <span className="text-emerald-400 font-bold">✓ MATCH</span>
                          ) : (
                            <span className="text-slate-500">-</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
