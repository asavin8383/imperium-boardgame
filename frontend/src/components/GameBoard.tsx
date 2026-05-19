import React, { useState, useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { CardInfo, MarketSlot as MarketSlotInfo } from '../types/game';
import regIcon from '../assets/icons/category/РЕГ.svg';
import istIcon from '../assets/icons/category/ИСТ.svg';
import civIcon from '../assets/icons/category/ЦИВ.svg';
import besIcon from '../assets/icons/category/БЕС.svg';
import nabIcon from '../assets/icons/category/НАБ.svg';
import slvIcon from '../assets/icons/category/СЛВ.svg';
import spsIcon from '../assets/icons/category/СПС.svg';
import varIcon from '../assets/icons/period/ВАР.svg';
import impIcon from '../assets/icons/period/ИМП.svg';
import postIcon from '../assets/icons/type/ПОСТ.svg';
import atkIcon from '../assets/icons/type/АТК.svg';

const CAT_COLOR: Record<string, string> = {
  region: '#2ecc71', origins: '#e67e22', civilization: '#3498db',
  raid: '#e74c3c', glory: '#f1c40f', disorder: '#9b59b6',
  ability: '#1abc9c', boost: '#fd7c40', transformation: '#fd7c40', progress: '#95a5a6',
};
const CAT_RU: Record<string, string> = {
  region: 'Регион', origins: 'Исток', civilization: 'Цивилизация',
  raid: 'Набег', glory: 'Слава', disorder: 'Беспорядки',
  ability: 'Способность', boost: 'Усиление', progress: 'Прогресс',
};
const PERIOD_RU: Record<string, string> = { barbarism: '⚔ Варварство', civilization: '🏛 Цивилизация' };
const VP_PER_LABEL: Record<string, string> = {
  region: 'рег', population: 'нас', civilization: 'цив',
  origins: 'ист', resource: 'рес', attack: 'ат', sea: 'море', glory: 'сл',
};
const REGION_ICON: Record<string, string> = { land: '🌾', sea: '🌊', mountain: '🏔' };
const PHASE_RU: Record<string, string> = {
  player_turn: '🎯 Ваш ход', player_discard: '🗑 Сброс карт', bot_turn: '🤖 Ход бота', solstice: '☀ Солнцестояние',
  final_round: '⚡ Финальный раунд', scoring: '🏆 Подсчёт ПО', game_over: '🏁 Конец игры', setup: '⚙ Подготовка',
};

function catColor(card: CardInfo) {
    const cat = card.categories?.[0];
    return cat ? (CAT_COLOR[cat] ?? '#555') : '#555';
}

const imgCache = new Map<string, 'loaded' | 'error'>();

function CardView({ card, selected = false, onClick, size = 'normal', dimmed = false, badge }: {
  card: CardInfo; selected?: boolean; onClick?: () => void;
  size?: 'small' | 'normal' | 'large' | 'xlarge'; dimmed?: boolean; badge?: React.ReactNode;
}) {
  const color = catColor(card);
  const [hov, setHov] = useState(false);
  const cached = imgCache.get(card.id);
  const [imgErr, setImgErr] = useState(cached === 'error');
  const [imgLoaded, setImgLoaded] = useState(cached === 'loaded');
  const d = { small: { w: 90, h: 114, n: 10, c: 8 }, normal: { w: 123, h: 163, n: 11, c: 9 }, large: { w: 143, h: 185, n: 12, c: 10 }, xlarge: { w: 180, h: 240, n: 15, c: 12 } }[size];
  return (
    <div onClick={onClick} onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)} style={{
      width: d.w, minHeight: d.h, flexShrink: 0,
      background: selected ? `linear-gradient(145deg,#1a3a2a,#0d2117)` : hov && onClick ? `linear-gradient(145deg,#1e2640,#131828)` : `linear-gradient(145deg,#12141f,#0a0c14)`,
      border: `${selected ? 2 : 1}px solid ${selected ? color : hov && onClick ? color + '88' : '#2a2d40'}`,
      borderRadius: 8, cursor: onClick ? 'pointer' : 'default', opacity: dimmed ? 0.4 : 1,
      transform: selected ? 'translateY(-4px)' : hov && onClick ? 'translateY(-2px)' : 'none',
      transition: 'all 0.15s', display: 'flex', flexDirection: 'column', overflow: 'hidden', position: 'relative',
      boxShadow: selected ? `0 0 12px ${color}44` : hov && onClick ? `0 4px 12px rgba(0,0,0,.5)` : `0 2px 4px rgba(0,0,0,.4)`,
    }}>
      {!imgErr && (
        <img
          src={`/cards/${card.id}.jpg`}
          alt=""
          onLoad={() => { imgCache.set(card.id, 'loaded'); setImgLoaded(true); }}
          onError={() => { imgCache.set(card.id, 'error'); setImgErr(true); }}
          style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover', opacity: imgLoaded ? 1 : 0.75, borderRadius: 7, pointerEvents: 'none' }}
        />
      )}
      {!imgLoaded && <div style={{ height: 4, background: color, flexShrink: 0 }} />}
      {!imgLoaded && (
        <div style={{ padding: '6px 7px', flex: 1, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: 4, flex: 1 }}>
            {card.card_type === 'permanent' && (
              <img src={postIcon} alt="Постоянная" style={{ width: 18, height: 18, opacity: 0.85, flexShrink: 0, marginTop: 1 }} />
            )}
            {card.card_type === 'attack' && (
              <img src={atkIcon} alt="Атака" style={{ width: 18, height: 18, opacity: 0.85, flexShrink: 0, marginTop: 1 }} />
            )}
            <div style={{ fontSize: d.n, fontWeight: 600, color: '#e8e8f0', lineHeight: 1.3, flex: 1 }}>{card.name}</div>
            {card.period && (
              <img
                src={card.period === 'barbarism' ? varIcon : impIcon}
                alt={card.period === 'barbarism' ? 'Варварство' : 'Цивилизация'}
                style={{ width: 18, height: 18, opacity: 0.85, flexShrink: 0, marginTop: 1 }}
              />
            )}
          </div>
          {/* Effect icons */}
          <div style={{ display: 'flex', gap: 3, flexWrap: 'wrap', minHeight: 12 }}>
            {card.card_type === 'attack' && <span title="Атака" style={{ fontSize: d.c - 1, color: '#e74c3c' }}>⚔</span>}
            {card.is_exploit && <span title="Эксплуатация" style={{ fontSize: d.c - 1, color: '#e67e22' }}>✕</span>}
            {card.passive_effect && <span title="Пассивный эффект" style={{ fontSize: d.c - 1, color: '#1abc9c' }}>∞</span>}
            {card.solstice_effect && <span title="Солнцестояние" style={{ fontSize: d.c - 1, color: '#f1c40f' }}>☀</span>}
            {(card.region_types ?? []).map(rt => REGION_ICON[rt] && (
              <span key={rt} style={{ fontSize: d.c - 1 }}>{REGION_ICON[rt]}</span>
            ))}
            {(card.progress_cost_resource ?? 0) > 0 && (
              <span style={{ fontSize: d.c - 1, color: '#e67e22' }}>⚙{card.progress_cost_resource}</span>
            )}
            {(card.progress_cost_population ?? 0) > 0 && (
              <span style={{ fontSize: d.c - 1, color: '#2ecc71' }}>👥{card.progress_cost_population}</span>
            )}
            {(card.progress_cost_upgrade ?? 0) > 0 && (
              <span style={{ fontSize: d.c - 1, color: '#3498db' }}>▶{card.progress_cost_upgrade}</span>
            )}
            {(card.gives_resource ?? 0) > 0 && (
              <span title="Даёт ресурсы" style={{ fontSize: d.c - 1, color: '#e67e22' }}>+⚙{card.gives_resource}</span>
            )}
            {(card.gives_population ?? 0) > 0 && (
              <span title="Даёт население" style={{ fontSize: d.c - 1, color: '#2ecc71' }}>+👥{card.gives_population}</span>
            )}
            {(card.gives_progress ?? 0) > 0 && (
              <span title="Даёт жетоны прогресса" style={{ fontSize: d.c - 1, color: '#f1c40f' }}>+◆{card.gives_progress}</span>
            )}
            {(card.draws_cards ?? 0) > 0 && (
              <span title="Тянет карты" style={{ fontSize: d.c - 1, color: '#1abc9c' }}>+🃏{card.draws_cards}</span>
            )}
            {(card.steal_progress ?? 0) > 0 && (
              <span title="Забирает жетоны прогресса у соперника" style={{ fontSize: d.c - 1, color: '#e74c3c' }}>−◆{card.steal_progress}</span>
            )}
            {(card.steal_population ?? 0) > 0 && (
              <span title="Забирает население у соперника" style={{ fontSize: d.c - 1, color: '#e74c3c' }}>−👥{card.steal_population}</span>
            )}
            {card.discard_opponent_card && (
              <span title="Соперник сбрасывает карту из игровой зоны" style={{ fontSize: d.c - 1, color: '#e74c3c' }}>✂</span>
            )}
            {(card.gives_disorder ?? 0) > 0 && (
              <span title="Даёт сопернику карты беспорядков" style={{ fontSize: d.c - 1, color: '#9b59b6' }}>⚡{card.gives_disorder}</span>
            )}
            {card.can_be_reinforced && (
              <span title="Можно укрепить" style={{ fontSize: d.c - 1, color: '#4a90d9' }}>🛡</span>
            )}
            {(card.sends_to_chronicle ?? 0) > 0 && (
              <span title="Отправляет карты в летопись" style={{ fontSize: d.c - 1, color: '#1abc9c' }}>📜{card.sends_to_chronicle}</span>
            )}
            {card.goes_to_chronicle && (
              <span title="Идёт в летопись после розыгрыша" style={{ fontSize: d.c - 1, color: '#c8a84b' }}>📜→</span>
            )}
          </div>
          {card.categories && card.categories.length > 0 && (() => {
            const iconMap: Record<string, { src: string; alt: string }> = {
              region:       { src: regIcon, alt: 'Регион' },
              origins:      { src: istIcon, alt: 'Исток' },
              civilization: { src: civIcon, alt: 'Цивилизация' },
              disorder:     { src: besIcon, alt: 'Беспорядки' },
              raid:         { src: nabIcon, alt: 'Набег' },
              glory:        { src: slvIcon, alt: 'Слава' },
              ability:      { src: spsIcon, alt: 'Способность' },
            };
            const icon = iconMap[card.categories[0]];
            return icon ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '4px 0 6px' }}>
                <img src={icon.src} alt={icon.alt} style={{ width: 24, height: 24, opacity: 0.85 }} />
              </div>
            ) : null;
          })()}
        </div>
      )}
      {badge && <div style={{ position: 'absolute', top: 6, right: 6 }}>{badge}</div>}
      {!imgLoaded && (
        <div style={{ position: 'absolute', bottom: 5, right: 5, display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 2 }}>
          {(card.vp_fixed ?? 0) > 0 && (
            <div style={{ background: '#f1c40f', color: '#000', borderRadius: 3, padding: '1px 4px', fontSize: d.c, fontWeight: 700, lineHeight: 1.2 }}>{card.vp_fixed}</div>
          )}
          {(card.vp_penalty ?? 0) > 0 && (
            <div style={{ background: '#e74c3c', color: '#fff', borderRadius: 3, padding: '1px 4px', fontSize: d.c, fontWeight: 700, lineHeight: 1.2 }}>−{card.vp_penalty}</div>
          )}
          {card.vp_per_condition && (
            <div style={{ color: '#f1c40f', fontSize: d.c - 1, fontWeight: 700, lineHeight: 1.2 }}>*/{VP_PER_LABEL[card.vp_per_condition] ?? card.vp_per_condition}</div>
          )}
        </div>
      )}
    </div>
  );
}

