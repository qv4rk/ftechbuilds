import type { Pattern } from './types';

/**
 * Pattern 3: DNA Double Helix
 * 
 * Creates a double helix structure resembling DNA.
 * Colors cycle through the palette for depth illusion.
 * Animates when autoplay is enabled.
 */
export const dnaHelixPattern: Pattern = {
  name: 'DNA Helix',
  description: 'Double helix structure with color cycling',

  draw: (p, state, relativeX, relativeY, _prevX, _prevY, time) => {
    const palette = state.getCurrentPalette();
    const rungs = 10;
    const amplitude = state.stampSize * 0.5;
    const frequency = 0.1;
    const animOffset = state.autoplay ? time * 0.02 : 0;

    p.strokeWeight(state.thickness * 0.8);

    // Draw the double helix rungs
    for (let i = 0; i < rungs; i++) {
      const t = i / rungs;
      const offsetT = (t + animOffset) % 1;

      // Calculate helix positions
      const x1 = relativeX + p.sin((offsetT * p.TWO_PI * frequency) + p.HALF_PI) * amplitude;
      const y1 = relativeY + (t - 0.5) * state.stampSize * 2;
      const x2 = relativeX - p.sin((offsetT * p.TWO_PI * frequency) + p.HALF_PI) * amplitude;
      const y2 = y1;

      // Cycle through palette colors
      const colorIndex = i % palette.length;
      p.stroke(palette[colorIndex]);

      // Draw rung
      p.line(x1, y1, x2, y2);

      // Draw helix curves
      if (i > 0) {
        const prevT = (i - 1) / rungs;
        const prevOffsetT = (prevT + animOffset) % 1;

        const px1 = relativeX + p.sin((prevOffsetT * p.TWO_PI * frequency) + p.HALF_PI) * amplitude;
        const py1 = relativeY + (prevT - 0.5) * state.stampSize * 2;
        const px2 = relativeX - p.sin((prevOffsetT * p.TWO_PI * frequency) + p.HALF_PI) * amplitude;
        const py2 = py1;

        p.strokeWeight(state.thickness * 1.5);
        p.stroke(palette[i % palette.length]);

        // Left helix strand
        p.line(px1, py1, x1, y1);

        // Right helix strand
        p.line(px2, py2, x2, y2);

        p.strokeWeight(state.thickness * 0.8);
      }
    }
  },
};