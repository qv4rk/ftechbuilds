import { useStore } from '../core/store';
import { COLOR_PALETTES } from '../core/store';
import type { PaletteName } from '../core/store';

export function ControlPanel() {
  const {
    // Pattern state
    patternIndex,
    setPatternIndex,

    // Geometry state
    segments,
    setSegments,
    thickness,
    setThickness,
    stampSize,
    setStampSize,
    zoom,
    setZoom,

    // Color state
    currentPaletteName,
    setPaletteName,
    colorIndex,
    setColorIndex,

    // Animation state
    autoplay,
    toggleAutoplay,
    speed,
    setSpeed,

    // Advanced parameters
    param1,
    setParam1,
    param2,
    setParam2,

    // UI state
    panelVisible,
    togglePanel,

    // Utility
    reset,
  } = useStore();

  // Pattern names corresponding to indices
  const patternNames = [
    'Free Draw',
    'Golden Spiral',
    'Flower of Life',
    'DNA Helix',
    "Metatron's Cube",
    'Koch Snowflake',
    'Sierpinski Triangle',
    'Fibonacci Squares',
    'Lorenz Attractor',
    'Mandelbrot Edge',
    'Tesseract (4D)',
    'Bell Curve',
  ];

  const paletteNames = Object.keys(COLOR_PALETTES) as PaletteName[];

  // Clear canvas function
  const handleClear = () => {
    const canvas = document.querySelector('canvas');
    if (canvas) {
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
      }
    }
  };

  // Save canvas as PNG
  const handleSave = () => {
    const canvas = document.querySelector('canvas');
    if (canvas) {
      const link = document.createElement('a');
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
      link.download = `aetherscope-${timestamp}.png`;
      link.href = canvas.toDataURL('image/png');
      link.click();
    }
  };

  return (
    <>
      {/* Control Panel */}
      <div className={`control-panel ${panelVisible ? '' : 'hidden'}`}>
        {/* Header */}
        <div className="control-panel-header">
          <h2 className="control-panel-title">AetherScope 2D</h2>
          <button 
            onClick={togglePanel}
            className="btn"
            style={{ padding: '4px 8px' }}
            aria-label="Close panel"
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="control-panel-body">
          {/* Pattern Selection */}
          <div className="control-group">
            <label className="control-label">
              <span>Pattern</span>
            </label>
            <select 
              className="select-control"
              value={patternIndex}
              onChange={(e) => setPatternIndex(Number(e.target.value))}
            >
              {patternNames.map((name, index) => (
                <option key={index} value={index}>
                  {name}
                </option>
              ))}
            </select>
          </div>

          {/* Color Palette */}
          <div className="control-group">
            <label className="control-label">
              <span>Color Palette</span>
            </label>
            <select 
              className="select-control"
              value={currentPaletteName}
              onChange={(e) => setPaletteName(e.target.value as PaletteName)}
            >
              {paletteNames.map((name) => (
                <option key={name} value={name}>
                  {name.charAt(0).toUpperCase() + name.slice(1)}
                </option>
              ))}
            </select>
          </div>

          {/* Color Mode */}
          <div className="control-group">
            <label className="control-label">
              <span>Color Mode</span>
            </label>
            <select 
              className="select-control"
              value={colorIndex}
              onChange={(e) => setColorIndex(Number(e.target.value))}
            >
              <option value={0}>Cycle Colors</option>
              <option value={1}>Color 1</option>
              <option value={2}>Color 2</option>
              <option value={3}>Color 3</option>
              <option value={4}>Color 4</option>
              <option value={5}>Color 5</option>
            </select>
          </div>

          {/* Mirrors (Segments) */}
          <div className="control-group">
            <label className="control-label">
              <span>Mirrors</span>
              <span className="control-value">{segments}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="3"
              max="24"
              step="1"
              value={segments}
              onChange={(e) => setSegments(Number(e.target.value))}
            />
          </div>

          {/* Thickness */}
          <div className="control-group">
            <label className="control-label">
              <span>Thickness</span>
              <span className="control-value">{thickness.toFixed(1)}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="0.5"
              max="10"
              step="0.5"
              value={thickness}
              onChange={(e) => setThickness(Number(e.target.value))}
            />
          </div>

          {/* Stamp Size */}
          <div className="control-group">
            <label className="control-label">
              <span>Stamp Size</span>
              <span className="control-value">{Math.round(stampSize)}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="10"
              max="200"
              step="5"
              value={stampSize}
              onChange={(e) => setStampSize(Number(e.target.value))}
            />
          </div>

          {/* Zoom */}
          <div className="control-group">
            <label className="control-label">
              <span>Zoom</span>
              <span className="control-value">{zoom.toFixed(1)}x</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="0.5"
              max="3"
              step="0.1"
              value={zoom}
              onChange={(e) => setZoom(Number(e.target.value))}
            />
          </div>

          {/* Speed (for animations) */}
          <div className="control-group">
            <label className="control-label">
              <span>Speed</span>
              <span className="control-value">{speed}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="1"
              max="100"
              step="1"
              value={speed}
              onChange={(e) => setSpeed(Number(e.target.value))}
            />
          </div>

          {/* Advanced Parameters */}
          <div className="control-group">
            <label className="control-label">
              <span>Parameter 1</span>
              <span className="control-value">{param1.toFixed(1)}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="0"
              max="10"
              step="0.1"
              value={param1}
              onChange={(e) => setParam1(Number(e.target.value))}
            />
          </div>

          <div className="control-group">
            <label className="control-label">
              <span>Parameter 2</span>
              <span className="control-value">{param2.toFixed(1)}</span>
            </label>
            <input 
              type="range"
              className="range-slider"
              min="0"
              max="10"
              step="0.1"
              value={param2}
              onChange={(e) => setParam2(Number(e.target.value))}
            />
          </div>

          {/* Action Buttons */}
          <div className="btn-group">
            <button 
              className={`btn ${autoplay ? 'active' : ''}`}
              onClick={toggleAutoplay}
            >
              {autoplay ? 'Stop' : 'Auto'}
            </button>
            <button 
              className="btn"
              onClick={handleClear}
            >
              Clear
            </button>
            <button 
              className="btn"
              onClick={handleSave}
            >
              Save
            </button>
          </div>

          {/* Reset Button */}
          <div style={{ marginTop: 'var(--space-md)' }}>
            <button 
              className="btn full-width"
              onClick={reset}
            >
              Reset All
            </button>
          </div>
        </div>
      </div>

      {/* Toggle Button (shows when panel is hidden) */}
      <button 
        className={`toggle-panel-btn ${panelVisible ? '' : 'visible'}`}
        onClick={togglePanel}
        aria-label="Open control panel"
      >
        ⚙
      </button>
    </>
  );
}