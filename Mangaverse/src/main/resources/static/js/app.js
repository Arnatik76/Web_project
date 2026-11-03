/*
 * MangaVerse UI behaviour. Pages are server-rendered and work without this file;
 * it adds the theme toggle, shelf arrows, Spotlight search, sharing and small polish.
 * Data from the API is only ever inserted with textContent / attributes, never innerHTML.
 */
(() => {
  'use strict';

  const ICONS = document.body.dataset.icons || '/img/icons.svg';
  const TYPE_LABELS = { MANGA: 'Манга', MANHWA: 'Манхва', MANHUA: 'Маньхуа' };
  const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)');
  const scrollBehavior = () => (reduceMotion.matches ? 'auto' : 'smooth');

  const store = {
    get(key) { try { return localStorage.getItem(key); } catch { return null; } },
    set(key, value) { try { localStorage.setItem(key, value); } catch { /* storage unavailable */ } },
  };

  function el(tag, attrs = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(attrs)) {
      if (value != null) node.setAttribute(key, value);
    }
    node.append(...children);
    return node;
  }

  function icon(name, className = 'icon') {
    const ns = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(ns, 'svg');
    svg.setAttribute('class', className);
    svg.setAttribute('aria-hidden', 'true');
    const use = document.createElementNS(ns, 'use');
    use.setAttribute('href', `${ICONS}#${name}`);
    svg.append(use);
    return svg;
  }

  /* ---- Theme ---------------------------------------------------------- */
  const root = document.documentElement;
  const currentTheme = () =>
    root.dataset.theme || (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');

  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const next = currentTheme() === 'dark' ? 'light' : 'dark';
      root.dataset.theme = next;
      store.set('mv-theme', next);
    });
  });

  /* ---- Header: hairline appears once content scrolls under it ---------- */
  const header = document.querySelector('[data-header]');
  if (header) {
    let queued = false;
    const update = () => {
      header.classList.toggle('is-scrolled', window.scrollY > 4);
      queued = false;
    };
    window.addEventListener('scroll', () => {
      if (!queued) { queued = true; requestAnimationFrame(update); }
    }, { passive: true });
    update();
  }

  /* ---- Library & progress (window.MV from sync.js) --------------------- */
  const MV = window.MV;

  function coverCard({ href, cover, title, meta, percent }) {
    const coverBox = el('div', { class: 'cover' }, el('img', { src: cover, alt: '', width: '375', height: '562', loading: 'lazy', decoding: 'async' }));
    if (percent != null) {
      const bar = el('span', { class: 'progress', role: 'progressbar', 'aria-valuenow': String(percent),
        'aria-valuemin': '0', 'aria-valuemax': '100', 'aria-label': `Прочитано ${percent}%` }, el('span'));
      bar.firstChild.style.width = `${percent}%`;
      coverBox.append(bar);
    }
    return el('a', { class: 'card', href }, coverBox,
      el('h3', { class: 'card__title' }, String(title || '')),
      el('p', { class: 'meta' }, ...meta.map((text) => el('span', {}, text))));
  }

  const percentOf = (e) => Math.round((Number(e.page) / Math.max(Number(e.total), 1)) * 100);
  const continueHref = (e) => `/read/${e.slug}/${e.chapter}?page=${Number(e.page) || 1}`;

  // Home: "Продолжить чтение" shelf.
  const continueSection = document.querySelector('[data-continue]');
  function renderContinue() {
    if (!continueSection || !MV) return;
    const entries = MV.progress.all().filter((e) => !e.finished).slice(0, 12);
    continueSection.hidden = entries.length === 0;
    continueSection.querySelector('[data-continue-track]').replaceChildren(...entries.map((e) => coverCard({
      href: continueHref(e), cover: e.cover, title: e.title, percent: percentOf(e),
      meta: [`Гл. ${e.chapter}`, `стр. ${e.page} из ${e.total}`],
    })));
    continueSection.querySelector('[data-shelf-track]')?.dispatchEvent(new Event('scroll'));
  }

  // Title page: read marks, "Продолжить" button, library status.
  const chapterList = document.querySelector('[data-chapters]');
  const titleMeta = document.querySelector('[data-title-meta]');
  function renderTitleProgress() {
    if (!chapterList || !MV) return;
    const entry = MV.progress.get(chapterList.dataset.slug);
    const rows = [...chapterList.querySelectorAll('[data-chapter]')];
    const current = entry ? Number(entry.chapter) : -1;
    rows.forEach((row) => {
      const n = Number(row.dataset.chapter);
      row.classList.toggle('is-read', !!entry && (n < current || (n === current && entry.finished)));
      row.classList.toggle('is-current', !!entry && n === current && !entry.finished);
    });
    const cta = document.querySelector('[data-read-cta]');
    const label = cta?.querySelector('[data-read-cta-label]');
    if (!entry || !cta || !label) return;
    const nextRow = rows.find((row) => Number(row.dataset.chapter) > current);
    if (!entry.finished) {
      cta.href = continueHref(entry);
      label.textContent = `Продолжить · гл. ${entry.chapter}`;
    } else if (nextRow) {
      cta.href = nextRow.getAttribute('href');
      label.textContent = `Читать гл. ${nextRow.dataset.chapter}`;
    } else {
      label.textContent = 'Перечитать';
    }
  }

  if (chapterList) {
    const sortButton = document.querySelector('[data-chapters-sort]');
    const sortLabel = sortButton?.querySelector('[data-chapters-sort-label]');
    const applyOrder = (desc) => {
      chapterList.classList.toggle('is-desc', desc);
      if (sortLabel) sortLabel.textContent = desc ? 'Сначала новые' : 'Сначала первые';
    };
    applyOrder(store.get('mv-chapters-order') !== 'asc');
    sortButton?.addEventListener('click', () => {
      const desc = !chapterList.classList.contains('is-desc');
      applyOrder(desc);
      store.set('mv-chapters-order', desc ? 'desc' : 'asc');
    });
  }

  const librarySheet = document.querySelector('[data-library-sheet]');
  const libraryButton = document.querySelector('[data-library-open]');
  function renderLibraryButton() {
    if (!titleMeta || !libraryButton || !MV) return;
    const entry = MV.library.get(titleMeta.dataset.slug);
    libraryButton.querySelector('[data-library-label]').textContent = entry ? MV.library.statuses[entry.status] : 'В библиотеку';
    libraryButton.querySelector('[data-library-icon] use')
      ?.setAttribute('href', `${ICONS}#${entry ? 'bookmark-fill' : 'bookmark'}`);
    librarySheet?.querySelectorAll('[data-status]').forEach((item) => {
      item.setAttribute('aria-pressed', String(!!entry && item.dataset.status === entry.status));
    });
    librarySheet?.querySelectorAll('[data-library-remove], [data-library-remove-divider]').forEach((node) => { node.hidden = !entry; });
  }
  if (libraryButton && librarySheet && MV && typeof librarySheet.showModal === 'function') {
    libraryButton.addEventListener('click', () => librarySheet.showModal());
    librarySheet.querySelector('[data-close]')?.addEventListener('click', () => librarySheet.close());
    librarySheet.addEventListener('click', (event) => { if (event.target === librarySheet) librarySheet.close(); });
    librarySheet.querySelectorAll('[data-status]').forEach((item) => item.addEventListener('click', () => {
      const status = item.dataset.status || null;
      MV.library.set(titleMeta.dataset.slug, status, { title: titleMeta.dataset.title, cover: titleMeta.dataset.cover });
      librarySheet.close();
      toast(status ? `Добавлено: ${MV.library.statuses[status]}` : 'Убрано из библиотеки');
    }));
  }

  // Library page.
  const libraryGrid = document.querySelector('[data-library-grid]');
  let activeShelf = store.get('mv-library-tab') || 'READING';
  function renderLibrary() {
    if (!libraryGrid || !MV) return;
    const entries = MV.library.all();
    const history = MV.progress.all();
    const counts = { HISTORY: history.length };
    entries.forEach((e) => { counts[e.status] = (counts[e.status] || 0) + 1; });
    document.querySelectorAll('[data-count]').forEach((node) => {
      node.textContent = counts[node.dataset.count] ? String(counts[node.dataset.count]) : '';
    });
    document.querySelectorAll('[data-shelf-tab]').forEach((tab) => {
      tab.setAttribute('aria-selected', String(tab.dataset.shelfTab === activeShelf));
      tab.setAttribute('aria-pressed', String(tab.dataset.shelfTab === activeShelf));
    });

    const progressBySlug = Object.fromEntries(history.map((e) => [e.slug, e]));
    const cards = activeShelf === 'HISTORY'
      ? history.map((e) => coverCard({
        href: e.finished ? `/title/${e.slug}` : continueHref(e), cover: e.cover, title: e.title,
        percent: e.finished ? 100 : percentOf(e), meta: [`Гл. ${e.chapter}`, e.finished ? 'прочитана' : `стр. ${e.page} из ${e.total}`],
      }))
      : entries.filter((e) => e.status === activeShelf).map((e) => {
        const p = progressBySlug[e.slug];
        return coverCard({
          href: p && !p.finished ? continueHref(p) : `/title/${e.slug}`, cover: e.cover, title: e.title,
          percent: p && !p.finished ? percentOf(p) : null, meta: [p ? `Гл. ${p.chapter}` : 'Не начато'],
        });
      });
    libraryGrid.replaceChildren(...cards);
    const empty = document.querySelector('[data-library-empty]');
    if (empty) {
      empty.hidden = cards.length > 0;
      const texts = {
        HISTORY: ['История пуста', 'Здесь появятся главы, которые вы открывали.'],
        READING: ['Вы пока ничего не читаете', 'Откройте любую главу — тайтл появится здесь.'],
      };
      const [title, text] = texts[activeShelf] || ['На этой полке пусто', 'Добавьте тайтл кнопкой «В библиотеку» на его странице.'];
      empty.querySelector('[data-library-empty-title]').textContent = title;
      empty.querySelector('[data-library-empty-text]').textContent = text;
    }
  }
  document.querySelectorAll('[data-shelf-tab]').forEach((tab) => tab.addEventListener('click', () => {
    activeShelf = tab.dataset.shelfTab;
    store.set('mv-library-tab', activeShelf);
    renderLibrary();
  }));

  const renderAll = () => { renderContinue(); renderTitleProgress(); renderLibraryButton(); renderLibrary(); };
  renderAll();
  document.addEventListener('mv:change', renderAll);
  // Another tab (e.g. the reader) changed the library or progress.
  window.addEventListener('storage', (event) => { if (event.key === 'mv-library' || event.key === 'mv-progress') renderAll(); });

  // Admin upload: show the picked files and accept drag and drop.
  const dropzone = document.querySelector('[data-dropzone]');
  if (dropzone) {
    const input = dropzone.querySelector('input[type="file"]');
    const label = dropzone.querySelector('[data-dropzone-label]');
    const update = () => { label.textContent = input.files.length ? `Выбрано страниц: ${input.files.length}` : 'Выберите или перетащите страницы'; };
    input.addEventListener('change', update);
    ['dragenter', 'dragover'].forEach((type) => dropzone.addEventListener(type, (e) => { e.preventDefault(); dropzone.classList.add('is-over'); }));
    ['dragleave', 'drop'].forEach((type) => dropzone.addEventListener(type, () => dropzone.classList.remove('is-over')));
    dropzone.addEventListener('drop', (e) => { e.preventDefault(); input.files = e.dataTransfer.files; update(); });
    document.querySelector('[data-upload]')?.addEventListener('submit', (e) => {
      e.submitter?.setAttribute('aria-disabled', 'true');
      if (e.submitter) e.submitter.textContent = 'Загружаем…';
    });
  }

  /* ---- iOS-style navigation title -------------------------------------- */
  // The page's large title shrinks into the bar when it scrolls under it (phones; see CSS).
  const headerTitle = document.querySelector('[data-header-title]');
  const largeTitle = document.querySelector('main h1:not(.visually-hidden)');
  if (header && headerTitle && largeTitle) {
    headerTitle.textContent = largeTitle.textContent.trim();
    new IntersectionObserver(([entry]) => {
      header.classList.toggle('has-title', !entry.isIntersecting && entry.boundingClientRect.top < 0);
    }, { rootMargin: `-${header.offsetHeight}px 0px 0px 0px` }).observe(largeTitle);
  }

  /* ---- Title page: floating read button once the hero is gone ----------- */
  const actionBar = document.querySelector('[data-action-bar]');
  const heroActions = document.querySelector('.title-hero__actions');
  if (actionBar && heroActions) {
    const mainCta = document.querySelector('[data-read-cta]');
    const barCta = actionBar.querySelector('[data-action-bar-cta]');
    const syncCta = () => {
      if (!mainCta || !barCta) return;
      barCta.href = mainCta.href;
      const label = mainCta.querySelector('[data-read-cta-label]')?.textContent || 'Читать';
      barCta.textContent = label.startsWith('Продолжить') ? 'Продолжить' : label.replace('Начать читать', 'Читать');
    };
    syncCta();
    document.addEventListener('mv:change', () => requestAnimationFrame(syncCta));
    new IntersectionObserver(([entry]) => {
      const show = !entry.isIntersecting && entry.boundingClientRect.top < 0;
      actionBar.classList.toggle('is-visible', show);
      actionBar.toggleAttribute('inert', !show);
      actionBar.setAttribute('aria-hidden', String(!show));
    }).observe(heroActions);
  }

  /* ---- Cover "flies" from the card into the title page ------------------ */
  // Only the tapped card gets the shared name, so duplicates on a page never clash.
  document.addEventListener('click', (event) => {
    const card = event.target instanceof Element ? event.target.closest('a.card[href^="/title/"]') : null;
    if (!card || event.metaKey || event.ctrlKey || event.shiftKey || event.button !== 0) return;
    document.querySelectorAll('[style*="view-transition-name: cover"]').forEach((node) => { node.style.viewTransitionName = ''; });
    const cover = card.querySelector('.cover');
    if (cover) cover.style.viewTransitionName = 'cover';
  });
  // Coming back with the browser's back button: drop stale names left on the cached page.
  window.addEventListener('pageshow', (event) => {
    if (event.persisted) document.querySelectorAll('a.card .cover').forEach((node) => { node.style.viewTransitionName = ''; });
  });

  /* ---- Catalog filters sheet (phones) ----------------------------------- */
  const filtersSheet = document.querySelector('[data-filters-sheet]');
  const filtersButton = document.querySelector('[data-filters-open]');
  if (filtersSheet && filtersButton && typeof filtersSheet.showModal === 'function') {
    filtersButton.addEventListener('click', () => filtersSheet.showModal());
    filtersSheet.querySelector('[data-close]')?.addEventListener('click', () => filtersSheet.close());
    filtersSheet.addEventListener('click', (event) => { if (event.target === filtersSheet) filtersSheet.close(); });
  }

  /* ---- Shelves: arrow buttons that page through the row ---------------- */
  document.querySelectorAll('[data-shelf]').forEach((shelf) => {
    const track = shelf.querySelector('[data-shelf-track]');
    const prev = shelf.querySelector('[data-shelf-prev]');
    const next = shelf.querySelector('[data-shelf-next]');
    if (!track || !prev || !next) return;

    const page = (direction) => track.scrollBy({ left: direction * track.clientWidth * 0.85, behavior: scrollBehavior() });
    const sync = () => {
      prev.disabled = track.scrollLeft <= 2;
      next.disabled = track.scrollLeft + track.clientWidth >= track.scrollWidth - 2;
    };
    prev.addEventListener('click', () => page(-1));
    next.addEventListener('click', () => page(1));
    track.addEventListener('scroll', sync, { passive: true });
    window.addEventListener('resize', sync);
    sync();
  });

  /* ---- Catalog: infinite scroll on top of the "Показать ещё" link -------- */
  const grid = document.querySelector('[data-results]');
  const more = document.querySelector('[data-load-more]');
  if (grid && more && 'IntersectionObserver' in window) {
    let loading = false;
    const loadNext = async () => {
      if (loading || !more.isConnected) return;
      loading = true;
      more.setAttribute('aria-busy', 'true');
      try {
        const response = await fetch(more.href, { headers: { Accept: 'text/html' } });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const page = new DOMParser().parseFromString(await response.text(), 'text/html');
        grid.append(...page.querySelectorAll('[data-results] > .card'));
        const next = page.querySelector('[data-load-more]');
        if (next) more.href = next.getAttribute('href');
        else more.parentElement.remove();
      } catch {
        // Leave the link in place: tapping it still loads the next page normally.
      } finally {
        loading = false;
        more.removeAttribute('aria-busy');
      }
    };
    new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) loadNext();
    }, { rootMargin: '600px 0px' }).observe(more);
  }

  /* ---- Long text: "Ещё" only when the text is actually clamped ---------- */
  document.querySelectorAll('[data-clamp]').forEach((text) => {
    const toggle = text.nextElementSibling;
    if (!toggle || !toggle.matches('[data-clamp-toggle]')) return;
    let expanded = false;
    // Re-measured after fonts load and on resize: the overflow depends on the final layout.
    const measure = () => {
      if (!expanded) toggle.hidden = text.scrollHeight - text.clientHeight < 2;
    };
    toggle.addEventListener('click', () => {
      expanded = !expanded;
      text.classList.toggle('is-clamped', !expanded);
      toggle.textContent = expanded ? 'Свернуть' : 'Ещё';
      toggle.setAttribute('aria-expanded', String(expanded));
    });
    new ResizeObserver(measure).observe(text);
    document.fonts?.ready.then(measure);
    measure();
  });

  /* ---- Toast ----------------------------------------------------------- */
  let toastTimer;
  function toast(message) {
    let node = document.querySelector('.toast');
    if (!node) {
      node = el('div', { class: 'toast', role: 'status', 'aria-live': 'polite' });
      document.body.append(node);
    }
    node.textContent = message;
    requestAnimationFrame(() => node.classList.add('is-visible'));
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => node.classList.remove('is-visible'), 2200);
  }

  /* ---- Share ----------------------------------------------------------- */
  document.querySelectorAll('[data-share]').forEach((button) => {
    button.addEventListener('click', async () => {
      const data = { title: button.dataset.shareTitle || document.title, url: location.href };
      if (navigator.share) {
        try { await navigator.share(data); } catch { /* user cancelled */ }
        return;
      }
      try {
        await navigator.clipboard.writeText(data.url);
        toast('Ссылка скопирована');
      } catch {
        toast('Не удалось скопировать ссылку');
      }
    });
  });

  /* ---- Spotlight search (⌘K / Ctrl K / "/") ---------------------------- */
  const dialog = document.querySelector('[data-spotlight]');
  if (!dialog || typeof dialog.showModal !== 'function') return;

  const input = dialog.querySelector('input[name="q"]');
  const list = dialog.querySelector('[data-spotlight-results]');
  const isMac = /Mac|iPhone|iPad/i.test(navigator.userAgentData?.platform || navigator.platform || navigator.userAgent);
  document.querySelectorAll('[data-kbd-mod]').forEach((kbd) => { kbd.textContent = isMac ? '⌘K' : 'Ctrl K'; });

  let items = [];
  let active = -1;
  let popular = null;
  let request = null;
  let debounce = null;

  const open = () => {
    if (dialog.open) return;
    dialog.showModal();
    input.select();
    if (!input.value.trim()) showPopular();
  };
  const close = () => dialog.close();

  document.querySelectorAll('[data-search-open]').forEach((button) => button.addEventListener('click', open));
  dialog.querySelector('[data-spotlight-close]')?.addEventListener('click', close);
  dialog.addEventListener('click', (event) => { if (event.target === dialog) close(); });

  const isTyping = (target) => target instanceof HTMLElement
    && (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName));

  document.addEventListener('keydown', (event) => {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      dialog.open ? close() : open();
    } else if (event.key === '/' && !dialog.open && !isTyping(event.target)) {
      event.preventDefault();
      open();
    }
  });

  input.addEventListener('input', () => {
    clearTimeout(debounce);
    const query = input.value.trim();
    if (!query) { showPopular(); return; }
    debounce = setTimeout(() => search(query), 160);
  });

  input.addEventListener('keydown', (event) => {
    if (event.key === 'ArrowDown') { event.preventDefault(); select(active + 1); }
    else if (event.key === 'ArrowUp') { event.preventDefault(); select(active - 1); }
    else if (event.key === 'Enter' && items[active]) { event.preventDefault(); items[active].click(); }
    // Enter with nothing selected submits the form: full results in the catalog.
  });

  async function fetchJson(url, signal) {
    const response = await fetch(url, { signal, headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  }

  async function showPopular() {
    try {
      popular ??= (await fetchJson('/api/manga/popular')).slice(0, 6);
      if (!input.value.trim()) render(popular, 'Популярное');
    } catch {
      hint('Не удалось загрузить каталог');
    }
  }

  async function search(query) {
    request?.abort();
    request = new AbortController();
    try {
      const data = await fetchJson(`/api/manga/search?q=${encodeURIComponent(query)}`, request.signal);
      if (input.value.trim() !== query) return;
      if (data.length) render(data.slice(0, 8), 'Тайтлы');
      else hint(`Ничего не найдено по запросу «${query}»`);
    } catch (error) {
      if (error.name !== 'AbortError') hint('Поиск временно недоступен');
    }
  }

  // Small WebP variant for local covers (see scripts/optimize_covers.py), original otherwise.
  function thumbnail(url) {
    const local = /^\/images\/([\w-]+)\.(?:jpe?g|png)$/.exec(url || '');
    const img = el('img', { class: 'result__cover', alt: '', loading: 'lazy', width: '40', height: '60',
      src: local ? `/images/variants/${local[1]}-192.webp` : url });
    if (local) img.addEventListener('error', () => { img.src = url; }, { once: true });
    return img;
  }

  function hint(message) {
    items = [];
    active = -1;
    input.removeAttribute('aria-activedescendant');
    list.replaceChildren(el('p', { class: 'spotlight__hint' }, message));
  }

  function render(mangaList, heading) {
    items = mangaList.map((manga, index) => {
      const meta = el('p', { class: 'meta' },
        el('span', {}, TYPE_LABELS[manga.type] || ''),
        el('span', {}, manga.genre),
        el('span', { class: 'rating' }, icon('star'), Number(manga.rating).toFixed(1)));
      const option = el('a', {
        class: 'result', href: SLUG.test(manga.slug || '') ? `/title/${manga.slug}` : `/title/${encodeURIComponent(manga.id)}`,
        role: 'option', id: `spotlight-option-${index}`, 'aria-selected': 'false',
      },
      thumbnail(manga.imageUrl),
      el('div', { class: 'result__text' }, el('p', { class: 'result__title' }, manga.title), meta),
      icon('return', 'icon result__go'));
      option.addEventListener('mousemove', () => { if (active !== index) select(index, false); });
      return option;
    });
    list.replaceChildren(el('p', { class: 'spotlight__section' }, heading), ...items);
    select(items.length ? 0 : -1, false);
  }

  function select(index, scroll = true) {
    if (!items.length) return;
    active = (index + items.length) % items.length;
    items.forEach((item, i) => item.setAttribute('aria-selected', String(i === active)));
    input.setAttribute('aria-activedescendant', items[active].id);
    if (scroll) items[active].scrollIntoView({ block: 'nearest' });
  }
})();
