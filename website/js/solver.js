/* ==========================================================================
   HANOI - GENERATOR-BASED RECURSIVE SOLVER ENGINE & RENDERER (solver.js)
   ========================================================================== */

class HanoiSimulator {
    constructor() {
        this.diskCount = 4;
        this.pegs = [[], [], []];
        this.moves = 0;
        this.isPlaying = false;
        this.speed = 600; // ms per move
        this.timer = null;
        this.moveGenerator = null;
        
        // DOM Elements
        this.pegsContainer = document.getElementById('pegs-row');
        this.movesCountEl = document.getElementById('current-moves');
        this.optimalMovesEl = document.getElementById('optimal-moves');
        this.diskCountSelect = document.getElementById('disk-count-select');
        
        // Buttons
        this.playBtn = document.getElementById('sim-play');
        this.stepBtn = document.getElementById('sim-step');
        this.resetBtn = document.getElementById('sim-reset');
        this.speedBtn = document.getElementById('sim-speed');
        
        this.init();
    }

    init() {
        // Initialize pegs arrays
        this.resetState();
        
        // Bind UI Controls
        if (this.playBtn) this.playBtn.addEventListener('click', () => this.togglePlay());
        if (this.stepBtn) this.stepBtn.addEventListener('click', () => this.step());
        if (this.resetBtn) this.resetBtn.addEventListener('click', () => this.reset());
        if (this.speedBtn) this.speedBtn.addEventListener('click', () => this.cycleSpeed());
        
        if (this.diskCountSelect) {
            this.diskCountSelect.addEventListener('change', (e) => {
                this.diskCount = parseInt(e.target.value, 10);
                this.reset();
            });
        }
        
        // Initial Draw
        this.renderBoard();
        
        // Auto start simulation on load after a brief delay
        setTimeout(() => {
            this.togglePlay();
        }, 1200);
    }

    resetState() {
        this.pegs = [[], [], []];
        // Populate peg 0 with disks descending in size
        for (let i = this.diskCount; i >= 1; i--) {
            this.pegs[0].push(i);
        }
        this.moves = 0;
        this.isPlaying = false;
        if (this.timer) clearInterval(this.timer);
        this.timer = null;
        
        // Re-generate the optimal moves list
        this.moveGenerator = this.solveHanoiGenerator(this.diskCount, 0, 2, 1);
        
        this.updateStats();
    }

    reset() {
        this.resetState();
        this.renderBoard();
        this.updatePlayButtonUI();
    }

    updateStats() {
        if (this.movesCountEl) this.movesCountEl.textContent = this.moves;
        if (this.optimalMovesEl) {
            const optimal = Math.pow(2, this.diskCount) - 1;
            this.optimalMovesEl.textContent = optimal;
        }
    }

    // Generator function for standard recursive solution
    // Yields moves in format: { from: integer, to: integer }
    *solveHanoiGenerator(n, source, target, auxiliary) {
        if (n > 0) {
            yield* this.solveHanoiGenerator(n - 1, source, auxiliary, target);
            yield { from: source, to: target };
            yield* this.solveHanoiGenerator(n - 1, auxiliary, target, source);
        }
    }

    renderBoard() {
        if (!this.pegsContainer) return;
        
        // Clear previous board
        this.pegsContainer.innerHTML = '';
        
        // Generate Pegs Columns
        const pegLabels = ['A', 'B', 'C'];
        const pegCenters = [16.66, 50, 83.33]; // percentage positions for peg centers
        
        for (let i = 0; i < 3; i++) {
            const pegCol = document.createElement('div');
            pegCol.className = 'peg-column';
            pegCol.setAttribute('data-peg', i);
            
            const pegShaft = document.createElement('div');
            pegShaft.className = 'peg-shaft';
            
            const label = document.createElement('span');
            label.className = 'peg-label';
            label.textContent = `Peg ${pegLabels[i]}`;
            
            pegCol.appendChild(pegShaft);
            pegCol.appendChild(label);
            this.pegsContainer.appendChild(pegCol);
        }

        // Draw disks as absolute positioned objects relative to their current peg stack
        for (let pegIdx = 0; pegIdx < 3; pegIdx++) {
            const stack = this.pegs[pegIdx];
            for (let diskIdx = 0; diskIdx < stack.length; diskIdx++) {
                const diskValue = stack[diskIdx];
                const diskEl = document.createElement('div');
                diskEl.className = `disk disk-${diskValue}`;
                diskEl.id = `disk-obj-${diskValue}`;
                
                // Calculate absolute coordinates
                // Base bottom offset + thickness * diskIndex
                const bottomPosition = 20 + (diskIdx * 20); 
                const leftPosition = pegCenters[pegIdx];
                
                diskEl.style.bottom = `${bottomPosition}px`;
                diskEl.style.left = `${leftPosition}%`;
                
                this.pegsContainer.appendChild(diskEl);
            }
        }
    }

