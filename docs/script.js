document.addEventListener('DOMContentLoaded', () => {
    // 0. Theme Switcher Logic
    const themeBtns = document.querySelectorAll('.theme-btn');
    const rootElement = document.documentElement;
    
    function setTheme(mode) {
        if (mode === 'system') {
            rootElement.removeAttribute('data-theme');
        } else {
            rootElement.setAttribute('data-theme', mode);
        }
        
        localStorage.setItem('akashboard-theme', mode);
        
        themeBtns.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.mode === mode);
        });
    }

    // Sync button states with initialized theme from <head>
    const activeTheme = rootElement.getAttribute('data-theme') || 'system';
    themeBtns.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === activeTheme);
        btn.addEventListener('click', (e) => setTheme(e.target.dataset.mode));
    });

    // 1. Signature Interaction: Part Highlighting
    const parts = document.querySelectorAll('.part-group');
    const rows = document.querySelectorAll('.spec-table tbody tr');

    function highlightPart(partId) {
        // Clear all highlights
        parts.forEach(p => p.querySelector('.part-box').classList.remove('highlight-part'));
        rows.forEach(r => r.classList.remove('highlight-row'));

        if (!partId) return;

        // Apply highlights
        const partElement = document.querySelector(`.part-group[data-part="${partId}"] .part-box`);
        const rowElement = document.querySelector(`.spec-table tbody tr[data-part="${partId}"]`);

        if (partElement) partElement.classList.add('highlight-part');
        if (rowElement) rowElement.classList.add('highlight-row');
    }

    // Attach listeners to SVG parts
    parts.forEach(part => {
        part.addEventListener('mouseenter', () => highlightPart(part.dataset.part));
        part.addEventListener('mouseleave', () => highlightPart(null));
    });

    // Attach listeners to Table rows
    rows.forEach(row => {
        if (row.dataset.part) {
            row.addEventListener('mouseenter', () => highlightPart(row.dataset.part));
            row.addEventListener('mouseleave', () => highlightPart(null));
        }
    });

    // 2. Web Audio API Mechanical Switch Click (preserved from previous hardware aesthetic)
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    
    function playClick() {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        const filter = audioCtx.createBiquadFilter();

        osc.type = 'square';
        osc.frequency.setValueAtTime(150, audioCtx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(40, audioCtx.currentTime + 0.03);

        filter.type = 'bandpass';
        filter.frequency.value = 1000;
        filter.Q.value = 2;

        gain.gain.setValueAtTime(0.5, audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.03);

        osc.connect(filter);
        filter.connect(gain);
        gain.connect(audioCtx.destination);

        osc.start();
        osc.stop(audioCtx.currentTime + 0.03);
    }

    // Attach click sound to all interactive elements
    document.querySelectorAll('.tab, .part-group, .spec-table tbody tr, .theme-btn, .direct-download-btn, .author-link').forEach(el => {
        el.addEventListener('mousedown', playClick);
    });

    // 3. Tab Navigation Scrolling
    const tabs = document.querySelectorAll('.tab');
    const sections = [
        document.getElementById('s-01'), // INTRO
        document.getElementById('s-02'), // EXPLODED
        document.getElementById('s-03'), // SPECS
        document.getElementById('s-04'), // INSTALL
        document.getElementById('s-05')  // WARNINGS
    ];

    tabs.forEach((tab, index) => {
        tab.addEventListener('click', () => {
            if (sections[index]) {
                sections[index].scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
});
