import React, { useState, useMemo } from 'react';
import { Search, Filter } from 'lucide-react';
import { ValidationFinding } from '../types/validator';

interface MandatoryMatrixTableProps {
  matrix: ValidationFinding[];
}

export const MandatoryMatrixTable: React.FC<MandatoryMatrixTableProps> = ({ matrix }) => {
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'VIOLATIONS' | 'MANDATORY' | 'CONDITIONAL' | 'PRESENT'>('ALL');

  const filteredData = useMemo(() => {
    return matrix.filter((row) => {
      const q = search.toLowerCase();
      const matchesSearch =
        row.fieldPath.toLowerCase().includes(q) ||
        row.fieldName.toLowerCase().includes(q) ||
        (row.ruleId && row.ruleId.toLowerCase().includes(q));

      if (!matchesSearch) return false;

      if (filter === 'VIOLATIONS') return !row.valid;
      if (filter === 'MANDATORY') return row.requirementLevel === 'MANDATORY';
      if (filter === 'CONDITIONAL') return row.requirementLevel === 'CONDITIONAL';
      if (filter === 'PRESENT') return row.present;

      return true;
    });
  }, [matrix, search, filter]);

  return (
    <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-bold text-white">Complete Scenario Mandatory Matrix</h3>
          <p className="text-xs text-slate-400">
            Evaluation of all expected, conditional, and optional elements against active rules.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <div className="relative">
            <Search className="w-3.5 h-3.5 absolute left-2.5 top-2.5 text-slate-500" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search DE / Tag / Name..."
              className="bg-slate-900 border border-slate-800 rounded-lg pl-8 pr-3 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>
          <div className="relative flex items-center">
            <Filter className="w-3.5 h-3.5 absolute left-2.5 text-slate-500" />
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              className="bg-slate-900 border border-slate-800 rounded-lg pl-8 pr-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="ALL">All Elements ({matrix.length})</option>
              <option value="VIOLATIONS">Violations Only</option>
              <option value="MANDATORY">Mandatory</option>
              <option value="CONDITIONAL">Conditional</option>
              <option value="PRESENT">Present</option>
            </select>
          </div>
        </div>
      </div>

      <div className="overflow-x-auto rounded-xl border border-slate-800">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-900/90 text-slate-400 font-semibold border-b border-slate-800">
            <tr>
              <th className="py-3 px-3.5">Field / Tag</th>
              <th className="py-3 px-3.5">Element Name</th>
              <th className="py-3 px-3.5">Requirement</th>
              <th className="py-3 px-3.5">Present?</th>
              <th className="py-3 px-3.5">Valid?</th>
              <th className="py-3 px-3.5">Actual (Masked)</th>
              <th className="py-3 px-3.5">Expected</th>
              <th className="py-3 px-3.5">Rule ID</th>
              <th className="py-3 px-3.5">Diagnosis / Reason</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60 font-mono text-[11px]">
            {filteredData.length === 0 ? (
              <tr>
                <td colSpan={9} className="py-8 text-center text-slate-500 italic">
                  No elements matching filter criteria.
                </td>
              </tr>
            ) : (
              filteredData.map((row) => (
                <tr key={row.findingId} className="hover:bg-slate-900/40 transition">
                  <td className="py-2.5 px-3.5 font-bold text-slate-200">{row.fieldPath}</td>
                  <td className="py-2.5 px-3.5 text-slate-300 font-sans">{row.fieldName}</td>
                  <td className="py-2.5 px-3.5">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] bg-slate-900 border border-slate-800 ${
                        row.requirementLevel === 'MANDATORY'
                          ? 'text-blue-300 font-bold'
                          : row.requirementLevel === 'PROHIBITED'
                          ? 'text-purple-400'
                          : 'text-slate-400'
                      }`}
                    >
                      {row.requirementLevel}
                    </span>
                  </td>
                  <td className="py-2.5 px-3.5">
                    {row.present ? (
                      <span className="text-emerald-400 font-semibold">Yes</span>
                    ) : (
                      <span className="text-slate-500">No</span>
                    )}
                  </td>
                  <td
                    className={`py-2.5 px-3.5 ${
                      row.valid ? 'text-emerald-400' : 'text-red-400 font-bold'
                    }`}
                  >
                    {row.valid ? '✓ Valid' : '✗ Failed'}
                  </td>
                  <td className="py-2.5 px-3.5 text-slate-300 max-w-xs truncate">
                    {row.actualValueMasked || (
                      <span className="text-slate-600">[ABSENT]</span>
                    )}
                  </td>
                  <td className="py-2.5 px-3.5 text-slate-400">
                    {row.expectedValueOrFormat || '-'}
                  </td>
                  <td className="py-2.5 px-3.5 text-slate-400 font-mono text-[10px]">
                    {row.ruleId || '-'}
                  </td>
                  <td className="py-2.5 px-3.5 text-slate-300 font-sans max-w-sm">
                    {row.reason}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
