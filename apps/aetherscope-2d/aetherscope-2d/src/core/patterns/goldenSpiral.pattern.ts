import type { Pattern } from './types';

/**
 * Pattern 1: Golden Spiral
 * 
 * The logarithmic spiral based on the golden ratio (φ = 1.618).
 * Found throughout nature in nautilus shells, galaxies, and hurricanes.
 * Param1 controls the length/complexity of the spiral.
 */
export const goldenSpiralPattern: Pattern = {
  name: 'Golden Spiral',
  description: 'Logarithmic spiral based on φ (golden ratio)',

  draw: (p, state, relativeX, relativeY, _prevX, _prevY, time) => {
    const PHI = 1.6180339887;
    const B = p.log(PHI) / (p.PI / 2); // Spiral growth rate
    const animOffset = state.autoplay ? time * 0.01 : 0;

    p.strokeWeight(state.thickness * 0.7);
    p.beginShape();
    p.noFill();

    const iterations = p.PI * state.param1 * 6; // Param1 controls spiral length

    for (let a = 0; a < iterations; a += 0.05) {
      const r = state.stampSize * 0.05 * p.exp(B * a);
      const x = relativeX + p.cos(a + animOffset) * r;
      const y = relativeY + p.sin(a + animOffset) * r;
      p.vertex(x, y);
    }

    p.endShape();
  },
};