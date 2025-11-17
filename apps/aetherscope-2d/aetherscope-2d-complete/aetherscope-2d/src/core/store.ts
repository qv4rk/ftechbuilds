import { create } from 'zustand';

// ===== COLOR PALETTES =====
export const COLOR_PALETTES = {
  neon: ['#00FFFF', '#FF00FF', '#FFFF00', '#00FF00', '#FF0000'],
  pastel: ['#FFB3BA', '#FFDFBA', '#FFFFBA', '#BAFFC9', '#BAE1FF'],
  fire: ['#FF0000', '#FF4500', '#FF8C00', '#FFD700', '#FFA500'],
  ocean: ['#000080', '#0000CD', '#1E90FF', '#00CED1', '#20B2AA'],
  monochrome: ['#FFFFFF', '#CCCCCC', '#999999', '#666666', '#333333'],
  sunset: ['#FF6B6B', '#FFA07A', '#FFD93D', '#6BCF7F', '#4ECDC4'],
  cosmic: ['#9D4EDD', '#C77DFF', '#E0AAFF', '#FF69B4', '#00CED1'],
} as const;

export type PaletteName = keyof typeof COLOR_PALETTES;

// ===== APP STATE INTERFACE =====
export interface AppState {
  // Pattern & Rendering
  patternIndex: number;
  segments: number; // Mirror count (kaleidoscope symmetry)
  thickness: number; // Line thickness
  stampSize: number; // Pattern size
  zoom: number;

  // Colors
  paletteIndex: number;
  palettes: typeof COLOR_PALETTES;
  currentPaletteName: PaletteName;
  colorIndex: number; // 0 = cycle through palette, >0 = use specific color

  // Animation & Interaction
  autoplay: boolean;
  speed: number; // Animation speed
  isDrawing: boolean;

  // Advanced Parameters (for complex patterns)
  param1: number; // Generic parameter 1
  param2: number; // Generic parameter 2

  // Canvas state
  backgroundColor: string;

  // UI state
  panelVisible: boolean;

  // Mouse/Touch tracking
  lastX: number;
  lastY: number;

  // ===== STATE MUTATION FUNCTIONS =====

  // Pattern controls
  setPatternIndex: (index: number) => void;
  nextPattern: () => void;
  prevPattern: () => void;

  // Mirror & geometry controls
  setSegments: (segments: number) => void;
  setThickness: (thickness: number) => void;
  setStampSize: (size: number) => void;
  setZoom: (zoom: number) => void;

  // Color controls
  setPaletteIndex: (index: number) => void;
  setPaletteName: (name: PaletteName) => void;
  setColorIndex: (index: number) => void;
  nextPalette: () => void;
  prevPalette: () => void;

  // Animation controls
  toggleAutoplay: () => void;
  setAutoplay: (autoplay: boolean) => void;
  setSpeed: (speed: number) => void;

  // Drawing state
  setIsDrawing: (isDrawing: boolean) => void;
  setLastPosition: (x: number, y: number) => void;

  // Advanced parameters
  setParam1: (value: number) => void;
  setParam2: (value: number) => void;

  // Canvas controls
  setBackgroundColor: (color: string) => void;

  // UI controls
  togglePanel: () => void;
  setPanelVisible: (visible: boolean) => void;

  // Utility functions
  reset: () => void;
  getCurrentPalette: () => string[];
  getCurrentColor: () => string;
}

// ===== INITIAL STATE =====
const INITIAL_STATE = {
  patternIndex: 0,
  segments: 9,
  thickness: 3,
  stampSize: 50,
  zoom: 1.0,

  paletteIndex: 0,
  palettes: COLOR_PALETTES,
  currentPaletteName: 'neon' as PaletteName,
  colorIndex: 0,

  autoplay: false,
  speed: 50,
  isDrawing: false,

  param1: 1.0,
  param2: 1.0,

  backgroundColor: '#000000',

  panelVisible: true,

  lastX: 0,
  lastY: 0,
};

