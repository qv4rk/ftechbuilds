import { Pattern } from './types';

// Import all patterns
import { linePattern } from './line.pattern';
import { goldenSpiralPattern } from './goldenSpiral.pattern';
import { flowerOfLifePattern } from './flowerOfLife.pattern';
import { dnaHelixPattern } from './dnaHelix.pattern';
import { metatronsCubePattern } from './metatronsCube.pattern';
import { kochSnowflakePattern } from './kochSnowflake.pattern';
import { sierpinskiTrianglePattern } from './sierpinskiTriangle.pattern';
import { fibonacciSquaresPattern } from './fibonacciSquares.pattern';
import { lorenzAttractorPattern } from './lorenzAttractor.pattern';
import { mandelbrotEdgePattern } from './mandelbrotEdge.pattern';
import { tesseractPattern } from './tesseract.pattern';
import { bellCurvePattern } from './bellCurve.pattern';

/**
 * Pattern Registry
 * 
 * All patterns are registered here in order.
 * To add a new pattern:
 * 1. Create a new file: patternName.pattern.ts
 * 2. Import it above
 * 3. Add it to this array
 */
export const patterns: Pattern[] = [
  linePattern,           // 0: Free Draw
  goldenSpiralPattern,   // 1: Golden Spiral
  flowerOfLifePattern,   // 2: Flower of Life
  dnaHelixPattern,       // 3: DNA Helix
  metatronsCubePattern,  // 4: Metatron's Cube
  kochSnowflakePattern,  // 5: Koch Snowflake
  sierpinskiTrianglePattern, // 6: Sierpinski Triangle
  fibonacciSquaresPattern,   // 7: Fibonacci Squares
  lorenzAttractorPattern,    // 8: Lorenz Attractor
  mandelbrotEdgePattern,     // 9: Mandelbrot Edge
  tesseractPattern,          // 10: Tesseract (4D)
  bellCurvePattern,          // 11: Bell Curve
];

/**
 * Get pattern by index
 */
export function getPattern(index: number): Pattern {
  return patterns[index] || patterns[0];
}

/**
 * Get pattern by name
 */
export function getPatternByName(name: string): Pattern | undefined {
  return patterns.find(p => p.name.toLowerCase() === name.toLowerCase());
}