    animateMove(fromPeg, toPeg, diskValue) {
        const diskEl = document.getElementById(`disk-obj-${diskValue}`);
        if (!diskEl) return;
        
        const pegCenters = [16.66, 50, 83.33];
        const targetDiskIdx = this.pegs[toPeg].length; // Index where disk will land (pre-push)
        
        // Coordinate shifts
        const targetLeft = pegCenters[toPeg];
        const targetBottom = 20 + (targetDiskIdx * 20);
        
        // High fidelity animation sequence: Lift up, slide sideways, drop down
        // 1. Lift Disk Above Peg Shaft
        diskEl.style.zIndex = '100';
        diskEl.style.bottom = '170px';
        
        setTimeout(() => {
            // 2. Slide to new Peg column horizontal location
            diskEl.style.left = `${targetLeft}%`;
            
            setTimeout(() => {
                // 3. Drop down onto the new stack
                diskEl.style.bottom = `${targetBottom}px`;
                diskEl.style.zIndex = '20';
                
                // Keep memory array synchronous with visual state
                this.pegs[fromPeg].pop();
                this.pegs[toPeg].push(diskValue);
                this.moves++;
                this.updateStats();
            }, this.speed * 0.35);
        }, this.speed * 0.3);
    }

    step() {
        if (!this.moveGenerator) return false;
        
        const nextMove = this.moveGenerator.next();
        if (nextMove.done) {
            this.isPlaying = false;
            if (this.timer) clearInterval(this.timer);
            this.timer = null;
            this.updatePlayButtonUI();
            
            // Auto trigger a reset/replay after solving finishes
            setTimeout(() => {
                this.reset();
                this.togglePlay();
            }, 3000);
            
            return false;
        }
        
        const { from, to } = nextMove.value;
        const diskValue = this.pegs[from][this.pegs[from].length - 1];
        
        this.animateMove(from, to, diskValue);
        return true;
    }

    togglePlay() {
        this.isPlaying = !this.isPlaying;
        this.updatePlayButtonUI();
        
        if (this.isPlaying) {
            this.runSimulation();
        } else {
            if (this.timer) clearInterval(this.timer);
            this.timer = null;
        }
    }

    runSimulation() {
        if (this.timer) clearInterval(this.timer);
        
        // Fire first move immediately
        const active = this.step();
        if (!active) return;
        
        // Keep triggering sequential steps at active speed
        this.timer = setInterval(() => {
            this.step();
        }, this.speed + 150); // slight buffer for smooth sliding layout transitions
    }

    updatePlayButtonUI() {
        if (!this.playBtn) return;
        if (this.isPlaying) {
            this.playBtn.innerHTML = `
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
                </svg> Pause
            `;
        } else {
            this.playBtn.innerHTML = `
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M8 5v14l11-7z"/>
                </svg> Play
            `;
        }
    }

    cycleSpeed() {
        if (this.speed === 600) {
            this.speed = 300;
            if (this.speedBtn) this.speedBtn.textContent = '2x Speed';
        } else if (this.speed === 300) {
            this.speed = 150;
            if (this.speedBtn) this.speedBtn.textContent = '4x Speed';
        } else {
            this.speed = 600;
            if (this.speedBtn) this.speedBtn.textContent = '1x Speed';
        }
        
        // Refresh timer running loops to incorporate speed updates
        if (this.isPlaying) {
            this.runSimulation();
        }
    }
}

// Instantiate solver globally on DOM completion
document.addEventListener('DOMContentLoaded', () => {
    window.hanoiSim = new HanoiSimulator();
});
