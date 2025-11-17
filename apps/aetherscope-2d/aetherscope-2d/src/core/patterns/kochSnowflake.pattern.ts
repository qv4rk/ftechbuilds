import type { Pattern } from './types';

/**
 * Pattern 5: Koch Snowflake
 * 
 * A fractal curve with infinite perimeter but finite area.
 * Demonstrates self-similarity at different scales.
 */
export const kochSnowflakePattern: Pattern = {
  name: 'Koch Snowflake',
  description: 'Fractal with infinite perimeter',

  draw: (p, state, relativeX, relativeY) => {
    const r = state.stampSize * 0.7;
    p.strokeWeight(state.thickness * 0.4);
    p.noFill();

    p.beginShape();

    // Draw hexagon as base (snowflake has 6-fold symmetry)
    for (let i = 0; i <= 6; i++) {
      const angle = (p.PI / 3) * i;
      const x = relativeX + p.cos(angle) * r;
      const y = relativeY + p.sin(angle) * r;

      if (i === 0) {
        p.vertex(x, y);
      } else {
        p.vertex(x, y);
      }
    }

    p.endShape(p.CLOSE);

    // Add fractal detail based on param1
    if (state.param1 > 3) {
      const r2 = r * 0.5;
      p.beginShape();
      for (let i = 0; i <= 12; i++) {
        const angle = (p.PI / 6) * i;
        const x = relativeX + p.cos(angle) * r2;
        const y = relativeY + p.sin(angle) * r2;
        p.vertex(x, y);
      }
      p.endShape(p.CLOSE);
    }
  },
};