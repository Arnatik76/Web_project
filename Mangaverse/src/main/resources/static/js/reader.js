/*
 * MangaVerse reader.
 * The page is a server-rendered vertical strip that works without JavaScript. This script adds:
 * paged mode (tap zones, swipes, arrow keys, RTL), auto-hiding chrome, the page slider,
 * per-title settings, reading progress in localStorage, preloading, fullscreen and screen wake lock.
 */
(() => {
  'use strict';

  const root = document.querySelector('[data-reader]');
  if (!root) return;

  const data = root.dataset;
  const pagesEl = root.querySelector('[data-pages]');
  const pages = [...root.querySelectorAll('.reader-page')];
  const end = root.querySelector('[data-end]');
  const slots = [...pages, end];           // index === pages.length is the end-of-chapter card
  const total = pages.length;
  const slider = root.querySelector('[data-slider]');
  const currentLabel = root.querySelector('[data-current]');
  const fullscreenButton = root.querySelector('[data-fullscreen]');
  const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)');

  const SETTINGS_KEY = `mv-reader:${data.slug}`;

  const store = {
    read(key, fallback) {
      try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch { return fallback; }
    },
    write(key, value) {
      try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage unavailable */ }
    },
  };

  let index = 0;
  let restored = false;
  const isPaged = () => data.mode === 'paged';
  const isRtl = () => data.dir === 'rtl';

  /* ---- Settings -------------------------------------------------------- */
  const settings = store.read(SETTINGS_KEY, {}) || {};

  function syncSettingButtons() {
    root.querySelectorAll('[data-setting]').forEach((button) => {
      button.setAttribute('aria-pressed', String(data[button.dataset.setting] === button.dataset.value));
    });
  }

  root.querySelectorAll('[data-setting]').forEach((button) => {
    button.addEventListener('click', () => {
      const { setting, value } = button.dataset;
      if (data[setting] === value) return;
      const keep = index;
      data[setting] = value;
      settings[setting] = value;
      store.write(SETTINGS_KEY, settings);
      syncSettingButtons();
      if (setting === 'mode' || setting === 'fit' || setting === 'width' || setting === 'gap') {
        requestAnimationFrame(() => goTo(keep, { instant: true }));
      }
      updateProgressUI();
    });
  });
  syncSettingButtons();

  /* ---- Sheets ---------------------------------------------------------- */
  root.querySelectorAll('[data-open]').forEach((button) => {
    const sheet = root.querySelector(`[data-sheet="${button.dataset.open}"]`);
    if (!sheet || typeof sheet.showModal !== 'function') return;
    button.addEventListener('click', () => {
      sheet.showModal();
      sheet.querySelector('[aria-current="page"]')?.scrollIntoView({ block: 'center' });
    });
  });
  root.querySelectorAll('.sheet').forEach((sheet) => {
    sheet.querySelector('[data-close]')?.addEventListener('click', () => sheet.close());
    sheet.addEventListener('click', (event) => { if (event.target === sheet) sheet.close(); });
  });
  const sheetOpen = () => !!root.querySelector('.sheet[open]');

  /* ---- Chrome (bars) --------------------------------------------------- */
  const setImmersive = (on) => root.classList.toggle('is-immersive', on);
  const toggleChrome = () => setImmersive(!root.classList.contains('is-immersive'));

  /* ---- Navigation ------------------------------------------------------ */
  function goTo(target, { instant = false, user = false } = {}) {
    index = Math.max(0, Math.min(target, total));
    if (isPaged()) {
      slots.forEach((slot, i) => slot.classList.toggle('is-current', i === index));
      window.scrollTo(0, 0);
      preload(index);
    } else {
      slots[index].scrollIntoView({ behavior: instant || reduceMotion.matches ? 'auto' : 'smooth', block: 'start' });
    }
    if (user) setImmersive(index > 0 && index < total);
    if (index === total) setImmersive(false);
    updateProgressUI();
    scheduleSave();
  }

  const next = () => {
    if (index >= total) {
      if (data.nextUrl) location.href = data.nextUrl;
      return;
    }
    goTo(index + 1, { user: true });
  };
  const prev = () => goTo(index - 1, { user: true });
  // Visual direction: in right-to-left manga the next page is on the left.
  const goLeft = () => (isRtl() ? next() : prev());
  const goRight = () => (isRtl() ? prev() : next());

  function updateProgressUI() {
    const page = Math.min(index + 1, Math.max(total, 1));
    if (currentLabel) currentLabel.textContent = String(total ? page : 0);
    if (slider) {
      slider.value = String(page);
      const span = Math.max(total - 1, 1);
      slider.style.setProperty('--progress', `${((page - 1) / span) * 100}%`);
    }
    if (index >= total - 2) prefetchNextChapter();
  }

  slider?.addEventListener('input', () => goTo(Number(slider.value) - 1, { instant: true }));

  /* Paged mode loads the next pages eagerly so turning a page is instant. */
  function preload(from) {
    for (let i = from; i <= from + 3 && i < total; i++) {
      const img = pages[i].querySelector('img');
      if (img && img.loading === 'lazy') img.loading = 'eager';
    }
  }

  let prefetched = false;
  function prefetchNextChapter() {
    if (prefetched || !data.nextUrl) return;
    prefetched = true;
    document.head.append(Object.assign(document.createElement('link'), { rel: 'prefetch', href: data.nextUrl }));
  }

  /* ---- Pointer: tap zones in paged mode, chrome toggle everywhere ------- */
  const isInteractive = (target) => target instanceof Element && target.closest('a, button, input, dialog');

  pagesEl.addEventListener('click', (event) => {
    if (isInteractive(event.target)) return;
    if (!isPaged()) { toggleChrome(); return; }
    const x = event.clientX / window.innerWidth;
    if (x < 0.3) goLeft();
    else if (x > 0.7) goRight();
    else toggleChrome();
  });

  let touch = null;
  pagesEl.addEventListener('touchstart', (event) => {
    if (event.touches.length !== 1) { touch = null; return; }
    touch = { x: event.touches[0].clientX, y: event.touches[0].clientY };
  }, { passive: true });
  pagesEl.addEventListener('touchend', (event) => {
    if (!touch || !isPaged() || (window.visualViewport && window.visualViewport.scale > 1.01)) { touch = null; return; }
    const dx = event.changedTouches[0].clientX - touch.x;
    const dy = event.changedTouches[0].clientY - touch.y;
    touch = null;
    if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy) * 1.5) {
      // Finger moving left reveals what is on the right, and vice versa.
      if (dx < 0) goRight(); else goLeft();
    }
  }, { passive: true });

  /* ---- Keyboard -------------------------------------------------------- */
  document.addEventListener('keydown', (event) => {
    if (event.metaKey || event.ctrlKey || event.altKey || sheetOpen()) return;
    if (event.target instanceof HTMLInputElement && event.target !== slider) return;
    switch (event.key) {
      case 'ArrowLeft':
        if (isPaged()) { event.preventDefault(); goLeft(); }
        break;
      case 'ArrowRight':
        if (isPaged()) { event.preventDefault(); goRight(); }
        break;
      case ' ':
      case 'PageDown':
        if (isPaged()) { event.preventDefault(); event.shiftKey ? prev() : next(); }
        break;
      case 'PageUp':
        if (isPaged()) { event.preventDefault(); prev(); }
        break;
      case '[':
        if (data.prevUrl) location.href = data.prevUrl;
        break;
      case ']':
        if (data.nextUrl) location.href = data.nextUrl;
        break;
      case 'f':
      case 'F':
        toggleFullscreen();
        break;
      default:
    }
  });

  /* ---- Vertical mode: current page follows the viewport center ---------- */
  const observer = new IntersectionObserver((entries) => {
    if (isPaged()) return;
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      index = slots.indexOf(entry.target);
      if (index === total) setImmersive(false);
      updateProgressUI();
      scheduleSave();
    }
  }, { rootMargin: '-50% 0px -50% 0px' });
  slots.forEach((slot) => observer.observe(slot));

  let lastY = window.scrollY;
  let ticking = false;
  window.addEventListener('scroll', () => {
    if (ticking || isPaged()) return;
    ticking = true;
    requestAnimationFrame(() => {
      const y = window.scrollY;
      if (y - lastY > 12 && y > 120 && index < total) setImmersive(true);
      else if (lastY - y > 24) setImmersive(false);
      lastY = y;
      ticking = false;
    });
  }, { passive: true });

  /* ---- Progress -------------------------------------------------------- */
  let saveTimer;
  function scheduleSave() {
    if (!restored || !total) return;
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveProgress, 300);
  }

  function saveProgress() {
    const finished = index >= total;
    window.MV?.progress.save({
      slug: data.slug,
      title: data.mangaTitle,
      cover: data.cover,
      chapter: data.chapter,
      chapterLabel: data.chapterLabel,
      page: Math.min(index + 1, total),
      total,
      finished,
    });
    // Finishing the last chapter moves the title from "Читаю" to "Прочитано".
    if (finished && !data.nextUrl && window.MV?.library.get(data.slug)?.status === 'READING') {
      window.MV.library.set(data.slug, 'COMPLETED', { title: data.mangaTitle, cover: data.cover });
    }
  }

  function restore() {
    if ('scrollRestoration' in history) history.scrollRestoration = 'manual';
    const param = Number(new URLSearchParams(location.search).get('page'));
    const saved = window.MV?.progress.get(data.slug);
    let page = 1;
    if (Number.isInteger(param) && param > 0) page = param;
    else if (saved && saved.chapter === data.chapter && !saved.finished) page = saved.page;
    goTo(Math.min(page, total) - 1, { instant: true });
    restored = true;
    scheduleSave();
    // Opening a title adds it to "Читаю", like Apple Books' Reading Now.
    if (window.MV && !window.MV.library.get(data.slug)) {
      window.MV.library.set(data.slug, 'READING', { title: data.mangaTitle, cover: data.cover });
    }
  }

  /* ---- Fullscreen & wake lock ------------------------------------------ */
  function toggleFullscreen() {
    if (!document.fullscreenEnabled) return;
    if (document.fullscreenElement) document.exitFullscreen();
    else document.documentElement.requestFullscreen().catch(() => {});
  }
  if (fullscreenButton && document.fullscreenEnabled) {
    fullscreenButton.hidden = false;
    fullscreenButton.addEventListener('click', toggleFullscreen);
  }
  document.addEventListener('fullscreenchange', () => {
    root.classList.toggle('is-fullscreen', !!document.fullscreenElement);
  });

  // Keep the screen on while reading.
  let wakeLock = null;
  async function keepAwake() {
    if (!('wakeLock' in navigator) || document.visibilityState !== 'visible') return;
    try { wakeLock = await navigator.wakeLock.request('screen'); } catch { wakeLock = null; }
  }
  document.addEventListener('visibilitychange', () => { if (!wakeLock || wakeLock.released) keepAwake(); });
  keepAwake();

  /* ---- Save chapter for offline reading ------------------------------- */
  // Same caches the service worker reads from: images in mv-media, the page itself in mv-pages-*.
  const offlineRow = root.querySelector('[data-offline-row]');
  const offlineButton = root.querySelector('[data-offline-save]');
  const offlineLabel = root.querySelector('[data-offline-label]');
  if (offlineRow && offlineButton && 'caches' in window && 'serviceWorker' in navigator && total) {
    offlineRow.hidden = false;
    const imageUrls = pages.map((page) => page.querySelector('img').getAttribute('src'));
    const pageUrl = location.pathname;
    const markSaved = () => {
      offlineLabel.textContent = 'Сохранено';
      offlineButton.setAttribute('aria-disabled', 'true');
    };
    caches.open('mv-media').then(async (cache) => {
      const cached = await Promise.all(imageUrls.map((url) => cache.match(url)));
      if (cached.every(Boolean) && (await caches.match(pageUrl, { ignoreSearch: true }))) markSaved();
    });
    offlineButton.addEventListener('click', async () => {
      offlineLabel.textContent = 'Сохраняем…';
      try {
        const media = await caches.open('mv-media');
        let done = 0;
        await Promise.all(imageUrls.map(async (url) => {
          if (!(await media.match(url))) await media.add(url);
          offlineLabel.textContent = `Сохраняем… ${Math.round((++done / imageUrls.length) * 100)}%`;
        }));
        const pagesCache = (await caches.keys()).find((key) => key.startsWith('mv-pages')) || 'mv-pages-v1';
        await (await caches.open(pagesCache)).add(pageUrl);
        markSaved();
      } catch {
        offlineLabel.textContent = 'Не удалось — повторить';
      }
    });
  }

  /* ---- Start ----------------------------------------------------------- */
  requestAnimationFrame(restore);
})();
