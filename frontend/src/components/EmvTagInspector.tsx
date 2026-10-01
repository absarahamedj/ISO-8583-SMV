import React from 'react';
import { IsoMessage } from '../types/validator';
import { Layers, Binary } from 'lucide-react';

interface EmvTagInspectorProps {
  message?: IsoMessage;
}

export const EmvTagInspector: React.FC<EmvTagInspectorProps> = ({ message }) => {
  if (!message) return null;

  const fields = message.fields || {};
  const deKeys = Object.keys(fields)
    .map(Number)
    .sort((a, b) => a - b);

  const de55 = fields['55'] || fields[55];
  const emvTags = de55?.emvTags || [];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Parsed ISO Fields */}
      <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-3">
        <h3 className="text-sm font-bold text-white flex items-center justify-between">
          <span className="flex items-center space-x-2">
            <Layers className="w-4 h-4 text-blue-400" />
            <span>Parsed ISO 8583 Data Elements</span>
          </span>
          <span className="text-xs font-normal text-slate-400">
            {deKeys.length} Fields Extracted
          </span>
        </h3>
        <div className="space-y-2 max-h-96 overflow-y-auto pr-1">
          {deKeys.map((de) => {
            const f = fields[de];
            return (
              <div
                key={de}
                className="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono flex items-center justify-between"
              >
                <div className="flex items-center space-x-2">
                  <span className="w-12 font-bold text-blue-400">DE {f.fieldNumber}</span>
                  <span className="text-slate-300 font-sans">{f.name}</span>
                </div>
                <div className="flex items-center space-x-3">
                  <span className="text-slate-400 text-[11px] truncate max-w-[180px]">
                    {f.maskedValue || f.decodedValue}
                  </span>
                  <span className="text-[10px] text-slate-500">
                    [{f.startOffset}-{f.endOffset}b]
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* DE 55 EMV Tags */}
      <div className="bg-slate-950 rounded-2xl border border-slate-800 p-6 shadow-xl space-y-3">
        <h3 className="text-sm font-bold text-white flex items-center justify-between">
          <span className="flex items-center space-x-2">
            <Binary className="w-4 h-4 text-indigo-400" />
            <span>DE 55 ICC BER-TLV Inspector</span>
          </span>
          <span className="text-xs font-normal text-slate-400">
            {emvTags.length} EMV Tag{emvTags.length === 1 ? '' : 's'}
          </span>
        </h3>
        <div className="space-y-2 max-h-96 overflow-y-auto pr-1">
          {emvTags.length === 0 ? (
            <p className="text-xs text-slate-500 italic py-6 text-center">
              No DE 55 ICC data present in this message.
            </p>
          ) : (
            emvTags.map((t, idx) => (
              <div
                key={idx}
                className="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono space-y-1.5"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <span className="font-bold text-indigo-400">Tag {t.tag}</span>
                    <span className="text-slate-300 font-sans">{t.name}</span>
                  </div>
                  <span className="text-[10px] text-slate-400">Len: {t.length}B</span>
                </div>
                <div className="text-[11px] text-slate-400 break-all bg-slate-950/60 p-2 rounded border border-slate-800/60">
                  {t.maskedHex || t.rawHex}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
