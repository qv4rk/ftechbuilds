import type { Pattern } from './types';

/**
 * Pattern 8: Lorenz Attractor
 * 
 * A chaotic system that demonstrates how small changes in initial conditions
 * can lead to vastly different outcomes (the "butterfly effect").
 */
export const lorenzAttractorPattern: Pattern = {
  name: 'Lorenz Attractor',
  description: 'Chaotic system demonstrating butterfly effect',

  draw: (p, state, relativeX, relativeY, _prevX, _prevY, time) => {
    p.strokeWeight(state.thickness * 0.3);
    p.beginShape();
    p.noFill();

    const scale = state.stampSize * 0.025;
    const dt = 0.01;
    const iterations = 500;

    // Lorenz parameters
    const sigma = 10;
    const rho = 28;
    const beta = 8 / 3;

    // Initial conditions (slightly varied by time for animation)
    let x = 0.1 + p.sin(time * 0.001) * 0.01;
    let y = 0;
    let z = 0;

    for (let i = 0; i < iterations; i++) {
      const dx = sigma * (y - x) * dt;
      const dy = (x * (rho - z) - y) * dt;
      const dz = (x * y - beta * z) * dt;

      x += dx;
      y += dy;
      z += dz;

      const px = relativeX + x * scale;
      const py = relativeY + y * scale;

      p.vertex(px, py);
    }

    p.endShape();
  },
};