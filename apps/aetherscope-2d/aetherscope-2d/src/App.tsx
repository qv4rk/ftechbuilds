import { KaleidoscopeCanvas } from './components/KaleidoscopeCanvas';
import { ControlPanel } from './components/ControlPanel';
import './App.css';

function App() {
  return (
    <div className="app-container">
      <KaleidoscopeCanvas />
      <ControlPanel />
    </div>
  );
}

export default App;