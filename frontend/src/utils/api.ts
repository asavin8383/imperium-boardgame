import axios from 'axios';
import { GameState, NationInfo } from '../types/game';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8000';

const api = axios.create({ baseURL: API_BASE });

export async function fetchNations(): Promise<Record<string, NationInfo>> {
  const res = await api.get('/api/nations');
  return res.data.nations;
}

export async function fetchDifficulties() {
  const res = await api.get('/api/difficulties');
  return res.data.difficulties;
}

export async function createGame(
  playerNation: string,
  botNation: string,
  difficulty: string,
  abilitySide: string = 'B'
): Promise<{ game_id: string; state: GameState }> {
  const res = await api.post('/api/games', {
    player_nation: playerNation,
    bot_nation: botNation,
    difficulty,
    ability_side: abilitySide,
  });
  return res.data;
}

export async function getGame(gameId: string): Promise<GameState> {
  const res = await api.get(`/api/games/${gameId}`);
  return res.data.state;
}

export async function playCard(gameId: string, cardId: string): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/play-card`, { card_id: cardId });
  return res.data.state;
}

export async function exploitCard(gameId: string, cardId: string): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/exploit-card`, { card_id: cardId });
  return res.data.state;
}

export async function doInnovation(gameId: string, category: string): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/innovation`, { category });
  return res.data.state;
}

export async function doRevolution(gameId: string, cardIds: string[]): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/revolution`, { card_ids: cardIds });
  return res.data.state;
}

export async function endTurn(gameId: string, discardIds: string[] = []): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/end-turn`, { discard_ids: discardIds });
  return res.data.state;
}

export async function acquireCard(gameId: string, slotIndex: number): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/acquire-card`, { slot_index: slotIndex });
  return res.data.state;
}

export async function accelerateProgress(gameId: string, progressCardId: string): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/accelerate-progress`, {
    progress_card_id: progressCardId,
  });
  return res.data.state;
}

export async function undoAction(gameId: string): Promise<GameState> {
  const res = await api.post(`/api/games/${gameId}/undo`);
  return res.data.state;
}

export async function deleteGame(gameId: string): Promise<void> {
  await api.delete(`/api/games/${gameId}`);
}
