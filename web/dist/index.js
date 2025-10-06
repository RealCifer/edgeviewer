"use strict";
class EdgeDetectionViewer {
    constructor() {
        // Get DOM elements
        this.canvas = document.getElementById('frameCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.fpsElement = document.getElementById('fpsValue');
        this.resolutionElement = document.getElementById('resolutionValue');
        this.modeElement = document.getElementById('modeValue');
        this.timestampElement = document.getElementById('timestampValue');
        this.uploadBtn = document.getElementById('uploadBtn');
        this.sampleBtn = document.getElementById('loadSampleBtn');
        this.fileInput = document.getElementById('fileInput');
        // Initialize stats
        this.currentStats = {
            fps: 0,
            width: 0,
            height: 0,
            timestamp: new Date(),
            processingMode: 'None'
        };
        this.setupEventListeners();
        this.loadSampleFrame();
    }
    setupEventListeners() {
        // Upload button
        this.uploadBtn.addEventListener('click', () => {
            this.fileInput.click();
        });
        // File input change
        this.fileInput.addEventListener('change', (e) => {
            const file = e.target.files?.[0];
            if (file) {
                this.loadImageFile(file);
            }
        });
        // Sample button
        this.sampleBtn.addEventListener('click', () => {
            this.loadSampleFrame();
        });
        // Drag and drop
        this.canvas.addEventListener('dragover', (e) => {
            e.preventDefault();
            this.canvas.style.border = '3px dashed #007bff';
        });
        this.canvas.addEventListener('dragleave', () => {
            this.canvas.style.border = '2px solid #ddd';
        });
        this.canvas.addEventListener('drop', (e) => {
            e.preventDefault();
            this.canvas.style.border = '2px solid #ddd';
            const file = e.dataTransfer?.files[0];
            if (file && file.type.startsWith('image/')) {
                this.loadImageFile(file);
            }
        });
    }
    loadImageFile(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                this.displayFrame(img, {
                    fps: 15.0,
                    width: img.width,
                    height: img.height,
                    timestamp: new Date(),
                    processingMode: 'Edge Detection'
                });
            };
            img.src = e.target?.result;
        };
        reader.readAsDataURL(file);
    }
    loadSampleFrame() {
        // Create a sample edge-detected frame using Canvas API
        const sampleCanvas = document.createElement('canvas');
        sampleCanvas.width = 640;
        sampleCanvas.height = 480;
        const sampleCtx = sampleCanvas.getContext('2d');
        // Draw sample pattern
        sampleCtx.fillStyle = '#000000';
        sampleCtx.fillRect(0, 0, 640, 480);
        // Draw edge-like patterns
        sampleCtx.strokeStyle = '#FFFFFF';
        sampleCtx.lineWidth = 2;
        // Draw geometric shapes (simulating edge detection)
        for (let i = 0; i < 10; i++) {
            sampleCtx.beginPath();
            const x = Math.random() * 640;
            const y = Math.random() * 480;
            const radius = Math.random() * 50 + 20;
            sampleCtx.arc(x, y, radius, 0, Math.PI * 2);
            sampleCtx.stroke();
        }
        // Draw lines
        for (let i = 0; i < 15; i++) {
            sampleCtx.beginPath();
            sampleCtx.moveTo(Math.random() * 640, Math.random() * 480);
            sampleCtx.lineTo(Math.random() * 640, Math.random() * 480);
            sampleCtx.stroke();
        }
        // Add text
        sampleCtx.fillStyle = '#FFFFFF';
        sampleCtx.font = 'bold 24px Arial';
        sampleCtx.fillText('SAMPLE EDGE DETECTION', 150, 240);
        // Convert to image and display
        const img = new Image();
        img.onload = () => {
            this.displayFrame(img, {
                fps: 15.0,
                width: 640,
                height: 480,
                timestamp: new Date(),
                processingMode: 'Edge Detection (Sample)'
            });
        };
        img.src = sampleCanvas.toDataURL();
    }
    displayFrame(img, stats) {
        // Update canvas size
        this.canvas.width = stats.width;
        this.canvas.height = stats.height;
        // Draw image
        this.ctx.drawImage(img, 0, 0);
        // Update stats
        this.currentStats = stats;
        this.updateStatsDisplay();
        console.log('Frame displayed:', stats);
    }
    updateStatsDisplay() {
        this.fpsElement.textContent = this.currentStats.fps.toFixed(1);
        this.resolutionElement.textContent =
            `${this.currentStats.width} × ${this.currentStats.height}`;
        this.modeElement.textContent = this.currentStats.processingMode;
        this.timestampElement.textContent =
            this.currentStats.timestamp.toLocaleTimeString();
    }
    // Method for receiving base64 frames (for future WebSocket integration)
    updateFrameFromBase64(base64Data, stats) {
        const img = new Image();
        img.onload = () => {
            this.displayFrame(img, stats);
        };
        img.src = `data:image/png;base64,${base64Data}`;
    }
    // Export current frame
    exportFrame() {
        const dataUrl = this.canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.download = `frame_${Date.now()}.png`;
        link.href = dataUrl;
        link.click();
    }
}
// Initialize viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new EdgeDetectionViewer();
    // Expose to window for debugging
    window.viewer = viewer;
    console.log('Edge Detection Viewer initialized');
});
