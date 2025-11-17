import type { Pattern } from './types';

/**
 * Pattern 7: Fibonacci Squares
 * 
 * Squares sized according to the Fibonacci sequence.
 * Forms the basis for the golden spiral when arcs are drawn.
 */
export const fibonacciSquaresPattern: Pattern = {
  name: 'Fibonacci Squares',
  description: 'Squares following the Fibonacci sequence',

  draw: (p, state, relativeX, relativeY) => {
    const fib = [1, 1, 2, 3, 5, 8, 13];
    const scale = state.stampSize / 25;

    p.strokeWeight(state.thickness * 0.6);
    p.noFill();

    let posX = relativeX;
    let posY = relativeY;
    let direction = 0; // 0: right, 1: up, 2: left, 3: down

    fib.forEach((f, i) => {
      if (i >= state.param1 + 3) return; // Param1 controls how many squares

      const s = f * scale;

      // Draw square
      p.rect(posX, posY, s, s);

      // Draw quarter-circle arc (golden spiral)
      p.arc(
        posX + (direction === 0 || direction === 3 ? s : 0),
        posY + (direction === 1 || direction === 2 ? 0 : s),
        s * 2,
        s * 2,
        direction * p.HALF_PI,
        (direction + 1) * p.HALF_PI
      );

      // Update position for next square
      switch (direction % 4) {
        case 0: posX += s; break;
        case 1: posY -= s; break;
        case 2: posX -= s; break;
        case 3: posY += s; break;
      }
      direction++;
    });
  },
};