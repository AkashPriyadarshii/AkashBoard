document.addEventListener('DOMContentLoaded', () => {

    // ─── Theme Switcher ───
    const themeBtns = document.querySelectorAll('.theme-btn');
    const root = document.documentElement;

    function setTheme(mode) {
        if (mode === 'system') {
            root.removeAttribute('data-theme');
        } else {
            root.setAttribute('data-theme', mode);
        }
        localStorage.setItem('akashboard-theme', mode);
        themeBtns.forEach(btn => {
            const isActive = btn.dataset.mode === mode;
            btn.classList.toggle('active', isActive);
            btn.setAttribute('aria-checked', isActive);
        });
    }

    const activeTheme = root.getAttribute('data-theme') || 'system';
    themeBtns.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === activeTheme);
        btn.setAttribute('aria-checked', btn.dataset.mode === activeTheme);
        btn.addEventListener('click', () => setTheme(btn.dataset.mode));
    });

    // ─── Mobile Menu ───
    const hamburger = document.getElementById('hamburger');
    const mobileMenu = document.getElementById('mobile-menu');
    const mobileLinks = document.querySelectorAll('.mobile-link, .mobile-cta');

    function toggleMenu() {
        const isOpen = mobileMenu.classList.toggle('open');
        hamburger.classList.toggle('active', isOpen);
        hamburger.setAttribute('aria-expanded', isOpen);
        mobileMenu.setAttribute('aria-hidden', !isOpen);
        document.body.style.overflow = isOpen ? 'hidden' : '';
    }

    hamburger.addEventListener('click', toggleMenu);
    mobileLinks.forEach(link => link.addEventListener('click', () => {
        if (mobileMenu.classList.contains('open')) toggleMenu();
    }));

    // ─── Nav Scroll ───
    const nav = document.querySelector('.top-nav');
    window.addEventListener('scroll', () => {
        nav.classList.toggle('scrolled', window.scrollY > 50);
    }, { passive: true });

    // ─── Scroll Progress ───
    const progressBar = document.querySelector('.scroll-progress');
    window.addEventListener('scroll', () => {
        const pct = window.scrollY / (document.body.scrollHeight - window.innerHeight);
        progressBar.style.transform = `scaleX(${pct})`;
    }, { passive: true });

    // ─── Scroll Reveal ───
    function initScrollReveal() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('in-view');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.15 });

        document.querySelectorAll('.reveal, .code-window').forEach(el => observer.observe(el));
    }

    initScrollReveal();

    // ─── Stat Counter Animation ───
    function animateCounter(el) {
        const target = parseInt(el.dataset.target, 10);
        const decimals = parseInt(el.dataset.decimals || '0', 10);
        const suffix = el.dataset.suffix || '';
        const duration = 1500;
        const start = performance.now();

        function tick(now) {
            const elapsed = now - start;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
            const current = eased * target;

            if (decimals > 0) {
                el.textContent = current.toFixed(decimals) + suffix;
            } else {
                el.textContent = Math.round(current).toLocaleString() + suffix;
            }

            if (progress < 1) {
                requestAnimationFrame(tick);
            }
        }

        requestAnimationFrame(tick);
    }

    function initCounters() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const nums = entry.target.querySelectorAll('.stat-number[data-target]');
                    nums.forEach(animateCounter);
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 });

        const statRow = document.querySelector('.stat-row');
        if (statRow) observer.observe(statRow);
    }

    initCounters();

    // ─── Smooth Scroll for Anchor Links ───
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', (e) => {
            const targetId = anchor.getAttribute('href');
            if (targetId === '#') return;
            const target = document.querySelector(targetId);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    // ─── Part Highlighting (SVG ↔ Table) ───
    const parts = document.querySelectorAll('.part-group');
    const specRows = document.querySelectorAll('.spec-table tbody tr');

    function highlightPart(partId) {
        parts.forEach(p => p.querySelector('.part-box').classList.remove('highlight-part'));
        specRows.forEach(r => r.classList.remove('highlight-row'));

        if (!partId) return;

        const partEl = document.querySelector(`.part-group[data-part="${partId}"] .part-box`);
        const rowEl = document.querySelector(`.spec-table tbody tr[data-part="${partId}"]`);

        if (partEl) partEl.classList.add('highlight-part');
        if (rowEl) rowEl.classList.add('highlight-row');
    }

    parts.forEach(part => {
        part.addEventListener('mouseenter', () => highlightPart(part.dataset.part));
        part.addEventListener('mouseleave', () => highlightPart(null));
    });

    specRows.forEach(row => {
        if (row.dataset.part) {
            row.addEventListener('mouseenter', () => highlightPart(row.dataset.part));
            row.addEventListener('mouseleave', () => highlightPart(null));
        }
    });

    // ─── Web Audio Mechanical Switch Synthesis (@design-engineer/audio) ───
    let audioCtx;
    function getAudioCtx() {
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        return audioCtx;
    }

    function playClick() {
        try {
            const ctx = getAudioCtx();
            if (ctx.state === 'suspended') ctx.resume();

            const t0 = ctx.currentTime;
            const duration = 0.012; // 12ms ultra-fast physical click

            // 1. Mechanical switch contact noise burst
            const bufferSize = Math.floor(ctx.sampleRate * duration);
            const noiseBuffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
            const output = noiseBuffer.getChannelData(0);
            for (let i = 0; i < bufferSize; i++) {
                output[i] = Math.random() * 2 - 1;
            }

            const whiteNoise = ctx.createBufferSource();
            whiteNoise.buffer = noiseBuffer;

            // 2. High-Q resonant bandpass filter centered at 1200Hz
            const filter = ctx.createBiquadFilter();
            filter.type = 'bandpass';
            filter.frequency.setValueAtTime(1200, t0);
            filter.Q.setValueAtTime(3.5, t0);

            // 3. Ultra-fast exponential decay
            const gain = ctx.createGain();
            gain.gain.setValueAtTime(0.25, t0);
            gain.gain.exponentialRampToValueAtTime(0.001, t0 + duration);

            whiteNoise.connect(filter);
            filter.connect(gain);
            gain.connect(ctx.destination);

            whiteNoise.start(t0);
            whiteNoise.stop(t0 + duration);
        } catch (e) {
            // Audio not available: silent fallback
        }
    }

    document.querySelectorAll('.btn-primary, .btn-ghost, .btn-dark, .btn-outline-dark, .theme-btn, .nav-link, .feature-card').forEach(el => {
        el.addEventListener('mousedown', playClick);
    });

});
