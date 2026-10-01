import React, { useState } from 'react';
import { Header } from './components/Header';
import { SingleValidatorTab } from './components/SingleValidatorTab';
import { CorrelatorTab } from './components/CorrelatorTab';
import { FileText, GitCompare, BookOpen } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'single' | 'compare' | 'guide'>('single');

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col">
      <Header />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6 flex-1 w-full">
        {/* Navigation Tabs */}
        <div className="flex border-b border-slate-800 space-x-2">
          <button
            onClick={() => setActiveTab('single')}
            className={`px-5 py-2.5 font-semibold text-sm rounded-t-lg flex items-center space-x-2 transition ${
              activeTab === 'single'
                ? 'bg-slate-800 text-blue-400 border-b-2 border-blue-500'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
            }`}
          >
            <FileText className="w-4 h-4" />
            <span>Single Message Validator</span>
          </button>
          <button
            onClick={() => setActiveTab('compare')}
            className={`px-5 py-2.5 font-medium text-sm rounded-t-lg flex items-center space-x-2 transition ${
              activeTab === 'compare'
                ? 'bg-slate-800 text-blue-400 border-b-2 border-blue-500'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
            }`}
          >
            <GitCompare className="w-4 h-4" />
            <span>Request / Response Correlator</span>
          </button>
          <button
            onClick={() => setActiveTab('guide')}
            className={`px-5 py-2.5 font-medium text-sm rounded-t-lg flex items-center space-x-2 transition ${
              activeTab === 'guide'
                ? 'bg-slate-800 text-blue-400 border-b-2 border-blue-500'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
            }`}
          >
            <BookOpen className="w-4 h-4" />
            <span>Rule Profiles &amp; Diagnostics Guide</span>
          </button>
        </div>

        {/* Tab Contents */}
        {activeTab === 'single' && <SingleValidatorTab />}
        {activeTab === 'compare' && <CorrelatorTab />}
        {activeTab === 'guide' && (
          <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-5">
            <h2 className="text-base font-bold text-white">Configurable Rule Hierarchy &amp; Profile Precedence</h2>
            <p className="text-xs text-slate-300 leading-relaxed">
              The validator dynamically resolves applicable mandatory rules using an 8-level inheritance hierarchy.
              Lower-level profiles override general profiles with explicit traceability:
            </p>
            <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800 font-mono text-xs text-slate-300 space-y-1">
              <div className="text-blue-400 font-semibold">Level 1: BASE_ISO8583_v1.0 (MTI, Bitmaps, STAN DE 11, Amount DE 4)</div>
              <div className="pl-4 text-indigo-400 font-semibold">↳ Level 2: NETWORK_CORE (Visa VIS 1.6 / Mastercard M/Chip Core)</div>
              <div className="pl-8 text-cyan-400 font-semibold">↳ Level 3: REGIONAL_PROFILE (e.g., US, Europe, Philippines Mandates)</div>
              <div className="pl-12 text-teal-400 font-semibold">↳ Level 4: TRANSACTION_PROFILE (Cash Withdrawal DE 3=01 vs Purchase DE 3=00)</div>
              <div className="pl-16 text-emerald-400 font-semibold">↳ Level 5: CHANNEL / ENTRY_MODE (ATM DE 18=6011, Chip DE 22=0510, E-Com DE 22=10)</div>
              <div className="pl-20 text-amber-400 font-semibold">↳ Level 6: TERMINAL / CARD_PRODUCT (VSDC Chip &amp; PIN vs Contactless)</div>
              <div className="pl-24 text-rose-400 font-semibold">↳ Level 7: SIMULATOR_TEST_PROFILE (Visa VTS Case 5.2 / 13.2 Rules)</div>
              <div className="pl-28 text-purple-400 font-semibold">↳ Level 8: CASE-SPECIFIC OVERRIDE (Auditable per-test overrides)</div>
            </div>

            <h3 className="text-sm font-bold text-white pt-4">Simulator Response Codes vs. Structural Errors</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
              <div className="bg-slate-900 p-3.5 rounded-xl border border-slate-800 space-y-1">
                <span className="font-bold text-amber-400 font-mono">DE 39 = 05 (&quot;Do Not Honor&quot;)</span>
                <p className="text-slate-400">
                  Generic cardholder or issuer security decline. Check card balance or simulator account status. Not an ISO parser defect.
                </p>
              </div>
              <div className="bg-slate-900 p-3.5 rounded-xl border border-slate-800 space-y-1">
                <span className="font-bold text-amber-400 font-mono">DE 39 = 55 (&quot;Incorrect PIN&quot;)</span>
                <p className="text-slate-400">
                  PIN verification failure in DE 52. VTS may mark DE 38 as &quot;Expected, But Not Received&quot; because approval code is legitimately absent on decline.
                </p>
              </div>
              <div className="bg-slate-900 p-3.5 rounded-xl border border-slate-800 space-y-1">
                <span className="font-bold text-amber-400 font-mono">DE 39 = 82 (&quot;CAM / Cryptogram Fail&quot;)</span>
                <p className="text-slate-400">
                  ARQC verification failure in DE 55 Tag 9F26. Inspect simulator UDK/MDK keys and ATC (Tag 9F36) counters.
                </p>
              </div>
            </div>
          </div>
        )}
      </main>

      <footer className="border-t border-slate-800/80 py-4 text-center text-xs text-slate-500">
        Dynamic ISO 8583 Mandatory Field Validator • Production Simulator Diagnostic System
      </footer>
    </div>
  );
};
