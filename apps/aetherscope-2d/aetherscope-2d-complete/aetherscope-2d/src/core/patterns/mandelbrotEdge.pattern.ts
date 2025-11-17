import type { Pattern } from './types';

/**
 * Pattern 9: Mandelbrot Edge
 * 
 * Renders points on the edge of the Mandelbrot set,
 * one of the most famous fractals in mathematics.
 */
export const mandelbrotEdgePattern: Pattern = {
  name: 'Mandelbrot Edge',
  description: 'Edge of the Mandelbrot set fractal',

  draw: (p, state, relativeX, relativeY) => {
    const scale = state.stampSize / 150;
    const maxIter = 30;
    const step = 3;

    p.strokeWeight(state.thickness * 0.4);

    for (let px = -75; px < 75; px += step) {
      for (let py = -75; py < 75; py += step) {
        const x0 = px * scale / 50 * 3 - 0.5;
        const y0 = py * scale / 50 * 2;

        let x = 0, y = 0;
        let iteration = 0;

        while (x * x + y * y <= 4 && iteration < maxIter) {
          const xtemp = x * x - y * y + x0;
          y = 2 * x * y + y0;
          x = xtemp;
          iteration++;
        }

        // Only draw points on the edge (not fully in or out)
        if (iteration > 2 && iteration < maxIter) {
          const alpha = p.map(iteration, 0, maxIter, 50, 255);
          p.stroke(state.getCurrentColor() + p.hex(Math.floor(alpha), 2));
          p.point(relativeX + px * scale, relativeY + py * scale);
        }
      }
    }
  },
};