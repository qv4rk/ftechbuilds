import type { Pattern } from './types';

/**
 * Pattern 11: Bell Curve (Gaussian Distribution)
 * 
 * The normal distribution curve, fundamental to statistics and probability.
 * Represents natural variation in populations.
 */
export const bellCurvePattern: Pattern = {
  name: 'Bell Curve',
  description: 'Gaussian (normal) distribution curve',

  draw: (p, state, relativeX, relativeY) => {
    p.strokeWeight(state.thickness * 0.8);
    p.beginShape();
    p.noFill();

    const sigma = state.stampSize * 0.35;
    const range = 100;

    for (let i = -range; i <= range; i++) {
      const x = i;
      const exponent = -(x * x) / (2 * sigma * sigma);
      const y = p.exp(exponent) * state.stampSize * 1.5;

      p.vertex(relativeX + x, relativeY - y);
    }

    p.endShape();

    // Draw horizontal axis
    p.strokeWeight(state.thickness * 0.3);
    p.line(relativeX - range, relativeY, relativeX + range, relativeY);
  },
};