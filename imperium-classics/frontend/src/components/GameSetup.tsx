import React, { useState, useEffect } from 'react';
import { fetchNations, fetchDifficulties } from '../utils/api';
import { useGameStore } from '../store/gameStore';
import { NationInfo } from '../types/game';

const NATION_COLORS: Record<string, string> = {
  vikings: '#E8A838',
  greeks: '#4A90D9',
  carthaginians: '#9B59B6',
  celts: '#27AE60',
  macedonians: '#8E44AD',
  persians: '#E67E22',
  romans: '#C0392B',
  scythians: '#16A085',
};

export default function GameSetup() {
  const [nations, setNations] = useState<Record<string, NationInfo>>({});
  const [difficulties, setDifficulties] = useState<any[]>([]);
  const [playerNation, setPlayerNation] = useState('romans');
  const [botNation, setBotNation] = useState('greeks');
  const [difficulty, setDifficulty] = useState('emperor');
  const { createGame, loading, error } = useGameStore();

  useEffect(() => {
    fetchNations().then(setNations);
    fetchDifficulties().then(setDifficulties);
  }, []);

  const handleStart = () => {
    if (playerNation === botNation) {
      alert('Игрок и бот не могут играть одним народом!');
      return;
    }
    createGame(playerNation, botNation, difficulty);
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>ИМПЕРИИ</h1>
        <p style={styles.subtitle}>Одиночный режим — Классика</p>
      </div>

      <div style={styles.content}>
        {/* Player Nation */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Ваш народ</h2>
          <div style={styles.nationGrid}>
            {Object.entries(nations).map(([id, info]) => (
              <button
                key={id}
                onClick={() => setPlayerNation(id)}
                style={{
                  ...styles.nationCard,
                  borderColor: playerNation === id ? NATION_COLORS[id] : '#333',
                  background: playerNation === id
                    ? `${NATION_COLORS[id]}22`
                    : '#1a1a2e',
                  boxShadow: playerNation === id
                    ? `0 0 12px ${NATION_COLORS[id]}66`
                    : 'none',
                }}
              >
                <div style={{ ...styles.nationDot, background: NATION_COLORS[id] }} />
                <div style={styles.nationName}>{info.name}</div>
                <div style={styles.nationComplexity}>{info.complexity}</div>
                <div style={styles.nationDesc}>{info.description}</div>
              </button>
            ))}
          </div>
        </section>

        {/* Bot Nation */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Народ бота</h2>
          <div style={styles.nationGrid}>
            {Object.entries(nations).map(([id, info]) => (
              <button
                key={id}
                onClick={() => setBotNation(id)}
                disabled={id === playerNation}
                style={{
                  ...styles.nationCard,
                  borderColor: botNation === id ? '#888' : '#333',
                  background: botNation === id ? '#2a2a3e' : '#1a1a2e',
                  opacity: id === playerNation ? 0.3 : 1,
                  cursor: id === playerNation ? 'not-allowed' : 'pointer',
                }}
              >
                <div style={{ ...styles.nationDot, background: NATION_COLORS[id] }} />
                <div style={styles.nationName}>{info.name}</div>
                <div style={styles.nationComplexity}>{info.complexity}</div>
              </button>
            ))}
          </div>
        </section>

        {/* Difficulty */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Уровень сложности</h2>
          <div style={styles.diffRow}>
            {difficulties.map((d) => (
              <button
                key={d.id}
                onClick={() => setDifficulty(d.id)}
                style={{
                  ...styles.diffBtn,
                  borderColor: difficulty === d.id ? '#c9a84c' : '#444',
                  background: difficulty === d.id ? '#c9a84c22' : '#1a1a2e',
                  color: difficulty === d.id ? '#c9a84c' : '#888',
                }}
              >
                <div style={{ fontWeight: 700, fontSize: 13 }}>{d.name}</div>
                <div style={{ fontSize: 11, marginTop: 3 }}>{d.description}</div>
              </button>
            ))}
          </div>
        </section>

        {error && <div style={styles.error}>{error}</div>}

        <button
          onClick={handleStart}
          disabled={loading}
          style={styles.startBtn}
        >
          {loading ? 'Подготовка...' : '⚔ Начать игру'}
        </button>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #0d0d1a 0%, #1a1a2e 50%, #0d0d1a 100%)',
    color: '#e0d5c0',
    fontFamily: "'Georgia', serif",
  },
  header: {
    textAlign: 'center',
    padding: '40px 20px 20px',
    borderBottom: '1px solid #333',
  },
  title: {
    fontSize: 52,
    fontWeight: 900,
    letterSpacing: 12,
    color: '#c9a84c',
    margin: 0,
    textShadow: '0 0 30px #c9a84c44',
  },
  subtitle: {
    color: '#888',
    fontSize: 16,
    margin: '8px 0 0',
  },
  content: {
    maxWidth: 900,
    margin: '0 auto',
    padding: 24,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    fontSize: 18,
    color: '#c9a84c',
    borderBottom: '1px solid #333',
    paddingBottom: 8,
    marginBottom: 16,
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
  nationGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
    gap: 12,
  },
  nationCard: {
    padding: 12,
    border: '2px solid #333',
    borderRadius: 8,
    cursor: 'pointer',
    textAlign: 'left',
    transition: 'all 0.2s',
    background: '#1a1a2e',
    color: '#e0d5c0',
  },
  nationDot: {
    width: 12,
    height: 12,
    borderRadius: '50%',
    marginBottom: 8,
  },
  nationName: {
    fontWeight: 700,
    fontSize: 15,
    marginBottom: 4,
  },
  nationComplexity: {
    fontSize: 12,
    color: '#c9a84c',
    marginBottom: 6,
  },
  nationDesc: {
    fontSize: 11,
    color: '#888',
    lineHeight: 1.4,
  },
  diffRow: {
    display: 'flex',
    gap: 10,
    flexWrap: 'wrap',
  },
  diffBtn: {
    padding: '10px 16px',
    border: '2px solid #444',
    borderRadius: 8,
    cursor: 'pointer',
    textAlign: 'left',
    flex: 1,
    minWidth: 140,
    transition: 'all 0.2s',
  },
  startBtn: {
    display: 'block',
    width: '100%',
    padding: 16,
    background: 'linear-gradient(135deg, #c9a84c, #a07830)',
    color: '#0d0d1a',
    border: 'none',
    borderRadius: 10,
    fontSize: 20,
    fontWeight: 700,
    cursor: 'pointer',
    letterSpacing: 2,
    marginTop: 16,
    transition: 'opacity 0.2s',
  },
  error: {
    background: '#8b000022',
    border: '1px solid #c0392b',
    borderRadius: 8,
    padding: 12,
    color: '#e74c3c',
    marginBottom: 16,
  },
};
