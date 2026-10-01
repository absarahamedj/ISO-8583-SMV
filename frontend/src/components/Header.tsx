import React from 'react';
import { ShieldCheck, Database, ExternalLink, Activity } from 'lucide-react';

export const Header: React.FC = () => {
  return (
    <header className="border-b border-slate-800 bg-slate-950/80 sticky top-0 z-50 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Activity className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="font-extrabold text-base sm:text-lg text-white tracking-tight flex items-center space-x-2">
              <span>ISO 8583 Mandatory Field Validator</span>
              <span className="text-[11px] px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-400 border border-blue-500/30 hidden sm:inline-block">
              
              
               Simulator Edition
              </span>
            </h1>
            <p className="text-xs text-slate-400">
              Dynamic Rules Engine • Visa / Mastercard / VTS Root-Cause Diagnostics
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-emerald-400">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span className="hidden sm:inline">PCI Masking:</span>
            <b>Active</b>
          </div>
          <div className="flex items-center space-x-1.5 text-xs text-slate-400 bg-slate-900/60 px-3 py-1.5 rounded-lg border border-slate-800">
            <Database className="w-3.5 h-3.5 text-amber-400" />
            <span>DB:</span>
            <span className="text-amber-400 font-semibold">MySQL</span>
          </div>
          <a
            href="/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="text-xs text-slate-300 hover:text-white px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 transition flex items-center space-x-1"
          >
            <span className="hidden sm:inline">API Docs</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </header>
  );
};
