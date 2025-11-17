import type { Pattern } from './types';

/**
 * Pattern 4: Metatron's Cube
 * 
 * Contains all five Platonic solids hidden within its structure.
 * Represents the blueprint of the universe in sacred geometry.
 */
export const metatronsCubePattern: Pattern = {
  name: "Metatron's Cube",
  description: 'Sacred geometry containing all Platonic solids',

  draw: (p, state, relativeX, relativeY) => {
    const r = state.stampSize * 0.4;
    p.strokeWeight(state.thickness * 0.4);
    p.noFill();

    // The 13 circles of Metatron's Cube
    const positions: [number, number][] = [
      [0, 0], // Center
      // Inner hexagon
      [0, -r],
      [r * 0.866, -r * 0.5],
      [r * 0.866, r * 0.5],
      [0, r],
      [-r * 0.866, r * 0.5],
      [-r * 0.866, -r * 0.5],
      // Outer points
      [0, -r * 2],
      [r * 1.732, -r],
      [r * 1.732, r],
      [0, r * 2],
      [-r * 1.732, r],
      [-r * 1.732, -r],
    ];

    // Draw circles
    const circleRadius = r * 0.3;
    positions.forEach(([x, y]) => {
      p.circle(relativeX + x, relativeY + y, circleRadius * 2);
    });

    // Draw connecting lines
    p.strokeWeight(state.thickness * 0.3);

    // Connect center to all outer circles
    for (let i = 1; i < positions.length; i++) {
      p.line(
        relativeX + positions[0][0],
        relativeY + positions[0][1],
        relativeX + positions[i][0],
        relativeY + positions[i][1]
      );
    }

    // Connect inner hexagon
    for (let i = 1; i <= 6; i++) {
      const next = i === 6 ? 1 : i + 1;
      p.line(
        relativeX + positions[i][0],
        relativeY + positions[i][1],
        relativeX + positions[next][0],
        relativeY + positions[next][1]
      );
    }
  },
};