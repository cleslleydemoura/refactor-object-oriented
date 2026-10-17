/** Capturas reais com Chrome instalado e CDP; Node 22+; nenhuma dependência npm.
 * Uso: node scripts/capturar_telas.mjs http://localhost:8080
 */
import { spawn } from 'node:child_process';
import { readFile, writeFile, mkdir, access } from 'node:fs/promises';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const base = (process.argv[2] || 'http://localhost:8080').replace(/\/$/, '');
const source = new URL(base);
if (source.protocol !== 'http:' || !['localhost', '127.0.0.1'].includes(source.hostname)) throw Error('Informe o servidor HTTP local.');
const chrome = process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
await access(chrome);
const profile = resolve(root, '.runtime', 'browser-cdp-' + Date.now());
await mkdir(profile, { recursive: true });
const browser = spawn(chrome, ['--headless=new', '--disable-gpu', '--hide-scrollbars', '--no-first-run', '--no-default-browser-check',
  '--remote-debugging-port=0', '--user-data-dir=' + profile, 'about:blank'], { windowsHide: true, stdio: 'ignore' });
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
let socket;
try {
  let port;
  for (let i = 0; i < 100; i++) {
    try { port = (await readFile(resolve(profile, 'DevToolsActivePort'), 'utf8')).split('\n')[0]; break; }
    catch { await delay(100); }
  }
  if (!port) throw Error('O navegador não iniciou o protocolo de captura.');
  const tab = await (await fetch(`http://127.0.0.1:${port}/json/new?about:blank`, { method: 'PUT' })).json();
  socket = new WebSocket(tab.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => { socket.addEventListener('open', resolve, { once: true }); socket.addEventListener('error', reject, { once: true }); });
  let nextId = 0;
  const pending = new Map();
  const listeners = new Map();
  socket.addEventListener('message', event => {
    const data = JSON.parse(event.data);
    if (data.id) {
      const item = pending.get(data.id); pending.delete(data.id);
      if (item) data.error ? item.reject(Error(JSON.stringify(data.error))) : item.resolve(data.result);
    } else {
      for (const resolve of listeners.get(data.method) || []) resolve(data.params);
      listeners.delete(data.method);
    }
  });
  const call = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++nextId; pending.set(id, { resolve, reject }); socket.send(JSON.stringify({ id, method, params }));
  });
  const once = method => new Promise(resolve => listeners.set(method, [...(listeners.get(method) || []), resolve]));
  await call('Page.enable');
  const page = await (await fetch(base + '/itens')).text();
  const id = /\/itens\/detalhes\?id=(\d+)/.exec(page)?.[1];
  if (!id) throw Error('Popule o catálogo demonstrativo antes de capturar.');
  const screens = [
    ['01_catalogo', '/itens', 1440, 1500], ['02_cadastro', '/itens/novo', 1440, 1250],
    ['03_detalhes', '/itens/detalhes?id=' + id, 1440, 1100], ['04_busca', '/itens?q=Machado', 1440, 1100],
    ['05_edicao', '/itens/editar?id=' + id, 1440, 1250], ['06_mobile', '/itens', 390, 1100],
  ];
  await mkdir(resolve(root, 'docs/telas'), { recursive: true });
  for (const [name, route, width, height] of screens) {
    await call('Emulation.setDeviceMetricsOverride', { width, height, deviceScaleFactor: 1, mobile: width < 600 });
    const loaded = once('Page.loadEventFired');
    await call('Page.navigate', { url: base + route }); await loaded;
    await call('Runtime.evaluate', { expression: 'document.fonts.ready', awaitPromise: true });
    const layout = await call('Runtime.evaluate', { expression: 'JSON.stringify({width:innerWidth,scroll:document.documentElement.scrollWidth})', returnByValue: true });
    const sizes = JSON.parse(layout.result.value);
    if (sizes.scroll > sizes.width) throw Error(`Conteúdo transborda em ${name}: ${JSON.stringify(sizes)}`);
    const shot = await call('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true, fromSurface: true,
      clip: { x: 0, y: 0, width, height, scale: 1 } });
    await writeFile(resolve(root, 'docs/telas', name + '.png'), Buffer.from(shot.data, 'base64'));
    console.log(`Captura salva: ${name}; viewport ${width} × ${height}; sem transbordamento horizontal.`);
  }
  await call('Browser.close');
} finally {
  socket?.close();
  browser.kill();
}
