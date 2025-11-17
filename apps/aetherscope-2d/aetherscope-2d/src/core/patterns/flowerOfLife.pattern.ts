import type { Pattern } from './types';

/**
 * Pattern 2: Flower of Life
 * 
 * Ancient sacred geometry pattern consisting of overlapping circles.
 * Found in temples and artwork across many cultures.
 * Represents the cycle of creation.
 */
export const flowerOfLifePattern: Pattern = {
  name: 'Flower of Life',
  description: 'Ancient sacred geometry with overlapping circles',

  draw: (p, state, relativeX, relativeY) => {
    const r = state.stampSize * 0.4;
    p.strokeWeight(state.thickness * 0.5);
    p.noFill();

    // Center circle
    p.circle(relativeX, relativeY, r * 2);

    // 6 surrounding circles (first ring)
    for (let i = 0; i < 6; i++) {
      const angle = (p.PI / 3) * i; // 60 degrees apart
      const x = relativeX + p.cos(angle) * r;
      const y = relativeY + p.sin(angle) * r;
      p.circle(x, y, r * 2);
    }

    // Optional: Second ring based on param1
    if (state.param1 > 5) {
      const r2 = r * 2;
      for (let i = 0; i < 12; i++) {
        const angle = (p.PI / 6) * i; // 30 degrees apart
        const x = relativeX + p.cos(angle) * r2;
        const y = relativeY + p.sin(angle) * r2;
        p.circle(x, y, r * 2);
      }
    }
  },
};