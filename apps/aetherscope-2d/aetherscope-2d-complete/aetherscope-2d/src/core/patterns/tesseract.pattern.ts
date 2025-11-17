import type { Pattern } from './types';

/**
 * Pattern 10: Tesseract (4D Hypercube)
 * 
 * A 4-dimensional cube projected into 2D space.
 * Param1 controls the 4D rotation, creating a mesmerizing animation.
 * Represents higher-dimensional thinking.
 */
export const tesseractPattern: Pattern = {
  name: 'Tesseract (4D)',
  description: '4D hypercube projected to 2D',

  draw: (p, state, relativeX, relativeY, _prevX, _prevY, time) => {
    const size = state.stampSize * 0.6;
    const angle4D = state.param1 * 0.5 + (state.autoplay ? time * 0.03 : 0);

    p.strokeWeight(state.thickness * 0.6);

    // 16 vertices of a tesseract in 4D space
    const vertices4D = [];
    for (let i = 0; i < 16; i++) {
      vertices4D.push([
        (i & 1) ? 1 : -1,
        (i & 2) ? 1 : -1,
        (i & 4) ? 1 : -1,
        (i & 8) ? 1 : -1,
      ]);
    }

    // 4D rotation matrix (simplified)
    const rotated = vertices4D.map(v => {
      const [x, y, z, w] = v;

      // Rotate in XW plane
      const x2 = x * p.cos(angle4D) - w * p.sin(angle4D);
      const w2 = x * p.sin(angle4D) + w * p.cos(angle4D);

      // Rotate in YW plane
      const y2 = y * p.cos(angle4D * 0.7) - w2 * p.sin(angle4D * 0.7);
      const w3 = y * p.sin(angle4D * 0.7) + w2 * p.cos(angle4D * 0.7);

      // Project 4D -> 3D -> 2D
      const distance = 2;
      const w4 = 1 / (distance - w3);

      return [
        x2 * w4 * size + relativeX,
        y2 * w4 * size + relativeY,
        z * w4 * size,
      ];
    });

    // Draw edges
    const edges = [
      // Inner cube
      [0,1],[1,3],[3,2],[2,0],[4,5],[5,7],[7,6],[6,4],[0,4],[1,5],[2,6],[3,7],
      // Outer cube
      [8,9],[9,11],[11,10],[10,8],[12,13],[13,15],[15,14],[14,12],[8,12],[9,13],[10,14],[11,15],
      // Connecting inner to outer
      [0,8],[1,9],[2,10],[3,11],[4,12],[5,13],[6,14],[7,15],
    ];

    edges.forEach(([i, j]) => {
      p.line(rotated[i][0], rotated[i][1], rotated[j][0], rotated[j][1]);
    });

    // Draw vertices
    rotated.forEach(([x, y]) => {
      p.circle(x, y, state.thickness * 2);
    });
  },
};