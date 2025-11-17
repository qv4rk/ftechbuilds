import p5 from 'p5';
import type { AppState } from './store';
import { patterns } from './patterns';

// ===== SKETCH STATE =====
interface SketchState {
  currentState: AppState | null;
  time: number;
  autoX: number;
  autoY: number;
}

// ===== CREATE SKETCH =====
export function createSketch(p: p5, initialState: AppState) {
  const sketchState: SketchState = {
    currentState: initialState,
    time: 0,
    autoX: 0,
    autoY: 0,
  };

  // ===== SETUP =====
  p.setup = () => {
    const canvas = p.createCanvas(p.windowWidth, p.windowHeight);
    canvas.parent(canvas.parent()!); // Attach to parent div

    // Set canvas style
    p.pixelDensity(p.displayDensity());
    p.background(0);
    p.strokeCap(p.ROUND);
    p.strokeJoin(p.ROUND);

    // Disable right-click context menu
    canvas.elt.addEventListener('contextmenu', (e: Event) => e.preventDefault());
  };

  // ===== WINDOW RESIZE =====
  p.windowResized = () => {
    p.resizeCanvas(p.windowWidth, p.windowHeight);
    p.background(0);
  };

  // ===== UPDATE WITH PROPS (Called from React) =====
  (p as any).updateWithProps = (newState: AppState) => {
    sketchState.currentState = newState;
  };

  // ===== DRAW LOOP =====
  p.draw = () => {
    const state = sketchState.currentState;
    if (!state) return;

    // Handle autoplay animation
    if (state.autoplay) {
      handleAutoplay(p, state, sketchState);
    }

    // Increment time
    sketchState.time += state.speed * 0.01;
  };

  // ===== MOUSE/TOUCH PRESSED =====
  p.mousePressed = () => {
    const state = sketchState.currentState;
    if (!state || state.autoplay) return;

    state.setIsDrawing(true);
    state.setLastPosition(p.mouseX, p.mouseY);

    // Draw initial stamp
    drawKaleidoscope(p, state, p.mouseX, p.mouseY, sketchState.time);
  };

  // ===== MOUSE/TOUCH DRAGGED =====
  p.mouseDragged = () => {
    const state = sketchState.currentState;
    if (!state || !state.isDrawing || state.autoplay) return;

    drawKaleidoscope(p, state, p.mouseX, p.mouseY, sketchState.time);
    state.setLastPosition(p.mouseX, p.mouseY);
  };

  // ===== MOUSE/TOUCH RELEASED =====
  p.mouseReleased = () => {
    const state = sketchState.currentState;
    if (!state) return;

    state.setIsDrawing(false);
  };

  // ===== TOUCH EVENTS =====
  p.touchStarted = () => {
    p.mousePressed();
    return false; // Prevent default
  };

  p.touchMoved = () => {
    p.mouseDragged();
    return false; // Prevent default
  };

  p.touchEnded = () => {
    p.mouseReleased();
    return false; // Prevent default
  };
}

// ===== DRAW KALEIDOSCOPE =====
function drawKaleidoscope(
  p: p5,
  state: AppState,
  x: number,
  y: number,
  time: number
) {
  const centerX = p.width / 2;
  const centerY = p.height / 2;

  // Get relative coordinates from center
  const relativeX = x - centerX;
  const relativeY = y - centerY;
  const prevRelativeX = state.lastX - centerX;
  const prevRelativeY = state.lastY - centerY;

  // Get current pattern
  const currentPattern = patterns[state.patternIndex] || patterns[0];

  // Get current color
  const color = state.getCurrentColor();

  // Apply line style
  applyLineStyle(p, state, color);

  // Save transformation state
  p.push();
  p.translate(centerX, centerY);
  p.scale(state.zoom);

  // Draw with kaleidoscope symmetry
  const angleStep = p.TWO_PI / state.segments;

  for (let i = 0; i < state.segments; i++) {
    p.push();
    p.rotate(angleStep * i);

    // Draw original (reflected across radial line)
    p.push();
    currentPattern.draw(p, state, relativeX, relativeY, prevRelativeX, prevRelativeY, time);
    p.pop();

    // Draw mirror reflection
    p.push();
    p.scale(1, -1); // Flip vertically
    currentPattern.draw(p, state, relativeX, -relativeY, prevRelativeX, -prevRelativeY, time);
    p.pop();

    p.pop();
  }

  p.pop();
}

// ===== APPLY LINE STYLE =====
function applyLineStyle(p: p5, state: AppState, color: string) {
  p.stroke(color);
  p.strokeWeight(state.thickness);
  p.noFill();
}

// ===== HANDLE AUTOPLAY =====
function handleAutoplay(p: p5, state: AppState, sketchState: SketchState) {
  const centerX = p.width / 2;
  const centerY = p.height / 2;

  // Lissajous curve for smooth movement
  sketchState.autoX = centerX + p.sin(sketchState.time * 0.05) * 150;
  sketchState.autoY = centerY + p.cos(sketchState.time * 0.07) * 150;

  drawKaleidoscope(p, state, sketchState.autoX, sketchState.autoY, sketchState.time);

  state.setLastPosition(sketchState.autoX, sketchState.autoY);
}