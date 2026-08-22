/* AkashBoard live demo — type on the keycaps, nothing leaves your browser */

(function() {
  const keyboard = document.querySelector('.keyboard-hero');
  if (!keyboard) return;

  const keys = Array.from(keyboard.querySelectorAll('.keycap-base'));
  const keyMap = new Map();
  const keyLabels = {
    'KeyQ':'q','KeyW':'w','KeyE':'e','KeyR':'r','KeyT':'t','KeyY':'y','KeyU':'u','KeyI':'i','KeyO':'o','KeyP':'p',
    'KeyA':'a','KeyS':'s','KeyD':'d','KeyF':'f','KeyG':'g','KeyH':'h','KeyJ':'j','KeyK':'k','KeyL':'l',
    'KeyZ':'z','KeyX':'x','KeyC':'c','KeyV':'v','KeyB':'b','KeyN':'n','KeyM':'m',
    'Comma':',','Period':'.','Backspace':'⌫','Enter':'Enter','Shift':'⇧','Tab':'Tab','CapsLock':'Caps',
    'ArrowLeft':'←'
  };

  keys.forEach((btn, i) => { keyMap.set(i, btn); });

  function pressKey(btn) {
    btn.classList.add('pressed');
    setTimeout(() => btn.classList.remove('pressed'), 90);
  }

  // Mouse
  keys.forEach((btn, i) => {
    btn.addEventListener('mousedown', () => pressKey(btn));
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const label = btn.getAttribute('aria-label');
      pressKey(btn);
      // no input field — demo stays visual
    });
  });

  // Keyboard
  document.addEventListener('keydown', (e) => {
    const label = keyLabels[e.code] || e.key.toLowerCase();
    const idx = keys.findIndex(b => b.getAttribute('aria-label').toLowerCase() === label);
    if (idx !== -1) {
      pressKey(keys[idx]);
      e.preventDefault();
    }
  });

  // Staggered load animation
  keys.forEach((btn, i) => {
    btn.style.transitionDelay = (i * 12) + 'ms';
  });
})();
