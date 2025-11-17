import type { Pattern } from './types';

/**
 * Pattern 6: Sierpinski Triangle
 * 
 * A fractal pattern created by recursively subdividing triangles.
 * Demonstrates self-similarity and infinite complexity.
 */
export const sierpinskiTrianglePattern: Pattern = {
  name: 'Sierpinski Triangle',
  description: 'Recursive fractal triangle',

  draw: (p, state, relativeX, relativeY) => {
    p.strokeWeight(state.thickness * 0.5);
    p.noFill();

    function drawTriangle(x: number, y: number, size: number, depth: number) {
      if (depth === 0) {
        p.beginShape();
        p.vertex(x, y - size);
        p.vertex(x - size, y + size);
        p.vertex(x + size, y + size);
        p.endShape(p.CLOSE);
      } else {
        const newSize = size / 2;
        drawTriangle(x, y - newSize, newSize, depth - 1);
        drawTriangle(x - newSize, y + newSize, newSize, depth - 1);
        drawTriangle(x + newSize, y + newSize, newSize, depth - 1);
      }
    }

    const depth = Math.min(4, Math.floor(state.param1));
    drawTriangle(relativeX, relativeY, state.stampSize * 0.5, depth);
  },
};