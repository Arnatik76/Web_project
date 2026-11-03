/*
 * Library and reading progress, offline-first.
 * Everything lives in this browser (localStorage) and works without an account. When signed in, every
 * change is also sent to the server, and a full sync merges devices (newest change wins per title).
 * Exposes window.MV for app.js and reader.js.
 */
window.MV = (() => {
  'use strict';

  const LIBRARY_KEY = 'mv-library';
  const PROGRESS_KEY = 'mv-progress';
  const SYNCED_KEY = 'mv-synced-at';
  const SLUG = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
  const CHAPTER = /^\d{1,5}(?:\.\d)?$/;
  const STATUSES = { READING: 'Читаю', PLANNED: 'В планах', COMPLETED: 'Прочитано', ON_HOLD: 'Отложено', DROPPED: 'Брошено' };

  const meta = (name) => document.querySelector(`meta[name="${name}"]`)?.content || '';
  const signedIn = meta('mv-user') === '1';
  const csrfHeader = meta('_csrf_header');
  const csrfToken = meta('_csrf');

  const read = (key) => {
    try { return JSON.parse(localStorage.getItem(key)) || {}; } catch { return {}; }
  };
  const write = (key, value) => {
    try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage full or blocked */ }
  };
  const isSafeCover = (url) => typeof url === 'string' && url.startsWith('/') && !url.startsWith('//');
  const emit = () => document.dispatchEvent(new CustomEvent('mv:change'));

  function api(method, url, body) {
    if (!signedIn) return Promise.resolve(null);
    const headers = { 'Content-Type': 'application/json', Accept: 'application/json' };
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
    return fetch(url, { method, headers, body: JSON.stringify(body), credentials: 'same-origin', keepalive: true })
      .then((response) => (response.ok ? response : null))
      .catch(() => null); // offline: the next full sync carries the change
  }

  /* ---- Library --------------------------------------------------------- */
  const library = {
    statuses: STATUSES,
    /** Entries with a status (tombstones excluded), newest first. */
    all() {
      return Object.values(read(LIBRARY_KEY))
        .filter((e) => e && SLUG.test(e.slug) && STATUSES[e.status] && isSafeCover(e.cover))
        .sort((a, b) => b.updatedAt - a.updatedAt);
    },
    get(slug) {
      const entry = read(LIBRARY_KEY)[slug];
      return entry && STATUSES[entry.status] ? entry : null;
    },
    /** status null removes the title (kept as a tombstone so the removal syncs). */
    set(slug, status, details = {}) {
      if (!SLUG.test(slug) || (status !== null && !STATUSES[status])) return;
      const all = read(LIBRARY_KEY);
      const previous = all[slug] || {};
      const updatedAt = Date.now();
      all[slug] = { slug, status, updatedAt, title: details.title || previous.title || '', cover: details.cover || previous.cover || '' };
      write(LIBRARY_KEY, all);
      api('PUT', `/api/me/library/${slug}`, { status, updatedAt });
      emit();
    },
  };

  /* ---- Progress -------------------------------------------------------- */
  let pending = null;
  let timer = null;
  const flush = () => {
    if (!pending) return;
    const entry = pending;
    pending = null;
    clearTimeout(timer);
    api('PUT', `/api/me/progress/${entry.slug}`, entry);
  };
  document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'hidden') flush(); });
  window.addEventListener('pagehide', flush);

  const progress = {
    all() {
      return Object.values(read(PROGRESS_KEY))
        .filter((e) => e && SLUG.test(e.slug) && CHAPTER.test(String(e.chapter)) && isSafeCover(e.cover))
        .sort((a, b) => b.updatedAt - a.updatedAt);
    },
    get(slug) {
      return progress.all().find((e) => e.slug === slug) || null;
    },
    save(entry) {
      if (!SLUG.test(entry.slug) || !CHAPTER.test(String(entry.chapter))) return;
      const all = read(PROGRESS_KEY);
      all[entry.slug] = { ...entry, updatedAt: Date.now() };
      const kept = Object.values(all).sort((a, b) => b.updatedAt - a.updatedAt).slice(0, 200);
      write(PROGRESS_KEY, Object.fromEntries(kept.map((e) => [e.slug, e])));
      // Page turns are frequent: send at most every 2 s, and always when the page is hidden.
      pending = all[entry.slug];
      clearTimeout(timer);
      timer = setTimeout(flush, 2000);
    },
  };

  /* ---- Full sync ------------------------------------------------------- */
  async function sync() {
    if (!signedIn) return false;
    const body = {
      library: Object.values(read(LIBRARY_KEY)).filter((e) => e && SLUG.test(e.slug))
        .map(({ slug, status, updatedAt }) => ({ slug, status: STATUSES[status] ? status : null, updatedAt })),
      progress: progress.all().filter((e) => e.page >= 1 && e.total >= e.page)
        .map(({ slug, chapter, page, total, finished, updatedAt }) => ({ slug, chapter: String(chapter), page, total, finished: !!finished, updatedAt })),
    };
    const response = await api('POST', '/api/me/sync', body);
    if (!response) return false;
    const state = await response.json();
    write(LIBRARY_KEY, Object.fromEntries(state.library.map((e) => [e.slug, e])));
    write(PROGRESS_KEY, Object.fromEntries(state.progress.map((e) => [e.slug, e])));
    try { sessionStorage.setItem(SYNCED_KEY, String(Date.now())); } catch { /* ignore */ }
    emit();
    return true;
  }

  function forget() {
    try {
      localStorage.removeItem(LIBRARY_KEY);
      localStorage.removeItem(PROGRESS_KEY);
      sessionStorage.removeItem(SYNCED_KEY);
    } catch { /* ignore */ }
    // Cached pages may show the account's name; saved chapter images are fine to keep.
    if ('caches' in window) caches.keys().then((keys) => keys.filter((k) => k.startsWith('mv-pages')).forEach((k) => caches.delete(k)));
  }

  // Sync once per browser session, right after signing in, and when coming back online.
  if (signedIn) {
    let last = 0;
    try { last = Number(sessionStorage.getItem(SYNCED_KEY)) || 0; } catch { /* ignore */ }
    if (!last || new URLSearchParams(location.search).has('synced') || Date.now() - last > 10 * 60 * 1000) sync();
    window.addEventListener('online', sync);
  }

  // Signing out of a shared device should not leave the account's library behind.
  document.addEventListener('submit', (event) => {
    if (event.target instanceof HTMLFormElement && event.target.matches('[data-logout]')) forget();
  });

  // Offline support (see /sw.js). Registered after load so it never competes with the first paint.
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}));
  }

  return { signedIn, library, progress, sync };
})();