function DeckPile({ count, label, color = '#2a2d40' }: { count: number; label: string; color?: string }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
      <div style={{
        width: 50, height: 68, background: `linear-gradient(145deg,${color},${color}88)`,
        border: '1px solid #3a3d50', borderRadius: 6, display: 'flex', alignItems: 'center',
        justifyContent: 'center', fontSize: 18, fontWeight: 700, color: '#e8e8f0',
        boxShadow: '2px 2px 0 #1a1a2e',
      }}>{count}</div>
      <div style={{ fontSize: 9, color: '#666', textAlign: 'center', maxWidth: 58 }}>{label}</div>
    </div>
  );
}

function Token({ type, value }: { type: string; value: number }) {
  const c: Record<string, { l: string; c: string; b: string }> = {
    resource: { l: '⚙', c: '#e67e22', b: '#3d1f0a' }, population: { l: '👥', c: '#2ecc71', b: '#0a2d1a' },
    upgrade: { l: '▶', c: '#3498db', b: '#0a1f3d' }, action: { l: '●', c: '#f1c40f', b: '#2d2600' }, exploit: { l: '✕', c: '#e74c3c', b: '#2d0a0a' },
  };
  const cfg = c[type] ?? { l: '?', c: '#888', b: '#222' };
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <div style={{ background: cfg.b, border: `1px solid ${cfg.c}55`, borderRadius: 5, padding: '3px 7px', fontSize: 15, fontWeight: 700, color: cfg.c, minWidth: 32, textAlign: 'center' }}>{value}</div>
      <div style={{ fontSize: 8, color: cfg.c }}>{cfg.l}</div>
    </div>
  );
}

