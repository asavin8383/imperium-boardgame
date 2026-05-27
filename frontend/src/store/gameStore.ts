import { create } from 'zustand';
import { GameState } from '../types/game';
import * as api from '../utils/api';

interface GameStore {
  gameId: string | null;
  gameState: GameState | null;
  loading: boolean;
  error: string | null;
  selectedCards: string[];

  setGame: (id: string, state: GameState) => void;
  updateState: (state: GameState) => void;
  setLoading: (v: boolean) => void;
  setError: (e: string | null) => void;
  toggleSelectCard: (cardId: string) => void;
  clearSelection: () => void;

  // Actions
  createGame: (playerNation: string, botNation: string, difficulty: string) => Promise<void>;
  undoAction: () => Promise<void>;
  playCard: (cardId: string) => Promise<void>;
  exploitCard: (cardId: string) => Promise<void>;
  doInnovation: () => Promise<void>;
  doRevolution: (cardIds?: string[]) => Promise<void>;
  endTurn: (discardIds?: string[]) => Promise<void>;
  acquireCard: (slotIndex: number) => Promise<void>;
  accelerateProgress: (progressCardId: string) => Promise<void>;
  makeChoice: (optionIndex: number) => Promise<void>;
  selectAppropriateCategory: (category: string) => Promise<void>;
  appropriateFromDeck: (deckName: string) => Promise<void>;
  returnExploitToken: (cardId: string | null) => Promise<void>;
  resolveDrawFromDeck: (draw: boolean) => Promise<void>;
  reinforceChoice: (reinforce: boolean) => Promise<void>;
  reinforceWithCard: (handCardId: string) => Promise<void>;
  chronicleChoice: (sendToChronicle: boolean) => Promise<void>;
  playFromDiscard: (cardId: string) => Promise<void>;
  placeUpgradeToken: (slotIndex: number) => Promise<void>;
  recallToAvoidAttack: (recall: boolean) => Promise<void>;
  chronicleFromDiscard: (cardId: string | null) => Promise<void>;
  exileFromMarket: (slotIndex: number) => Promise<void>;
  destroyCards: (cardIds: string[]) => Promise<void>;
  gloryDeckTake: (cardId: string) => Promise<void>;
  moveDiscardToDeck: (cardId: string | null) => Promise<void>;
  sacredPathExploit: (destroy: boolean) => Promise<void>;
  sacredPathExchange: (handCardId: string) => Promise<void>;
  resetGame: () => void;
}

export const useGameStore = create<GameStore>((set, get) => ({
  gameId: null,
  gameState: null,
  loading: false,
  error: null,
  selectedCards: [],

  setGame: (id, state) => set({ gameId: id, gameState: state }),
  updateState: (state) => set({ gameState: state }),
  setLoading: (v) => set({ loading: v }),
  setError: (e) => set({ error: e }),
  toggleSelectCard: (cardId) => set((s) => ({
    selectedCards: s.selectedCards.includes(cardId)
      ? s.selectedCards.filter(id => id !== cardId)
      : [...s.selectedCards, cardId]
  })),
  clearSelection: () => set({ selectedCards: [] }),
  resetGame: () => set({ gameId: null, gameState: null, selectedCards: [], error: null }),

  createGame: async (playerNation, botNation, difficulty) => {
    set({ loading: true, error: null });
    try {
      const { game_id, state } = await api.createGame(playerNation, botNation, difficulty);
      set({ gameId: game_id, gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  undoAction: async () => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.undoAction(gameId);
      set({ gameState: state, loading: false, selectedCards: [] });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  playCard: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.playCard(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  exploitCard: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.exploitCard(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  doInnovation: async () => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.doInnovation(gameId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  doRevolution: async (cardIds?: string[]) => {
    const { gameId, selectedCards } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.doRevolution(gameId, cardIds ?? selectedCards);
      set({ gameState: state, loading: false, selectedCards: [] });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  endTurn: async (discardIds = []) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.endTurn(gameId, discardIds);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  acquireCard: async (slotIndex) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.acquireCard(gameId, slotIndex);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  accelerateProgress: async (progressCardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.accelerateProgress(gameId, progressCardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  makeChoice: async (optionIndex) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.makeChoice(gameId, optionIndex);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  selectAppropriateCategory: async (category) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.selectAppropriateCategory(gameId, category);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  appropriateFromDeck: async (deckName) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.appropriateFromDeck(gameId, deckName);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  returnExploitToken: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.returnExploitToken(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  resolveDrawFromDeck: async (draw) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.resolveDrawFromDeck(gameId, draw);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  reinforceChoice: async (reinforce) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.reinforceChoice(gameId, reinforce);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  reinforceWithCard: async (handCardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.reinforceWithCard(gameId, handCardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  chronicleChoice: async (sendToChronicle) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.chronicleChoice(gameId, sendToChronicle);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  playFromDiscard: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.playFromDiscard(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  placeUpgradeToken: async (slotIndex) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.placeUpgradeToken(gameId, slotIndex);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  recallToAvoidAttack: async (recall) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.recallToAvoidAttack(gameId, recall);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  chronicleFromDiscard: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.chronicleFromDiscard(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  exileFromMarket: async (slotIndex) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.exileFromMarket(gameId, slotIndex);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  destroyCards: async (cardIds) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.destroyCards(gameId, cardIds);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  gloryDeckTake: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.gloryDeckTake(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  moveDiscardToDeck: async (cardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.moveDiscardToDeck(gameId, cardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  sacredPathExploit: async (destroy) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.sacredPathExploit(gameId, destroy);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },

  sacredPathExchange: async (handCardId) => {
    const { gameId } = get();
    if (!gameId) return;
    set({ loading: true, error: null });
    try {
      const state = await api.sacredPathExchange(gameId, handCardId);
      set({ gameState: state, loading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.detail || e.message, loading: false });
    }
  },
}));
