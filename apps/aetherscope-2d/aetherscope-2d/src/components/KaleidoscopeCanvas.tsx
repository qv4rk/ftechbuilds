import { useEffect, useRef } from 'react';
import p5 from 'p5';
import { useStore } from '../core/store';
import { createSketch } from '../core/sketch';

export function KaleidoscopeCanvas() {
  const canvasRef = useRef<HTMLDivElement>(null);
  const p5Instance = useRef<p5 | null>(null);

  // Subscribe to entire store
  const state = useStore();

  useEffect(() => {
    if (!canvasRef.current) return;

    // Create p5 instance
    p5Instance.current = new p5((p: p5) => {
      createSketch(p, state);
    }, canvasRef.current);

    // Cleanup
    return () => {
      if (p5Instance.current) {
        p5Instance.current.remove();
        p5Instance.current = null;
      }
    };
  }, []); // Only create once

  // Update p5 sketch when state changes
  useEffect(() => {
    if (p5Instance.current && (p5Instance.current as any).updateWithProps) {
      (p5Instance.current as any).updateWithProps(state);
    }
  }, [state]);

  return <div ref={canvasRef} className="canvas-container" />;
}