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

  /* ---- Reading progress (written by reader.js) ------------------------- */
  const SLUG = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
  const CHAPTER = /^\d{1,5}(?:\.\d)?$/;
  const readProgress = () => {
    try {
      const all = JSON.parse(localStorage.getItem('mv-progress')) || {};
      // Only trust well-formed entries: they become links and image URLs.
      return Object.values(all).filter((e) => e && SLUG.test(e.slug) && CHAPTER.test(String(e.chapter))
        && typeof e.cover === 'string' && e.cover.startsWith('/') && !e.cover.startsWith('//'));
    } catch {
      return [];
    }
  };

  // Home: "Продолжить чтение" shelf.
  const continueSection = document.querySelector('[data-continue]');
  if (continueSection) {
    const entries = readProgress().filter((e) => !e.finished).sort((a, b) => b.updatedAt - a.updatedAt).slice(0, 12);
    if (entries.length) {
      const track = continueSection.querySelector('[data-continue-track]');
      track.replaceChildren(...entries.map((e) => {
        const percent = Math.round((Number(e.page) / Math.max(Number(e.total), 1)) * 100);
        const bar = el('span', { class: 'progress', role: 'progressbar', 'aria-valuenow': String(percent),
          'aria-valuemin': '0', 'aria-valuemax': '100', 'aria-label': `Прочитано ${percent}%` }, el('span'));
        bar.firstChild.style.width = `${percent}%`;
        return el('a', { class: 'card', href: `/read/${e.slug}/${e.chapter}?page=${Number(e.page) || 1}` },
          el('div', { class: 'cover' }, el('img', { src: e.cover, alt: '', width: '375', height: '562', loading: 'lazy' }), bar),
          el('h3', { class: 'card__title' }, String(e.title || '')),
          el('p', { class: 'meta' }, el('span', {}, `Гл. ${e.chapter}`), el('span', {}, `стр. ${e.page} из ${e.total}`)));
      }));
      continueSection.hidden = false;
    }
  }

  // Title page: mark read chapters and turn "Начать читать" into "Продолжить".
  const chapterList = document.querySelector('[data-chapters]');
  if (chapterList) {
    const entry = readProgress().find((e) => e.slug === chapterList.dataset.slug);
    const rows = [...chapterList.querySelectorAll('[data-chapter]')];
    if (entry) {
      const current = Number(entry.chapter);
      rows.forEach((row) => {
        const n = Number(row.dataset.chapter);
        row.classList.toggle('is-read', n < current || (n === current && entry.finished));
        row.classList.toggle('is-current', n === current && !entry.finished);
      });
      const cta = document.querySelector('[data-read-cta]');
      const label = cta?.querySelector('[data-read-cta-label]');
      if (cta && label) {
        const nextRow = rows.find((row) => Number(row.dataset.chapter) > current);
        if (!entry.finished) {
          cta.href = `/read/${entry.slug}/${entry.chapter}?page=${Number(entry.page) || 1}`;
          label.textContent = `Продолжить · гл. ${entry.chapter}`;
        } else if (nextRow) {
          cta.href = nextRow.getAttribute('href');
          label.textContent = `Читать гл. ${nextRow.dataset.chapter}`;
        } else {
          label.textContent = 'Перечитать';
        }
      }
    }

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
      el('img', { class: 'result__cover', src: manga.imageUrl, alt: '', loading: 'lazy', width: '40', height: '60' }),
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
