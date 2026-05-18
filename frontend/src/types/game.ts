// Imperium game types matching backend models

export type Period = 'barbarism' | 'civilization';
export type GamePhase = 'setup' | 'player_turn' | 'bot_turn' | 'solstice' | 'final_round' | 'scoring' | 'game_over';
export type TurnAction = 'activation' | 'innovation' | 'revolution';
export type Nation = 'vikings' | 'greeks' | 'carthaginians' | 'celts' | 'macedonians' | 'persians' | 'romans' | 'scythians';
export type Difficulty = 'chieftain' | 'commander' | 'emperor' | 'overlord' | 'sovereign';
export type CardCategory = 'ability' | 'region' | 'origins' | 'civilization' | 'raid' | 'glory' | 'disorder' | 'transformation' | 'boost' | 'progress' | 'reserve';

export interface CardInfo {
  id: string;
  name: string;
  categories?: CardCategory[];
  period?: Period | null;
  vp_fixed?: number;
  vp_condition?: string | null;
  vp_per_condition?: string | null;
  vp_penalty?: number;
  requires_action?: boolean;
  is_exploit?: boolean;
  passive_effect?: boolean;
  solstice_effect?: boolean;
  card_type?: 'normal' | 'attack' | 'permanent';
  region_types?: string[];
  progress_cost_resource?: number;
  progress_cost_population?: number;
  progress_cost_upgrade?: number;
}

export interface Resources {
  resource: number;
  population: number;
  upgrade: number;
  action: number;
  exploit: number;
}

export interface MarketSlot {
  card: CardInfo | null;
  upgrade_tokens: number;
  has_disorder_under: boolean;
  market_marker: number;
}

export interface PlayerState {
  nation: Nation;
  period: Period;
  deck_count: number;
  hand: CardInfo[];
  discard_count: number;
  play_area: CardInfo[];
  chronicle_count: number;
  progress_area: CardInfo[];
  boost_deck_count: number;
  boost_top_token: boolean;
  ability_card: { id: string; name: string; side: string } | null;
  resources: Resources;
  hand_limit: number;
}

export interface BotSlot {
  id: string;
  name: string;
}

export interface BotState {
  nation: Nation;
  period: Period;
  bot_deck_count: number;
  dynasty_deck_count: number;
  chronicle_count: number;
  play_area_count: number;
  hand_slots: (BotSlot | null)[];
  resource: number;
  population: number;
  upgrade: number;
}

export interface SharedState {
  region_deck_count: number;
  origins_deck_count: number;
  civilization_deck_count: number;
  main_deck_count: number;
  disorder_deck_count: number;
  glory_deck_count: number;
  exile_pile_count: number;
  king_of_kings_side_b: boolean;
  market: MarketSlot[];
}

export interface GameState {
  game_id: string;
  phase: GamePhase;
  round_number: number;
  is_final_round: boolean;
  end_condition: string | null;
  player: PlayerState | null;
  bot: BotState | null;
  shared: SharedState;
  difficulty: Difficulty;
  log: string[];
  pending_choice: Record<string, unknown> | null;
}

export interface NationInfo {
  name: string;
  difficulty: number;
  description: string;
  complexity: string;
}
