import React, { useState, useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { SaveMeta } from '../utils/api';

const NATION_RU: Record<string, string> = {
  vikings: 'Викинги', greeks: 'Греки', carthaginians: 'Карфагеняне',
  celts: 'Кельты', macedonians: 'Македоняне', persians: 'Персы',
  romans: 'Римляне', scythians: 'Скифы',
};
const DIFFICULTY_RU: Record<string, string> = {
  chieftain: 'Вождь', commander: 'Полководец', emperor: 'Император',
  overlord: 'Повелитель', sovereign: 'Властелин',
};
const PHASE_RU: Record<string, string> = {
  player_turn: 'Ваш ход', player_discard: 'Сброс', bot_turn: 'Ход бота',
  solstice: 'Солнцестояние', final_round: 'Финал', scoring: 'Подсчёт', game_over: 'Конец',
};
const PERIOD_RU: Record<string, string> = { barbarism: 'Варварство', civilization: 'Цивилизация' };

function formatDate(iso: string): string {
  try {
    return new Date(iso).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', year: '2-digit', hour: '2-digit', minute: '2-digit' });
  } catch {
    return iso;
  }
}

interface Props {
  mode: 'save' | 'load';
  onClose: () => void;
}

export default function SaveLoadModal({ mode, onClose }: Props) {
  const { gameId, saves, loading, saveGame, listSaves, loadSave, deleteSave, setGame, gameState } = useGameStore();
  const [saveName, setSaveName] = useState('');
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    listSaves();
  }, []);

  async function handleSave() {
    if (!saveName.trim()) return;
    await saveGame(saveName.trim());
    setSaved(true);
    setSaveName('');
    await listSaves();
  }

  async function handleLoad(saveId: string) {
    await loadSave(saveId);
    onClose();
  }

  async function handleDelete(saveId: string) {
    await deleteSave(saveId);
    setConfirmDelete(null);
  }

  const overlay: React.CSSProperties = {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,.82)',
    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
  };
  const modal: React.CSSProperties = {
    background: 'linear-gradient(135deg,#0d1020,#0a0c18)',
    border: '1px solid #2a2d50', borderRadius: 14,
    padding: '28px 32px', width: 520, maxHeight: '80vh',
    display: 'flex', flexDirection: 'column', gap: 18,
  };
  const btn = (col: string, bg: string): React.CSSProperties => ({
    background: bg, border: `1px solid ${col}`, borderRadius: 6,
    color: col, padding: '5px 14px', cursor: 'pointer', fontSize: 12, fontFamily: 'inherit',
  });

  return (
    <div style={overlay} onClick={e => e.target === e.currentTarget && onClose()}>
      <div style={modal}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: 16, color: '#c8a84b', letterSpacing: '.08em', fontWeight: 700 }}>
            {mode === 'save' ? 'СОХРАНЕНИЕ ИГРЫ' : 'ЗАГРУЗКА ИГРЫ'}
          </span>
          <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: '#555', cursor: 'pointer', fontSize: 18, lineHeight: 1 }}>✕</button>
        </div>

        {/* Save form — only in save mode */}
        {mode === 'save' && (
          <div style={{ display: 'flex', gap: 8 }}>
            <input
              autoFocus
              value={saveName}
              onChange={e => { setSaveName(e.target.value); setSaved(false); }}
              onKeyDown={e => e.key === 'Enter' && handleSave()}
              placeholder="Название сохранения…"
              style={{
                flex: 1, background: '#12141f', border: '1px solid #2a2d40', borderRadius: 6,
                color: '#e8e8f0', padding: '7px 12px', fontSize: 13, fontFamily: 'inherit', outline: 'none',
              }}
            />
            <button
              onClick={handleSave}
              disabled={!saveName.trim() || loading}
              style={{ ...btn('#2ecc71', '#0a2d1a'), opacity: !saveName.trim() || loading ? 0.5 : 1 }}
            >
              {loading ? '…' : 'Сохранить'}
            </button>
          </div>
        )}

        {saved && (
          <div style={{ color: '#2ecc71', fontSize: 12, marginTop: -10 }}>Игра сохранена!</div>
        )}

        {/* Saves list */}
        <div style={{ overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
          {saves.length === 0 && !loading && (
            <div style={{ color: '#555', fontSize: 13, textAlign: 'center', padding: '24px 0' }}>
              Нет сохранённых партий
            </div>
          )}
          {saves.map(s => (
            <SaveRow
              key={s.id}
              save={s}
              mode={mode}
              confirmDelete={confirmDelete}
              onLoad={() => handleLoad(s.id)}
              onDeleteRequest={() => setConfirmDelete(s.id)}
              onDeleteConfirm={() => handleDelete(s.id)}
              onDeleteCancel={() => setConfirmDelete(null)}
              loading={loading}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function SaveRow({ save, mode, confirmDelete, onLoad, onDeleteRequest, onDeleteConfirm, onDeleteCancel, loading }: {
  save: SaveMeta;
  mode: 'save' | 'load';
  confirmDelete: string | null;
  onLoad: () => void;
  onDeleteRequest: () => void;
  onDeleteConfirm: () => void;
  onDeleteCancel: () => void;
  loading: boolean;
}) {
  const isConfirming = confirmDelete === save.id;
  const row: React.CSSProperties = {
    background: '#0f1120', border: '1px solid #1e2235', borderRadius: 8,
    padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 12,
  };
  const btn = (col: string, bg: string): React.CSSProperties => ({
    background: bg, border: `1px solid ${col}`, borderRadius: 5,
    color: col, padding: '4px 12px', cursor: 'pointer', fontSize: 11, fontFamily: 'inherit', flexShrink: 0,
  });

  return (
    <div style={row}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, color: '#e8e8f0', fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {save.name}
        </div>
        <div style={{ fontSize: 11, color: '#666', marginTop: 3 }}>
          {NATION_RU[save.player_nation] ?? save.player_nation}
          {' vs '}
          {NATION_RU[save.bot_nation] ?? save.bot_nation}
          {' · '}
          {DIFFICULTY_RU[save.difficulty] ?? save.difficulty}
          {' · '}
          Раунд {save.round_number}
          {' · '}
          {PERIOD_RU[save.player_period] ?? save.player_period}
        </div>
        <div style={{ fontSize: 10, color: '#444', marginTop: 2 }}>
          {PHASE_RU[save.game_phase] ?? save.game_phase}
          {' · '}
          {formatDate(save.updated_at)}
        </div>
      </div>

      {isConfirming ? (
        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
          <span style={{ fontSize: 11, color: '#e74c3c' }}>Удалить?</span>
          <button onClick={onDeleteConfirm} disabled={loading} style={btn('#e74c3c', '#2d0a0a')}>Да</button>
          <button onClick={onDeleteCancel} style={btn('#555', 'transparent')}>Нет</button>
        </div>
      ) : (
        <div style={{ display: 'flex', gap: 6 }}>
          {mode === 'load' && (
            <button onClick={onLoad} disabled={loading} style={btn('#3498db', '#0a1a2d')}>Загрузить</button>
          )}
          <button onClick={onDeleteRequest} style={btn('#e74c3c', 'transparent')}>✕</button>
        </div>
      )}
    </div>
  );
}