// ===== ZUSTAND STORE =====
export const useStore = create<AppState>((set, get) => ({
  ...INITIAL_STATE,

  // Pattern controls
  setPatternIndex: (index: number) => 
    set({ patternIndex: index }),

  nextPattern: () => 
    set((state) => ({ 
      patternIndex: (state.patternIndex + 1) % 12 // Assuming 12 patterns
    })),

  prevPattern: () => 
    set((state) => ({ 
      patternIndex: state.patternIndex === 0 ? 11 : state.patternIndex - 1 
    })),

  // Mirror & geometry controls
  setSegments: (segments: number) => 
    set({ segments: Math.max(3, Math.min(24, segments)) }),

  setThickness: (thickness: number) => 
    set({ thickness: Math.max(0.5, Math.min(20, thickness)) }),

  setStampSize: (size: number) => 
    set({ stampSize: Math.max(10, Math.min(200, size)) }),

  setZoom: (zoom: number) => 
    set({ zoom: Math.max(0.5, Math.min(3, zoom)) }),

  // Color controls
  setPaletteIndex: (index: number) => {
    const paletteNames = Object.keys(COLOR_PALETTES) as PaletteName[];
    const clampedIndex = Math.max(0, Math.min(paletteNames.length - 1, index));
    set({ 
      paletteIndex: clampedIndex,
      currentPaletteName: paletteNames[clampedIndex]
    });
  },

  setPaletteName: (name: PaletteName) => {
    const paletteNames = Object.keys(COLOR_PALETTES) as PaletteName[];
    const index = paletteNames.indexOf(name);
    set({ 
      paletteIndex: index,
      currentPaletteName: name
    });
  },

  setColorIndex: (index: number) => 
    set({ colorIndex: Math.max(0, Math.min(5, index)) }),

  nextPalette: () => {
    const paletteNames = Object.keys(COLOR_PALETTES) as PaletteName[];
    set((state) => {
      const nextIndex = (state.paletteIndex + 1) % paletteNames.length;
      return {
        paletteIndex: nextIndex,
        currentPaletteName: paletteNames[nextIndex]
      };
    });
  },

  prevPalette: () => {
    const paletteNames = Object.keys(COLOR_PALETTES) as PaletteName[];
    set((state) => {
      const prevIndex = state.paletteIndex === 0 
        ? paletteNames.length - 1 
        : state.paletteIndex - 1;
      return {
        paletteIndex: prevIndex,
        currentPaletteName: paletteNames[prevIndex]
      };
    });
  },

  // Animation controls
  toggleAutoplay: () => 
    set((state) => ({ autoplay: !state.autoplay })),

  setAutoplay: (autoplay: boolean) => 
    set({ autoplay }),

  setSpeed: (speed: number) => 
    set({ speed: Math.max(1, Math.min(100, speed)) }),

  // Drawing state
  setIsDrawing: (isDrawing: boolean) => 
    set({ isDrawing }),

  setLastPosition: (x: number, y: number) => 
    set({ lastX: x, lastY: y }),

  // Advanced parameters
  setParam1: (value: number) => 
    set({ param1: Math.max(0, Math.min(10, value)) }),

  setParam2: (value: number) => 
    set({ param2: Math.max(0, Math.min(10, value)) }),

  // Canvas controls
  setBackgroundColor: (color: string) => 
    set({ backgroundColor: color }),

  // UI controls
  togglePanel: () => 
    set((state) => ({ panelVisible: !state.panelVisible })),

  setPanelVisible: (visible: boolean) => 
    set({ panelVisible: visible }),

  // Utility functions
  reset: () => 
    set(INITIAL_STATE),

  getCurrentPalette: () => {
    const state = get();
    return COLOR_PALETTES[state.currentPaletteName];
  },

  getCurrentColor: () => {
    const state = get();
    const palette = COLOR_PALETTES[state.currentPaletteName];

    if (state.colorIndex === 0) {
      // Cycle through palette randomly
      return palette[Math.floor(Math.random() * palette.length)];
    } else {
      // Use specific color (colorIndex - 1 because 0 is "cycle")
      return palette[(state.colorIndex - 1) % palette.length];
    }
  },
}));

// ===== STORE SELECTORS (for performance optimization) =====
export const selectPatternState = (state: AppState) => ({
  patternIndex: state.patternIndex,
  segments: state.segments,
  thickness: state.thickness,
  stampSize: state.stampSize,
  zoom: state.zoom,
});

export const selectColorState = (state: AppState) => ({
  paletteIndex: state.paletteIndex,
  currentPaletteName: state.currentPaletteName,
  colorIndex: state.colorIndex,
  getCurrentPalette: state.getCurrentPalette,
  getCurrentColor: state.getCurrentColor,
});

export const selectAnimationState = (state: AppState) => ({
  autoplay: state.autoplay,
  speed: state.speed,
  isDrawing: state.isDrawing,
});

export const selectAdvancedParams = (state: AppState) => ({
  param1: state.param1,
  param2: state.param2,
});