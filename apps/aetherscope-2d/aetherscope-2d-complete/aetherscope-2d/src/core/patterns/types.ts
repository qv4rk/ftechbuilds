import p5 from 'p5';
import type { AppState } from '../store';

/**
 * Pattern Interface
 * 
 * All patterns must implement this interface to ensure consistent behavior.
 * The draw function receives the full app state and coordinate information.
 */
export interface Pattern {
  /** Display name of the pattern */
  name: string;

  /** Short description of the pattern */
  description: string;

  /** 
   * Main drawing function
   * 
   * @param p - p5.js instance
   * @param state - Complete application state
   * @param relativeX - X coordinate relative to canvas center
   * @param relativeY - Y coordinate relative to canvas center
   * @param prevRelativeX - Previous X coordinate relative to canvas center
   * @param prevRelativeY - Previous Y coordinate relative to canvas center
   * @param time - Animation time value
   */
  draw: (
    p: p5,
    state: AppState,
    relativeX: number,
    relativeY: number,
    prevRelativeX: number,
    prevRelativeY: number,
    time: number
  ) => void;
}