import type { Pattern } from './types';

/**
 * Pattern 0: Free Draw (Line)
 * 
 * Draws a line from the previous position to the current position.
 * This is the fundamental drawing mode for freehand drawing.
 */
export const linePattern: Pattern = {
  name: 'Free Draw',
  description: 'Freehand line drawing',

  draw: (p, state, relativeX, relativeY, prevRelativeX, prevRelativeY) => {
    p.line(prevRelativeX, prevRelativeY, relativeX, relativeY);
  },
};