function Btn({ label, onClick, color = '#2ecc71', disabled = false, active = false, icon }: {
  label: string; onClick: () => void; color?: string; disabled?: boolean; active?: boolean; icon?: string;
}) {
  return (
    <button onClick={onClick} disabled={disabled} style={{
      background: active ? color : disabled ? '#141420' : `${color}18`,
      border: `1px solid ${disabled ? '#2a2d40' : color}`,
      borderRadius: 7, color: active ? '#000' : disabled ? '#3a3d50' : color,
      padding: '7px 12px', cursor: disabled ? 'not-allowed' : 'pointer', fontSize: 11, fontWeight: 600,
      transition: 'all .15s', display: 'flex', alignItems: 'center', gap: 4,
    }}>
      {icon && <span>{icon}</span>}{label}
    </button>
  );
}

export default function GameBoard() {
  const { gameState: gs, loading, error, playCard, exploitCard, doInnovation, doRevolution, endTurn, acquireCard, accelerateProgress, resetGame, undoAction } = useGameStore();
  const [selectedCards, setSelectedCards] = useState<string[]>([]);
  const [mode, setMode] = useState<'activation' | 'innovation' | 'revolution' | null>(null);
  const [acquireMode, setAcquireMode] = useState(false);
  const [innovationCat, setInnovationCat] = useState<string | null>(null);
  const [scores, setScores] = useState<{ player: number; bot: number } | null>(null);
  const [previewCard, setPreviewCard] = useState<CardInfo | null>(null);
  const [previewOrigin, setPreviewOrigin] = useState('top left');
  const previewRef = React.useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!previewCard) return;
    const handler = (e: MouseEvent) => {
      if (previewRef.current && !previewRef.current.contains(e.target as Node)) {
        setPreviewCard(null);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [previewCard]);

  if (!gs) return null;
  const { player, bot, shared } = gs;
  const isPlayerTurn = gs.phase === 'player_turn';
  const isDiscardPhase = gs.phase === 'player_discard';
  const isGameOver = gs.phase === 'game_over' || gs.phase === 'scoring';

  useEffect(() => {
    if (isGameOver && !scores) {
      const sl = gs.log.find(l => l.includes('Игрок:') && l.includes('ПО'));
      if (sl) {
        const m = sl.match(/(\d+)/g);
          if (m && m.length >= 2) setScores({ player: parseInt(m[0]), bot: parseInt(m[1]) });
      }
    }
  }, [isGameOver, gs.log]);

  function toggleCard(id: string) {
    setSelectedCards(p => p.includes(id) ? p.filter(x => x !== id) : [...p, id]);
  }

  async function handlePlay() {
    for (const id of selectedCards) await playCard(id);
    setSelectedCards([]);
  }
  async function handleExploit() {
    if (selectedCards.length !== 1) return;
    await exploitCard(selectedCards[0]);
    setSelectedCards([]);
  }
  async function handleInnovation() {
    if (!innovationCat) return;
    setMode(null);
    await doInnovation(innovationCat);
    setInnovationCat(null);
  }
  async function handleRevolution() {
    setMode(null);
    await doRevolution(selectedCards);
    setSelectedCards([]);
  }
  async function handleEndTurn() {
    setMode(null); setSelectedCards([]); setAcquireMode(false);
    await endTurn([]);
  }
  async function handleConfirmDiscard() {
    await endTurn(selectedCards);
    setSelectedCards([]);
  }

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(160deg,#0a0c14,#080a10)', color: '#e8e8f0', fontFamily: "'Georgia', serif", display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{ background: '#0b0d18', borderBottom: '1px solid #1e2235', padding: '9px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontSize: 18, color: '#f1c40f' }}>⚔</span>
          <span style={{ fontSize: 15, fontWeight: 700, letterSpacing: '.15em', color: '#c8a84b' }}>ИМПЕРИИ: КЛАССИКА</span>
          <span style={{ background: '#12141f', border: '1px solid #2a2d40', borderRadius: 4, padding: '2px 8px', fontSize: 10, color: '#666' }}>Раунд {gs.round_number}</span>
          {gs.is_final_round && <span style={{ background: '#3d1a00', border: '1px solid #e67e22', borderRadius: 4, padding: '2px 8px', fontSize: 10, color: '#e67e22', fontWeight: 700 }}>⚡ ФИНАЛЬНЫЙ</span>}
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div style={{ padding: '3px 10px', background: isPlayerTurn ? '#0a2d1a' : isDiscardPhase ? '#1a0a2d' : '#1a0a1a', border: `1px solid ${isPlayerTurn ? '#2ecc71' : isDiscardPhase ? '#9b59b6' : '#9b59b6'}`, borderRadius: 5, fontSize: 11, color: isPlayerTurn ? '#2ecc71' : isDiscardPhase ? '#c39bd3' : '#9b59b6', fontWeight: 600 }}>
            {PHASE_RU[gs.phase] ?? gs.phase}
          </div>
          <button onClick={resetGame} style={{ background: 'transparent', border: '1px solid #2a2d40', borderRadius: 5, color: '#555', padding: '3px 9px', cursor: 'pointer', fontSize: 10 }}>Новая игра</button>
        </div>
      </div>

      {error && <div style={{ background: '#200a0a', borderBottom: '1px solid #e74c3c', padding: '6px 20px', fontSize: 11, color: '#e74c3c' }}>⚠ {error}</div>}

      {/* Game over modal */}
      {isGameOver && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.88)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999 }}>
          <div style={{ background: 'linear-gradient(135deg,#0d1020,#0a0c18)', border: '1px solid #2a2d50', borderRadius: 14, padding: 40, maxWidth: 380, width: '90%', textAlign: 'center' }}>
            <div style={{ fontSize: 22, color: '#c8a84b', letterSpacing: '.1em', marginBottom: 24 }}>КОНЕЦ ИГРЫ</div>
            {scores ? (
              <>
                <div style={{ display: 'flex', gap: 32, justifyContent: 'center', marginBottom: 20 }}>
                  <div><div style={{ fontSize: 48, fontWeight: 700, color: '#2ecc71' }}>{scores.player}</div><div style={{ fontSize: 12, color: '#2ecc71' }}>Вы</div></div>
                  <div style={{ color: '#333', fontSize: 28, alignSelf: 'center' }}>—</div>
                  <div><div style={{ fontSize: 48, fontWeight: 700, color: '#e74c3c' }}>{scores.bot}</div><div style={{ fontSize: 12, color: '#e74c3c' }}>Бот</div></div>
                </div>
                <div style={{ fontSize: 18, fontWeight: 700, color: scores.player > scores.bot ? '#f1c40f' : '#e74c3c', marginBottom: 24 }}>
                  {scores.player > scores.bot ? '🏆 Вы победили!' : '😞 Победил бот'}
                </div>
              </>
            ) : (
              <div style={{ marginBottom: 24, fontSize: 13, color: '#888' }}>
                {gs.log.slice(-5).map((l, i) => <div key={i}>{l}</div>)}
              </div>
            )}
            <button onClick={resetGame} style={{ background: 'linear-gradient(135deg,#c8a84b,#9a7d38)', border: 'none', borderRadius: 8, color: '#000', padding: '10px 28px', cursor: 'pointer', fontSize: 13, fontWeight: 700, letterSpacing: '.05em' }}>Играть снова</button>
          </div>
        </div>
      )}

      {/* Main layout */}
      <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 260px 1fr', overflow: 'hidden' }}>

        {/* LEFT — Player */}
        <div style={{ padding: '14px 12px', display: 'flex', flexDirection: 'column', gap: 12, overflow: 'auto', borderRight: '1px solid #1e2235' }}>
          {/* Player info */}
          <div style={{ background: '#0d1020', border: '1px solid #1e2235', borderRadius: 8, padding: '8px 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: 13, fontWeight: 700, color: '#c8a84b', letterSpacing: '.1em' }}>{player?.nation?.toUpperCase()}</div>
              <div style={{ fontSize: 10, color: player?.period === 'barbarism' ? '#e74c3c' : '#3498db', marginTop: 2 }}>{PERIOD_RU[player?.period ?? '']}</div>
            </div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
              {(['resource', 'population', 'upgrade', 'action', 'exploit'] as const).map(t => (
                <Token key={t} type={t} value={(player?.resources as any)?.[t] ?? 0} />
              ))}
            </div>
          </div>

          {/* Ability card + Progress stack — same row, xlarge */}
          {(player?.ability_card || (player?.progress_area ?? []).length > 0) && (
            <div style={{ display: 'flex', gap: 10, alignItems: 'flex-start' }}>

              {/* Ability card */}
              {player?.ability_card && (
                <div>
                  <div style={{ fontSize: 9, color: '#555', marginBottom: 5, letterSpacing: '.08em', textTransform: 'uppercase' }}>Способность</div>
                  <CardView card={player.ability_card} size="xlarge" />
                </div>
              )}

              {/* Progress stack — only top card visible */}
              {(player?.progress_area ?? []).length > 0 && (() => {
                const progressCards = player!.progress_area;
                const topCard = progressCards[0];
                const count = progressCards.length;
                const isPreview = previewCard?.id === topCard.id;
                return (
                  <div>
                    <div style={{ fontSize: 9, color: '#555', marginBottom: 5, letterSpacing: '.08em', textTransform: 'uppercase' }}>
                      Прогресс <span style={{ color: '#9098b8' }}>({count})</span>
                    </div>
                    <div
                      ref={isPreview ? previewRef : undefined}
                      onClickCapture={(e) => {
                        if (!isPreview) {
                          const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
                          const leftColRight = (window.innerWidth - 260) / 2;
                          setPreviewOrigin(rect.left + rect.width * 2 > leftColRight ? 'top right' : 'top left');
                        }
                      }}
                      style={{
                        position: 'relative', display: 'inline-block',
                        zIndex: isPreview ? 200 : undefined,
                        transform: isPreview ? 'scale(1.5)' : undefined,
                        transformOrigin: isPreview ? previewOrigin : 'top left',
                        transition: 'transform 0.15s',
                      }}
                    >
                      {count > 2 && (
                        <div style={{ position: 'absolute', top: 4, left: 4, width: 180, height: 240, background: '#0d0f1a', border: '1px solid #1a1d30', borderRadius: 8, pointerEvents: 'none' }} />
                      )}
                      {count > 1 && (
                        <div style={{ position: 'absolute', top: 2, left: 2, width: 180, height: 240, background: '#10121e', border: '1px solid #1e2138', borderRadius: 8, pointerEvents: 'none' }} />
                      )}
                      <CardView
                        card={topCard}
                        size="xlarge"
                        onClick={() => {
                          if (isPreview) {
                            setPreviewCard(null);
                            if (isPlayerTurn) accelerateProgress(topCard.id);
                          } else {
                            setPreviewCard(topCard);
                          }
                        }}
                      />
                    </div>
                  </div>
                );
              })()}

            </div>
          )}

          {/* Play area */}
          {(player?.play_area ?? []).length > 0 && (
            <div>
              <div style={{ fontSize: 9, color: '#555', marginBottom: 5, letterSpacing: '.08em', textTransform: 'uppercase' }}>Разыгранные</div>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                {(player?.play_area ?? []).map(c => {
                  const isPreview = previewCard?.id === c.id;
                  return (
                    <div key={c.id} ref={isPreview ? previewRef : undefined}
                      style={{ flexShrink: 0, position: 'relative', zIndex: isPreview ? 200 : undefined, transform: isPreview ? 'scale(2)' : undefined, transformOrigin: 'top left', transition: 'transform 0.15s' }}>
                      <CardView card={c} size="small"
                        selected={selectedCards.includes(c.id)}
                        onClick={() => { setPreviewCard(isPreview ? null : c); if (mode === 'activation') toggleCard(c.id); }} />
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Deck counts */}
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <DeckPile count={player?.deck_count ?? 0} label="Личная колода" color="#1a2040" />
            <DeckPile count={player?.discard_count ?? 0} label="Сброс" color="#2d1a00" />
            <DeckPile count={player?.boost_deck_count ?? 0} label="Усиление" color="#3d2000" />
            <DeckPile count={player?.chronicle_count ?? 0} label="Летопись" color="#1a2a1a" />
          </div>

          {/* Actions */}
          {isPlayerTurn && (
            <div style={{ background: '#0d1020', border: '1px solid #1e2235', borderRadius: 9, padding: '10px 12px' }}>
              <div style={{ fontSize: 9, color: '#555', marginBottom: 8, letterSpacing: '.08em', textTransform: 'uppercase' }}>Действия</div>
              {!mode ? (
                <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
                  <Btn label="Активация" onClick={() => { setMode('activation'); setSelectedCards([]); }} color="#2ecc71" icon="▶" />
                  <Btn label="Инновация" onClick={() => setMode('innovation')} color="#3498db" icon="💡" />
                  <Btn label="Революция" onClick={() => setMode('revolution')} color="#9b59b6" icon="🔄" />
                  <Btn label="Закончить ход" onClick={handleEndTurn} color="#e67e22" icon="⏭" />
                  <Btn label="Отменить" onClick={() => { undoAction(); setMode(null); setSelectedCards([]); }} color="#e74c3c" icon="↩" />
                </div>
              ) : mode === 'activation' ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ fontSize: 10, color: '#2ecc71' }}>{selectedCards.length > 0 ? `Выбрано: ${selectedCards.length}` : 'Выберите карты с руки'}</div>
                  <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
                    <Btn label="Разыграть" onClick={handlePlay} color="#2ecc71" disabled={selectedCards.length === 0} />
                    <Btn label="Эксплуатировать" onClick={handleExploit} color="#e67e22" disabled={selectedCards.length !== 1} />
                    <Btn label={acquireMode ? "Отмена покупки" : "Купить с рынка"} onClick={() => setAcquireMode(!acquireMode)} color="#3498db" active={acquireMode} />
                    <Btn label="Завершить" onClick={handleEndTurn} color="#888" />
                    <Btn label="Отменить" onClick={() => { undoAction(); setMode(null); setSelectedCards([]); }} color="#e74c3c" icon="↩" />
                  </div>
                </div>
              ) : mode === 'innovation' ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ fontSize: 10, color: '#3498db' }}>Выберите категорию:</div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {['region', 'origins', 'civilization', 'raid'].map(cat => (
                      <button key={cat} onClick={() => setInnovationCat(cat)} style={{ background: innovationCat === cat ? CAT_COLOR[cat] : `${CAT_COLOR[cat]}20`, border: `1px solid ${CAT_COLOR[cat]}`, borderRadius: 5, color: innovationCat === cat ? '#000' : CAT_COLOR[cat], padding: '5px 10px', cursor: 'pointer', fontSize: 10, fontWeight: 600 }}>{CAT_RU[cat]}</button>
                    ))}
                  </div>
                  <div style={{ display: 'flex', gap: 7 }}>
                    <Btn label="Подтвердить" onClick={handleInnovation} color="#3498db" disabled={!innovationCat} />
                    <Btn label="Отмена" onClick={() => { setMode(null); setInnovationCat(null); }} color="#888" />
                  </div>
                </div>
              ) : mode === 'revolution' ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ fontSize: 10, color: '#9b59b6' }}>Выберите карты беспорядков ({selectedCards.length} выбрано)</div>
                  <div style={{ display: 'flex', gap: 7 }}>
                    <Btn label={`Вернуть (${selectedCards.length})`} onClick={handleRevolution} color="#9b59b6" disabled={selectedCards.length === 0} />
                    <Btn label="Отмена" onClick={() => { setMode(null); setSelectedCards([]); }} color="#888" />
                  </div>
                </div>
              ) : null}
            </div>
          )}

          {isDiscardPhase && (
            <div style={{ background: '#0d1020', border: '1px solid #9b59b6', borderRadius: 9, padding: '10px 12px' }}>
              <div style={{ fontSize: 9, color: '#9b59b6', marginBottom: 8, letterSpacing: '.08em', textTransform: 'uppercase' }}>Сброс карт</div>
              <div style={{ fontSize: 10, color: '#aaa', marginBottom: 8 }}>
                {selectedCards.length > 0 ? `Выбрано для сброса: ${selectedCards.length}` : 'Выберите карты для сброса или пропустите'}
              </div>
              <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
                <Btn label={selectedCards.length > 0 ? `Сбросить (${selectedCards.length})` : 'Пропустить'} onClick={handleConfirmDiscard} color="#9b59b6" icon="🗑" />
              </div>
            </div>
          )}

          {/* Hand */}
          <div>
            <div style={{ fontSize: 9, color: '#555', letterSpacing: '.1em', textTransform: 'uppercase', marginBottom: 6 }}>
              Рука ({(player?.hand ?? []).length}/{player?.hand_limit ?? 5})
            </div>
            <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
              {(player?.hand ?? []).map(card => {
                const isDisorder = card.categories?.includes('disorder');
                const canSelect = isDiscardPhase || (isPlayerTurn && (mode === 'activation' || (mode === 'revolution' && isDisorder)));
                const isPreview = previewCard?.id === card.id;
                return (
                  <div key={card.id} ref={isPreview ? previewRef : undefined}
                    onClickCapture={(e) => {
                      if (!isPreview) {
                        const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
                        const leftColRight = (window.innerWidth - 260) / 2;
                        setPreviewOrigin(rect.left + rect.width * 2 > leftColRight ? 'top right' : 'top left');
                      }
                    }}
                    style={{ flexShrink: 0, position: 'relative', zIndex: isPreview ? 200 : undefined, transform: isPreview ? 'scale(1.5)' : undefined, transformOrigin: isPreview ? previewOrigin : 'top left', transition: 'transform 0.15s' }}>
                    <CardView card={card} size="xlarge"
                      selected={selectedCards.includes(card.id)}
                      onClick={() => { setPreviewCard(isPreview ? null : card); if (canSelect) toggleCard(card.id); }}
                      dimmed={isPlayerTurn && mode === 'revolution' && !isDisorder} />
                  </div>
                );
              })}
              {(player?.hand ?? []).length === 0 && (
                <div style={{ fontSize: 11, color: '#2a2d40' }}>Рука пуста</div>
              )}
            </div>
          </div>

          {loading && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 11, color: '#3498db' }}>
              <div style={{ width: 12, height: 12, border: '2px solid #3498db44', borderTop: '2px solid #3498db', borderRadius: '50%', animation: 'spin .8s linear infinite' }} />
              Обработка...
            </div>
          )}
        </div>

        {/* CENTER — Market + Log */}
        <div style={{ display: 'flex', flexDirection: 'column', borderRight: '1px solid #1e2235', overflow: 'hidden' }}>
          {/* Deck summary */}
          <div style={{ padding: '8px 10px', background: '#080a10', borderBottom: '1px solid #1e2235', display: 'flex', gap: 8, justifyContent: 'center', flexWrap: 'wrap' }}>
            <DeckPile count={shared?.main_deck_count ?? 0} label="Основная" color="#2a1a40" />
            <DeckPile count={shared?.disorder_deck_count ?? 0} label="Беспорядки" color="#2d0a2d" />
            <DeckPile count={shared?.glory_deck_count ?? 0} label="Слава" color="#2d2600" />
          </div>

          {/* Market */}
          <div style={{ flex: 1, overflow: 'auto', padding: '10px 8px', background: '#08090e' }}>
            <div style={{ fontSize: 9, color: '#555', marginBottom: 10, textAlign: 'center', letterSpacing: '.1em', textTransform: 'uppercase' }}>☉ Текущий рынок</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, alignItems: 'center' }}>
              {(shared?.market ?? []).map((slot, idx) => (
                <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                  <div style={{ fontSize: 9, color: '#444', background: '#12141f', border: '1px solid #1e2235', borderRadius: 3, padding: '1px 6px' }}>Маркер {slot.market_marker}</div>
                  {slot.card ? (
                    <>
                      <div ref={previewCard?.id === slot.card.id ? previewRef : undefined}
                        style={{ flexShrink: 0, position: 'relative', zIndex: previewCard?.id === slot.card.id ? 200 : undefined, transform: previewCard?.id === slot.card.id ? 'scale(2)' : undefined, transformOrigin: 'top center', transition: 'transform 0.15s' }}>
                        <CardView card={slot.card} size="large"
                          onClick={() => { setPreviewCard(previewCard?.id === slot.card!.id ? null : slot.card); if (acquireMode && isPlayerTurn) acquireCard(idx); }}
                          badge={slot.upgrade_tokens > 0 ? <div style={{ background: '#3498db', color: '#fff', borderRadius: 3, padding: '1px 4px', fontSize: 8, fontWeight: 700 }}>+{slot.upgrade_tokens}▶</div> : undefined} />
                      </div>
                      {slot.has_disorder_under && <div style={{ fontSize: 8, color: '#9b59b6' }}>⚠ Беспорядки</div>}
                      {acquireMode && isPlayerTurn && (
                        <button onClick={() => acquireCard(idx)} style={{ background: 'linear-gradient(135deg,#1abc9c,#16a085)', border: 'none', borderRadius: 5, color: '#fff', fontSize: 9, padding: '3px 8px', cursor: 'pointer', fontWeight: 600 }}>Приобрести</button>
                      )}
                    </>
                  ) : (
                    <div style={{ width: 112, height: 148, border: '1px dashed #1e2235', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 11, color: '#2a2d40' }}>Пусто</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Log */}
          <div style={{ padding: '8px 10px', background: '#06070c', maxHeight: 180, overflow: 'auto', flexShrink: 0 }}>
            <div style={{ fontSize: 9, color: '#333', marginBottom: 5, letterSpacing: '.08em', textTransform: 'uppercase' }}>Журнал</div>
            {[...(gs.log ?? [])].reverse().map((entry, i) => (
              <div key={i} style={{ fontSize: 9, color: i === 0 ? '#9098b8' : '#383c50', lineHeight: 1.5, borderLeft: `2px solid ${i === 0 ? '#2a2d50' : 'transparent'}`, paddingLeft: 5, marginBottom: 2 }}>{entry}</div>
            ))}
          </div>
        </div>

        {/* RIGHT — Bot */}
        <div style={{ padding: '14px 12px', display: 'flex', flexDirection: 'column', gap: 12, overflow: 'auto' }}>
          <div style={{ background: '#100d20', border: '1px solid #1e1e35', borderRadius: 8, padding: '8px 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: 13, fontWeight: 700, color: '#e74c3c', letterSpacing: '.1em' }}>🤖 {bot?.nation?.toUpperCase()}</div>
              <div style={{ fontSize: 10, color: bot?.period === 'barbarism' ? '#e74c3c' : '#3498db', marginTop: 2 }}>{PERIOD_RU[bot?.period ?? '']}</div>
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              <Token type="resource" value={bot?.resource ?? 0} />
              <Token type="population" value={bot?.population ?? 0} />
              <Token type="upgrade" value={bot?.upgrade ?? 0} />
            </div>
          </div>

          <div>
            <div style={{ fontSize: 9, color: '#555', marginBottom: 6, letterSpacing: '.08em', textTransform: 'uppercase' }}>Карты ниже маркеров</div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              {(bot?.hand_slots ?? []).map((slot, idx) => (
                <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
                  <div style={{ width: 75, height: 95, background: slot ? '#14090d' : '#090910', border: `1px solid ${slot ? '#2d1a1a' : '#1a1a2e'}`, borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', color: slot ? '#4a1a1a' : '#1a1a2e', fontSize: slot ? 22 : 18 }}>
                    {slot ? '🃏' : '—'}
                  </div>
                  <div style={{ fontSize: 8, color: '#333' }}>#{idx + 1}</div>
                </div>
              ))}
            </div>
          </div>

          {(bot?.play_area_count ?? 0) > 0 && (
            <div style={{ background: '#0d0a18', border: '1px solid #1e1a2e', borderRadius: 6, padding: '8px 12px', fontSize: 11, color: '#444' }}>
              🃏 В игровой области: {bot?.play_area_count} карт
            </div>
          )}

          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <DeckPile count={bot?.bot_deck_count ?? 0} label="Колода бота" color="#2d0a1a" />
            <DeckPile count={bot?.dynasty_deck_count ?? 0} label="Династия" color="#1a0a2d" />
            <DeckPile count={bot?.chronicle_count ?? 0} label="Летопись" color="#1a2a1a" />
          </div>
        </div>
      </div>

      <style>{`@keyframes spin{to{transform:rotate(360deg)}}*{box-sizing:border-box}::-webkit-scrollbar{width:4px;height:4px}::-webkit-scrollbar-thumb{background:#2a2d40;border-radius:2px}`}</style>
    </div>
  );
}
