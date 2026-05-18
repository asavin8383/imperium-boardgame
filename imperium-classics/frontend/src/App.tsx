import React from 'react';
import { useGameStore } from './store/gameStore';
import GameSetup from './components/GameSetup';
import GameBoard from './components/GameBoard';

export default function App() {
  const { gameId } = useGameStore();
  return gameId ? <GameBoard /> : <GameSetup />;
}
