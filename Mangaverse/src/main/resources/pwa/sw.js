/*
 * MangaVerse service worker.
 * - Static assets, covers and chapter pages: cache-first (URLs are content-hashed or immutable).
 * - Pages: network-first with a 3 s timeout, then the cached copy, then the offline page.
 * - /api/** and /admin/** are never cached.
 */
const VERSION = 'v1';
const STATIC = `mv-static-${VERSION}`;
const PAGES = `mv-pages-${VERSION}`;
const MEDIA = 'mv-media';                 // kept across versions: saved chapters survive updates
const OFFLINE_URL = '/offline.html';
const MAX_PAGES = 60;
const MAX_MEDIA = 1500;

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(STATIC).then((cache) => cache.add(OFFLINE_URL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (event) => {
  event.waitUntil((async () => {
    const keep = new Set([STATIC, PAGES, MEDIA]);
    for (const key of await caches.keys()) if (key.startsWith('mv-') && !keep.has(key)) await caches.delete(key);
    await self.clients.claim();
  })());
});

const isAsset = (url) => /^\/(css|js|img)\//.test(url.pathname) || url.pathname === '/manifest.webmanifest';
const isMedia = (url) => /^\/(images|media)\//.test(url.pathname);

async function trim(cacheName, max) {
  const cache = await caches.open(cacheName);
  const keys = await cache.keys();
  for (let i = 0; i < keys.length - max; i++) await cache.delete(keys[i]);   // oldest first
}

async function cacheFirst(request, cacheName, max) {
  const cache = await caches.open(cacheName);
  const hit = await cache.match(request);
  if (hit) return hit;
  const response = await fetch(request);
  if (response.ok && response.type === 'basic') {
    await cache.put(request, response.clone());
    if (max) trim(cacheName, max);
  }
  return response;
}

async function networkFirst(request) {
  const cache = await caches.open(PAGES);
  try {
    const response = await Promise.race([
      fetch(request),
      new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), 3000)),
    ]);
    if (response.ok && response.type === 'basic') {
      await cache.put(request, response.clone());
      trim(PAGES, MAX_PAGES);
    }
    return response;
  } catch {
    return (await cache.match(request, { ignoreSearch: true }))
      || (await caches.match(OFFLINE_URL))
      || Response.error();
  }
}

self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;
  if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/admin') || url.pathname.startsWith('/actuator')) return;

  if (request.mode === 'navigate') {
    event.respondWith(networkFirst(request));
  } else if (isAsset(url)) {
    event.respondWith(cacheFirst(request, STATIC));
  } else if (isMedia(url)) {
    event.respondWith(cacheFirst(request, MEDIA, MAX_MEDIA));
  }
});
