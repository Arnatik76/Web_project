/*
 * Bottom sheets that behave like iOS: drag the grabber or header down to dismiss.
 * Closes past a distance or with a quick flick; otherwise springs back. Applies to every <dialog class="sheet">.
 */
(() => {
  'use strict';

  const phone = matchMedia('(max-width: 719px)');
  const DISMISS_DISTANCE = 110;   // px
  const DISMISS_VELOCITY = 0.6;   // px per ms

  document.querySelectorAll('dialog.sheet').forEach((sheet) => {
    let startY = 0;
    let lastY = 0;
    let lastTime = 0;
    let velocity = 0;
    let dragging = false;

    const handle = (target) => target instanceof Element && target.closest('.sheet__grabber, .sheet__head')
      && !target.closest('button, a, input');
    // A scrolled list must scroll, not drag the sheet.
    const listAtTop = (target) => {
      const list = target instanceof Element ? target.closest('.sheet__list, .sheet__body') : null;
      return !list || list.scrollTop <= 0;
    };

    sheet.addEventListener('pointerdown', (event) => {
      if (!phone.matches || event.button !== 0) return;
      if (!handle(event.target) && !listAtTop(event.target)) return;
      dragging = true;
      startY = lastY = event.clientY;
      lastTime = event.timeStamp;
      velocity = 0;
    });

    sheet.addEventListener('pointermove', (event) => {
      if (!dragging) return;
      const dy = event.clientY - startY;
      if (dy <= 0 && !sheet.classList.contains('is-dragging')) return;
      if (!sheet.classList.contains('is-dragging')) {
        if (dy < 6) return;               // ignore taps
        sheet.classList.add('is-dragging');
        sheet.setPointerCapture(event.pointerId);
      }
      velocity = (event.clientY - lastY) / Math.max(event.timeStamp - lastTime, 1);
      lastY = event.clientY;
      lastTime = event.timeStamp;
      // Rubber band when pulled upward.
      const offset = dy > 0 ? dy : dy / 4;
      sheet.style.transform = `translateY(${offset}px)`;
    });

    const end = () => {
      if (!dragging) return;
      dragging = false;
      if (!sheet.classList.contains('is-dragging')) return;
      sheet.classList.remove('is-dragging');
      const dy = lastY - startY;
      sheet.style.transition = 'transform var(--dur) var(--ease-spring)';
      if (dy > DISMISS_DISTANCE || velocity > DISMISS_VELOCITY) {
        sheet.style.transform = 'translateY(100%)';
        setTimeout(() => sheet.close(), 220);
      } else {
        sheet.style.transform = '';
      }
      setTimeout(() => { sheet.style.transition = ''; }, 300);
    };
    sheet.addEventListener('pointerup', end);
    sheet.addEventListener('pointercancel', end);
    sheet.addEventListener('close', () => { sheet.style.transform = ''; });
  });
})();
