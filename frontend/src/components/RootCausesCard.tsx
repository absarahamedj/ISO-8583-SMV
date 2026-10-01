import React from 'react';
import { AlertOctagon, CheckCircle2, ChevronRight, Lightbulb } from 'lucide-react';
import { ValidationFinding } from '../types/validator';

interface RootCausesCardProps {
  rootCauses: ValidationFinding[];
}

export const RootCausesCard: React.FC<RootCausesCardProps> = ({ rootCauses }) => {
  if (rootCauses.length === 0) {
    return (
      <div className="bg-slate-950 rounded-2xl border border-emerald-900/40 p-6 shadow-xl">
        <div className="flex items-center space-x-3 text-emerald-400">
          <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
          <div className="text-xs">
            <span className="font-bold text-sm block text-emerald-300">
              Zero Blocking Root-Cause Violations
            </span>
            All required and conditional data elements are verified and conform to the active scenario profile.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-slate-950 rounded-2xl border border-red-900/40 p-6 shadow-xl space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-lg bg-red-500/20 text-red-400 flex items-center justify-center font-bold">
            <AlertOctagon className="w-5 h-5 text-red-400" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white">Prioritized Root-Cause Findings</h3>
            <p className="text-xs text-slate-400">
              Blockers ranked by payment-network failure precedence. Fix these to resolve the simulator rejection.
            </p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-red-500/20 text-red-300 border border-red-500/30">
          {rootCauses.length} Blocker{rootCauses.length > 1 ? 's' : ''}
        </span>
      </div>

      <div className="space-y-3">
        {rootCauses.map((cause) => (
          <div
            key={cause.findingId}
            className="p-4 rounded-xl bg-red-950/30 border border-red-800/60 text-xs space-y-2.5"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <span className="font-mono text-sm font-bold text-red-400">{cause.fieldPath}</span>
                <span className="text-slate-300 font-semibold">- {cause.fieldName}</span>
              </div>
              <span className="text-[10px] px-2 py-0.5 rounded bg-red-900/60 text-red-300 font-mono">
                {cause.ruleId || 'RULE-BLOCKER'}
              </span>
            </div>

            <div className="text-slate-300 leading-relaxed">{cause.reason}</div>

            {cause.conditionExpression && (
              <div className="text-[11px] font-mono text-slate-400">
                Trigger Condition: <span className="text-blue-300">{cause.conditionExpression}</span>
              </div>
            )}

            {cause.suggestedFix && (
              <div className="p-2.5 rounded-lg bg-slate-900/80 border border-slate-800 text-emerald-300 text-[11px] flex items-center space-x-2">
                <Lightbulb className="w-4 h-4 flex-shrink-0 text-amber-400" />
                <span>
                  <b>Suggested Action:</b> {cause.suggestedFix}
                </span>
              </div>
            )}

            {cause.dependentFindings && cause.dependentFindings.length > 0 && (
              <div className="mt-2 pt-2 border-t border-red-900/40 space-y-1.5">
                <span className="text-[11px] font-semibold text-red-300 flex items-center space-x-1">
                  <ChevronRight className="w-3.5 h-3.5" />
                  <span>Dependent Missing Tags (Blocked by {cause.fieldPath} absence):</span>
                </span>
                <div className="pl-4 space-y-1">
                  {cause.dependentFindings.map((dep) => (
                    <div key={dep.findingId} className="text-[11px] text-slate-300 flex items-center space-x-2">
                      <span className="w-1.5 h-1.5 rounded-full bg-red-400" />
                      <span className="font-mono text-red-300 font-bold">{dep.fieldPath}</span>
                      <span>
                        ({dep.fieldName}) - {dep.reason}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
